package com.tdd.secureflow.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 실행(@Async) 활성화 및 메일 전송 전용 스레드 풀 정의.
 * <p>
 * HTTP 요청 스레드에서 SMTP로 메일을 보내면 네트워크 지연만큼 응답이 늦어지므로,
 * 메일 발송은 별도 풀에서 실행하는 편이 일반적입니다.
 * </p>
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * {@link com.tdd.secureflow.domain.support.email.AsyncEmailVerificationSender} 에서
     * {@code @Async("mailTaskExecutor")} 로 지정하는 빈 이름과 일치해야 합니다.
     */
    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2); // 코어 스레드 수 (동시 실행 가능한 최소 스레드 수)
        executor.setMaxPoolSize(5); // 최대 스레드 수 (최대 동시 실행 작업 수)
        executor.setQueueCapacity(100); // 큐 용량 (최대 대기 작업 수)
        executor.setThreadNamePrefix("mail-async-"); // 스레드 접두사
        executor.initialize(); // 스레드 풀 초기화
        return executor;
    }
}
