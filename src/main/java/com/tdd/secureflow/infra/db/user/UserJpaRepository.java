package com.tdd.secureflow.infra.db.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tdd.secureflow.domain.user.domain.model.User;

public interface UserJpaRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);  // 기존 아이디 존재 여부 체크
}
