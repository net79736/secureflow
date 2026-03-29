package com.tdd.secureflow.security.service;

import static com.tdd.secureflow.domain.user.domain.model.UserType.LOCAL;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.repository.UserRepository;
import com.tdd.secureflow.security.dto.CustomUserDetails;

import lombok.extern.slf4j.Slf4j;

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

        // findByEmail 은 없으면 CoreException 이라 DaoAuthenticationProvider 가 UsernameNotFoundException 으로 인식하지 못함.
        // 로그인 실패 유형(LoginFailureMessage.USER_NOT_FOUND 등)과 맞추려면 findByEmailOrNull + UsernameNotFoundException 사용.
        User user = userRepository.findByEmailOrNull(username);

        if (user == null) {
            log.warn("사용자를 찾을 수 없습니다.");
            throw new UsernameNotFoundException("해당 사용자를 찾을 수 없습니다.");
        }

        if (user.getType() != LOCAL) {
            log.warn("소셜 회원이 로컬 로그인 시도 중 - 거부됨");
            throw new UsernameNotFoundException("이 계정은 소셜 로그인 전용입니다. 소셜 로그인을 이용해주세요.");
        }

        log.info("유저가 존재합니다. 인증 처리 로직을 실행합니다.");
        return new CustomUserDetails(user);
    }
}
