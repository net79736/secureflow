package com.tdd.secureflow.domain.support.email;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증 메일 발송을 HTTP 스레드가 아닌 {@link #mailTaskExecutor} 풀에서 실행하기 위한 래퍼입니다.
 * <p>
 * <b>주의:</b> {@link #sendAsync(String)} 호출 직후 메서드는 즉시 반환되며, 실제 SMTP 성공 여부는
 * 이후 백그라운드에서 결정됩니다. 전송 실패 시 API로 예외를 되돌리지 않고 로그로만 남깁니다.
 * (빠른 응답 vs 전송 결과를 응답에 포함하는 트레이드오프)
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AsyncEmailVerificationSender {

    private final EmailVerificationSender emailVerificationSender; // 실제 이메일 발송을 담당하는 클래스

    /**
     * 메일 전송을 비동기로 예약합니다. 호출자 스레드는 여기서 바로 돌아옵니다.
     *
     * @param email 수신자 주소
     */
    @Async("mailTaskExecutor")
    public void sendAsync(String email) {
        // [디버그] 브레이크포인트 ⑤ — 메일 풀 스레드 (이름에 mail-async- 포함). ①~④와 다르면 비동기 성공
        log.info("[mail-async-flow] ⑤ async sendAsync ENTRY | thread={} | email={}", Thread.currentThread().getName(), email);
        try {
            emailVerificationSender.send(email);
        } catch (Exception e) {
            // @Async 메서드에서 던진 예외는 호출자에게 전파되지 않음 → 반드시 여기서 처리
            log.error("비동기 이메일 인증 메일 전송 실패. to={}", email, e);
        }
    }
}
