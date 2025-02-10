package com.tdd.secureflow.oauth2.exception;

import org.springframework.security.core.AuthenticationException;

public class ExistingUserAuthenticationException extends AuthenticationException {
    public ExistingUserAuthenticationException(String message) {
        super(message);
    }
}