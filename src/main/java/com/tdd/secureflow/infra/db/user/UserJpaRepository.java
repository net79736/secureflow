package com.tdd.secureflow.infra.db.user;

import com.tdd.secureflow.domain.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);  // 기존 아이디 존재 여부 체크
}
