package com.tdd.secureflow.domain.user.repository;

import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.dto.UserRepositoryParam.CreateUserParam;

public interface UserRepository {
    User createUser(CreateUserParam param);

    User findByEmail(String email);

    boolean existsByEmail(String email);  // 이메일 중복 여부 확인 메서드
}
