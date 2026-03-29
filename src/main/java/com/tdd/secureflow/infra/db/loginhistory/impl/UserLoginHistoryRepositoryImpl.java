package com.tdd.secureflow.infra.db.loginhistory.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tdd.secureflow.domain.loginhistory.model.UserLoginHistory;
import com.tdd.secureflow.domain.loginhistory.repository.UserLoginHistoryRepository;
import com.tdd.secureflow.infra.db.loginhistory.UserLoginHistoryJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserLoginHistoryRepositoryImpl implements UserLoginHistoryRepository {

    private final UserLoginHistoryJpaRepository userLoginHistoryJpaRepository;

    @Override
    public UserLoginHistory save(UserLoginHistory entity) {
        return userLoginHistoryJpaRepository.save(entity);
    }

    @Override
    public Optional<UserLoginHistory> findLatestOpenSession(String userId) {
        // 해당 유저의 가장 최근 로그인 이력을 조회
        return userLoginHistoryJpaRepository.findTopByUserIdAndFinalDttmIsNullOrderByLoginDttmDesc(userId);
    }
}
