package com.bank.microservicePayment.controller;



import com.bank.microservicePayment.Model.api.payment.PaymentDto;
import com.bank.microservicePayment.Model.api.payment.PaymentRequest;
import com.bank.microservicePayment.Model.api.shared.ResponseDto;
import com.bank.microservicePayment.Model.api.shared.ResponseDtoBuilder;
import com.bank.microservicePayment.business.service.IPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    @Autowired
    private IPaymentService paymentExpenseService;

    // Registrar un pago o consumo
    @PostMapping
    public Mono<ResponseDto<PaymentDto>> registerPaymentExpense(@RequestBody PaymentRequest request) {
        return paymentExpenseService.registerPaymentExpense(request)
                .map(paymentDto -> ResponseDtoBuilder.success(paymentDto, "Transacción registrada con éxito"))
                .onErrorResume(e -> {
                    // Manejo de errores, como excepciones personalizadas
                    return Mono.just(ResponseDtoBuilder.error("Error desconocido"));
                });
    }
}
