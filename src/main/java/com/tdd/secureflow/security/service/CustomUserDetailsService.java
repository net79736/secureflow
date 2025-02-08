package com.tdd.secureflow.security.service;

import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.repository.UserRepository;
import com.tdd.secureflow.security.dto.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.tdd.secureflow.domain.user.domain.model.UserType.LOCAL;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("일반 로그인 CustomUserDetailsService 실행됨");
        log.info("username : {}", username);
        User user = userRepository.findByEmail(username);

        if (user != null) {
            // 로컬 회원인지 소셜 회원인지 확인
            if (user.getType() != LOCAL) {
                log.warn("소셜 회원이 로컬 로그인 시도 중 - 거부됨");
                return null;
            }

            log.info("유저가 존재합니다. 인증 처리 로직을 실행합니다.");
            return new CustomUserDetails(user);
        }

        log.warn("사용자를 찾을수 없습니다.");
        return null;
    }
}