package com.tdd.secureflow.domain.refresh.doamin.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Tokens {
    private final String accessToken;
    private final String refreshToken;
}
