package com.bank.microservicePayment.Model.api.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceUpdateRequest {
    private Double newBalance;

}
