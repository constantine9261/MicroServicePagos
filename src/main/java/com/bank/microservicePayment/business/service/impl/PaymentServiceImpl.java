package com.bank.microservicePayment.business.service.impl;

import com.bank.microservicePayment.Model.api.payment.AccountDto;
import com.bank.microservicePayment.Model.api.payment.PaymentDto;
import com.bank.microservicePayment.Model.api.payment.PaymentRequest;
import com.bank.microservicePayment.Model.api.shared.ResponseDto;
import com.bank.microservicePayment.Model.entity.PaymentEntity;
import com.bank.microservicePayment.business.repository.IPaymentRepository;
import com.bank.microservicePayment.business.service.IPaymentService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    @Autowired
    private IPaymentRepository paymentExpenseRepository;

    @Autowired
    private WebClient accountWebClient; // WebClient para comunicarse con el microservicio de cuentas

    @Autowired
    private WebClient customerWebClient; // WebClient para comunicarse con el microservicio de clientes

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

}
