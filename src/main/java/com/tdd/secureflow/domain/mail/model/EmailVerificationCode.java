package com.tdd.secureflow.domain.mail.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class EmailVerificationCode {
    private final String code;
    private final LocalDateTime expirationTime;
}
