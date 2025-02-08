package com.tdd.secureflow.infra.db.user.impl;

import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.dto.UserRepositoryParam.CreateUserParam;
import com.tdd.secureflow.domain.user.repository.UserRepository;
import com.tdd.secureflow.infra.db.user.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.tdd.secureflow.domain.support.error.ErrorType.User.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User createUser(CreateUserParam param) {
        return userJpaRepository.save(User.builder()
                .email(param.email())
                .password(param.password())
                .name(param.name())
                .role(param.role())
                .type(param.type())
                .build());
    }

    @Override
    public User findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .orElseThrow(() -> new CoreException(USER_NOT_FOUND));
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}
