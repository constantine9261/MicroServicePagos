package com.bank.microservicePayment.Model.entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "payments_and_expenses")
public class PaymentEntity implements Serializable {

    @Id
    private String id;

    private String customerId;
    private String accountId;
    private String creditId;      // Identificador del crédito asociado
    private Double amount;
    private String type;  // "PAYMENT" o "EXPENSE"
    private LocalDateTime transactionDate;
    private Double newBalance;  // Nuevo saldo después de pago o consumo
}
