package com.tdd.secureflow.domain.user.service;

import static com.tdd.secureflow.domain.support.error.CoreException.createErrorJson;
import static com.tdd.secureflow.domain.support.error.ErrorType.User.ACCOUNT_ALREADY_EXISTS;
import static com.tdd.secureflow.domain.support.error.ErrorType.User.CONFIRM_PASSWORD_NOT_MATCHING;
import static com.tdd.secureflow.domain.support.error.ErrorType.User.PASSWORD_MUST_NOT_BE_NULL;
import static com.tdd.secureflow.domain.user.domain.model.UserRole.USER;
import static com.tdd.secureflow.domain.user.domain.model.UserType.LOCAL;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.dto.UserCommand.CreateUserCommand;
import com.tdd.secureflow.domain.user.dto.UserCommand.RecordLoginFailureCommand;
import com.tdd.secureflow.domain.user.dto.UserCommand.RecordLoginSuccessCommand;
import com.tdd.secureflow.domain.user.dto.UserRepositoryParam.CreateUserParam;
import com.tdd.secureflow.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public User createBasicUser(CreateUserCommand command) {
        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(command.email())) {
            log.warn("이메일 중복: {}", command.email());
            throw new CoreException(ACCOUNT_ALREADY_EXISTS, createErrorJson("email", ACCOUNT_ALREADY_EXISTS.getMessage()));
        }

        validatePasswordMatching(command.password(), command.confirmPassword());

        return userRepository.createUser(
                new CreateUserParam(
                        command.email(),
                        passwordEncoder.encode(command.password()),
                        command.name(),
                        USER,
                        LOCAL
                )
        );
    }

    /**
     * 비밀번호와 비밀번호 확인이 일치하는지 검증하는 메서드
     */
    public void validatePasswordMatching(String password, String confirmPassword) {
        if (password == null) {
            throw new CoreException(PASSWORD_MUST_NOT_BE_NULL, CoreException.createErrorJson("password", PASSWORD_MUST_NOT_BE_NULL.getMessage()));
        }

        if (!password.equals(confirmPassword)) {
            throw new CoreException(PASSWORD_MUST_NOT_BE_NULL, CoreException.createErrorJson("confirmPassword", CONFIRM_PASSWORD_NOT_MATCHING.getMessage()));
        }
    }

    /**
     * 로그인 성공 시 마지막 로그인 시각 갱신 및 실패 횟수 초기화
     * @param command
     */
    public void recordLoginSuccess(RecordLoginSuccessCommand command) {
        String email = command.email();
        if (email == null || email.isBlank()) {
            return;
        }
        User user = userRepository.findByEmailOrNull(email);
        if (user == null) {
            return;
        }
        user.recordLoginSuccess();
        userRepository.save(user);
    }

    /**
     * 로그인 실패 시 실패 횟수 증가
     * @param command
     */
    public void recordLoginFailure(RecordLoginFailureCommand command) {
        String email = command.email();
        if (email == null || email.isBlank()) {
            return;
        }
        User user = userRepository.findByEmailOrNull(email);
        if (user == null) {
            return;
        }
        user.incrementLoginFailureCount();
        userRepository.save(user);
    }
}
