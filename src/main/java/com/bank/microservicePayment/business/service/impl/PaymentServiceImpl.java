package com.bank.microservicePayment.business.service.impl;

import com.bank.microservicePayment.Model.api.payment.AccountDto;
import com.bank.microservicePayment.Model.api.payment.CreditDto;
import com.bank.microservicePayment.Model.api.payment.PaymentDto;
import com.bank.microservicePayment.Model.api.payment.PaymentRequest;
import com.bank.microservicePayment.Model.api.shared.ResponseDto;
import com.bank.microservicePayment.Model.api.shared.ResponseWrapper;
import com.bank.microservicePayment.Model.entity.PaymentEntity;
import com.bank.microservicePayment.business.repository.IPaymentRepository;
import com.bank.microservicePayment.business.service.IPaymentService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    @Autowired
    private IPaymentRepository paymentExpenseRepository;

    @Autowired
    private WebClient accountWebClient; // WebClient para comunicarse con el microservicio de cuentas

    @Autowired
    private WebClient customerWebClient; // WebClient para comunicarse con el microservicio de clientes

    @Autowired
    private WebClient creditServiceWebClient;

    // Método para registrar un pago o consumo
    public Mono<PaymentDto> registerPaymentExpense(PaymentRequest request) {
        return verifyCustomerExists(request.getCustomerId())
                .flatMap(customerExists -> {
                    if (!customerExists) {
                        return Mono.error(new CustomException("Cliente no válido", "ERROR_CLIENT_NOT_FOUND"));
                    }

                    return validateCreditLimit(request.getAccountId(), request.getAmount())
                            .flatMap(valid -> {
                                if (!valid && "EXPENSE".equals(request.getType())) {
                                    return Mono.error(new CustomException("Límite de crédito insuficiente", "ERROR_INSUFFICIENT_CREDIT_LIMIT"));
                                }

                                PaymentEntity entity = PaymentEntity.builder()
                                        .customerId(request.getCustomerId())
                                        .accountId(request.getAccountId())
                                        .amount(request.getAmount())
                                        .type(request.getType())
                                        .transactionDate(LocalDateTime.now())
                                        .newBalance(valid ? (request.getType().equals("EXPENSE") ? 0.0 : request.getAmount()) : 0.0)
                                        .build();

                                return paymentExpenseRepository.save(entity)
                                        .map(savedEntity -> PaymentDto.builder()
                                                .id(savedEntity.getId())
                                                .customerId(savedEntity.getCustomerId())
                                                .accountId(savedEntity.getAccountId())
                                                .amount(savedEntity.getAmount())
                                                .type(savedEntity.getType())
                                                .transactionDate(savedEntity.getTransactionDate())
                                                .newBalance(savedEntity.getNewBalance())
                                                .build());
                            });
                });
    }


    // Verificar si el cliente existe usando WebClient para llamar al microservicio de clientes
    private Mono<Boolean> verifyCustomerExists(String customerId) {
        return customerWebClient.get()
                .uri("/{id}", customerId)
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    // Validar si el límite de crédito es suficiente usando WebClient para llamar al microservicio de cuentas
    private Mono<Boolean> validateCreditLimit(String accountId, double amount) {
        return accountWebClient.get()
                .uri("/{id}", accountId) // Suponiendo que el microservicio de cuentas tenga un endpoint para obtener el límite
                .retrieve()
                .bodyToMono(Double.class)
                .flatMap(limit -> Mono.just(limit >= amount)); // Verificar si el monto solicitado no excede el límite de crédito
    }



    public Mono<String> payCredit(PaymentRequest request) {
        // Validaciones iniciales de los datos de entrada
        if (request.getCreditId() == null || request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("El ID del crédito y el monto deben ser válidos.");
        }

        log.debug("Procesando pago para creditId: {}, monto: {}", request.getCreditId(), request.getAmount());

        // Consultar el crédito desde el microservicio
        return creditServiceWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/credits/{creditId}").build(request.getCreditId()))
                .retrieve()
                .onStatus(status -> status.value() >= 400 && status.value() < 500, response -> {
                    log.error("Error 4xx al consultar el microservicio de créditos para creditId: {}", request.getCreditId());
                    return Mono.error(new IllegalArgumentException("Crédito no encontrado o solicitud inválida."));
                })
                .onStatus(status -> status.value() >= 500, response -> {
                    log.error("Error 5xx en el microservicio de créditos");
                    return Mono.error(new IllegalStateException("Servicio de créditos no disponible."));
                })
                .bodyToMono(ResponseWrapper.class)  // Suponiendo que el microservicio devuelve un ResponseWrapper
                .map(response -> response.getData())  // Extraer el campo "data" de la respuesta
                .doOnNext(credit -> log.info("Respuesta recibida del microservicio de créditos: {}", credit))
                .flatMap(credit -> {
                    // Verificar si el balance del crédito es nulo
                    if (credit.getBalance() == null) {
                        log.error("El balance del crédito es nulo para creditId: {}", credit.getId());
                        return Mono.error(new IllegalArgumentException("El balance del crédito no puede ser nulo."));
                    }

                    // Verificar si el saldo es suficiente para realizar el pago
                    if (credit.getBalance() < request.getAmount()) {
                        return Mono.error(new IllegalArgumentException("Saldo insuficiente para realizar el pago."));
                    }

                    // Actualizar el balance del crédito, descontando el monto del pago
                    credit.setBalance(credit.getBalance() - request.getAmount());
                    log.info("Saldo actualizado para creditId: {}, nuevo saldo: {}", credit.getId(), credit.getBalance());

                    // Guardar la entidad del crédito con el saldo actualizado
                    return paymentExpenseRepository.save(convertToEntity(credit))
                            .doOnSuccess(savedCredit -> log.info("Credito actualizado correctamente con saldo: {}", savedCredit.getNewBalance()))
                            .thenReturn("Pago realizado con éxito");
                })
                .doOnError(error -> log.error("Error al procesar el pago: {}", error.getMessage()));
    }



    private PaymentEntity convertToEntity(CreditDto credit) {
        return PaymentEntity.builder()
                .creditId(credit.getId())
                .amount(credit.getBalance())
                .transactionDate(LocalDateTime.now()) // Fecha de la transacción
                .build();
    }

}
