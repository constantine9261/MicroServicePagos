package com.bank.microservicePayment.Model.api.payment;

import lombok.Data;

@Data
public class PaymentRequest {
    private String customerId;
    private String accountId;
    private Double amount;
    private String type;  // "PAYMENT" o "EXPENSE"
    private String creditId; // Identificador del crédito (nuevo campo)

}
