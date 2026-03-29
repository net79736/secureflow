package com.tdd.secureflow.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@link com.tdd.secureflow.config.SecurityConfig}와 {@link com.tdd.secureflow.domain.user.service.UserCommandService} 사이
 * 순환 참조를 막기 위해 비밀번호 인코더만 별도 구성합니다.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
