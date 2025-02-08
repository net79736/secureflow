package com.tdd.secureflow.interfaces.api.support;

public record ErrorResponse(
        String code,
        String message
) {

}