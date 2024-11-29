package com.bank.microservicePayment.business.service;



import com.bank.microservicePayment.Model.api.payment.PaymentDto;
import com.bank.microservicePayment.Model.api.payment.PaymentRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IPaymentService {
    // Método para registrar un pago o consumo
    Mono<PaymentDto> registerPaymentExpense(PaymentRequest request);

    Mono<String> payCredit(PaymentRequest request);
}