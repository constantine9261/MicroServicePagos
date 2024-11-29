package com.bank.microservicePayment.Model.api.shared;

import com.bank.microservicePayment.Model.api.payment.CreditDto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseWrapper {

    private CreditDto data;
    private String message;
    private String status;
}
