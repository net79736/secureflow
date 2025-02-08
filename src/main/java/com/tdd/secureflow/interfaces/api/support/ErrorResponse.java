package com.tdd.secureflow.interfaces.api.support;

public record ErrorResponse<T>(
        String code,
        String message,
        T data
) {

}