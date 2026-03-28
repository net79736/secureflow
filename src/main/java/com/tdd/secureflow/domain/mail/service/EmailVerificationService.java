package com.tdd.secureflow.domain.mail.service;

import org.springframework.stereotype.Service;

import com.tdd.secureflow.domain.support.email.AsyncEmailVerificationSender;
import com.tdd.secureflow.domain.support.email.EmailVerificationSender;
import com.tdd.secureflow.domain.support.error.CoreException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이메일 인증 유스케이스 진입점.
 * 발송은 {@link AsyncEmailVerificationSender} 를 통해 별도 스레드 풀에서 수행합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final AsyncEmailVerificationSender asyncEmailVerificationSender;
    private final EmailVerificationSender emailVerificationSender;

    /**
     * 인증 메일 발송을 비동기로 요청합니다. 이 메서드는 SMTP 완료를 기다리지 않고 끝납니다.
     */
    public void send(String email) {
        // [디버그] 브레이크포인트 ③ — ①②와 동일하게 HTTP 스레드
        log.info("[mail-async-flow] ③ service BEFORE sendAsync | thread={} | email={}", Thread.currentThread().getName(), email);
        asyncEmailVerificationSender.sendAsync(email);
        // [디버그] 브레이크포인트 ④ — @Async 는 여기서 바로 돌아옴(메일 전송 스레드와 다름)
        log.info("[mail-async-flow] ④ service AFTER sendAsync (즉시 반환) | thread={}", Thread.currentThread().getName());
    }

    /**
     * 인증 코드 검증 — 메모리에 저장된 코드와 비교 (동기, 요청 스레드에서 실행).
     */
    public void validateVerifyCode(String email, String certificationCode) throws CoreException {
        emailVerificationSender.print();
        emailVerificationSender.validateVerificationCode(email, certificationCode);
    }
}
