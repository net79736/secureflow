package com.tdd.secureflow.domain.user.service;

import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.dto.UserCommand.CreateUserCommand;
import com.tdd.secureflow.domain.user.dto.UserRepositoryParam.CreateUserParam;
import com.tdd.secureflow.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tdd.secureflow.domain.support.error.CoreException.createErrorJson;
import static com.tdd.secureflow.domain.support.error.ErrorType.User.*;
import static com.tdd.secureflow.domain.user.domain.model.UserRole.USER;
import static com.tdd.secureflow.domain.user.domain.model.UserType.LOCAL;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandService {

    private final UserRepository userRepository;

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
                        command.password(),
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
}
