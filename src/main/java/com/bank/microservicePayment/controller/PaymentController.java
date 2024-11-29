package com.bank.microservicePayment.controller;



import com.bank.microservicePayment.Model.api.payment.PaymentDto;
import com.bank.microservicePayment.Model.api.payment.PaymentRequest;
import com.bank.microservicePayment.Model.api.shared.ResponseDto;
import com.bank.microservicePayment.Model.api.shared.ResponseDtoBuilder;
import com.bank.microservicePayment.business.service.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Registrar un pago o consumo", description = "Registra una nueva transacción de pago o consumo basado en la solicitud proporcionada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transacción registrada con éxito",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida"),
            @ApiResponse(responseCode = "500", description = "Error desconocido")
    })
    @PostMapping
    public Mono<ResponseDto<PaymentDto>> registerPaymentExpense(@RequestBody PaymentRequest request) {
        return paymentExpenseService.registerPaymentExpense(request)
                .map(paymentDto -> ResponseDtoBuilder.success(paymentDto, "Transacción registrada con éxito"))
                .onErrorResume(e -> {
                    // Manejo de errores, como excepciones personalizadas
                    return Mono.just(ResponseDtoBuilder.error("Error desconocido"));
                });
    }


    @Operation(summary = "Pagar producto de crédito", description = "Permite a un cliente pagar un producto de crédito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pago realizado con éxito"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/pay-credit")
    public Mono<ResponseDto<String>> payCredit(@RequestBody PaymentRequest request) {
        return paymentExpenseService.payCredit(request)
                .map(response -> ResponseDtoBuilder.success("Pago realizado con éxito", response));
    }

}
