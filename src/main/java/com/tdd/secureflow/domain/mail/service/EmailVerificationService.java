package com.tdd.secureflow.domain.mail.service;

import com.tdd.secureflow.domain.support.email.EmailVerificationSender;
import com.tdd.secureflow.domain.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final EmailVerificationSender emailVerificationSender;

    public void send(String email) {
        emailVerificationSender.send(email);
    }

    public void validateVerifyCode(String email, String certificationCode) throws CoreException {
        emailVerificationSender.print();
        emailVerificationSender.validateVerificationCode(email, certificationCode);
    }

}
