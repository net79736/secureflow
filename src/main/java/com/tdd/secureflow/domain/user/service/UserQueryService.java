package com.tdd.secureflow.domain.user.service;

import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.tdd.secureflow.domain.support.error.CoreException.createErrorJson;
import static com.tdd.secureflow.domain.support.error.ErrorType.User.ACCOUNT_ALREADY_EXISTS;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService {

    private final UserRepository userRepository;

    public Boolean existsByEmail(String email) {
        boolean existsByEmail = userRepository.existsByEmail(email);
        if (existsByEmail) {
            throw new CoreException(ACCOUNT_ALREADY_EXISTS, createErrorJson("email", ACCOUNT_ALREADY_EXISTS.getMessage()));
        }
        return existsByEmail;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
