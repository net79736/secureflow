package com.tdd.secureflow.domain.mail.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    public void send(String email) {

    }

    public boolean verify(String email, String verificationCode) {
        return true;
    }

}
