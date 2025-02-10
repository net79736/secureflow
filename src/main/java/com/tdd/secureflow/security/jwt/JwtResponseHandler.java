package com.tdd.secureflow.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Deprecated
@Component
@RequiredArgsConstructor
public class JwtResponseHandler {
    private final JwtProvider jwtProvider;
}
