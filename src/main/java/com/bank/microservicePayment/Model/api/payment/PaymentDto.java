package com.bank.microservicePayment.Model.api.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor  // Genera un constructor público con todos los campos
public class PaymentDto {

    private String id;

    private String customerId;
    private String accountId;
    private Double amount;
    private String type;  // "PAYMENT" o "EXPENSE"
    private LocalDateTime transactionDate;
    private Double newBalance;  // Nuevo saldo después de pago o consumo
}
