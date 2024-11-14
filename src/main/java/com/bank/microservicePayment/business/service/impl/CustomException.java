package com.bank.microservicePayment.business.service.impl;

public class CustomException extends RuntimeException {

    private String errorCode;

    // Constructor
    public CustomException(String message, String errorCode) {
        super(message);  // Llamada al constructor de RuntimeException
        this.errorCode = errorCode;  // Almacenar el código de error
    }

    // Getter para el código de error
    public String getErrorCode() {
        return errorCode;
    }
}
