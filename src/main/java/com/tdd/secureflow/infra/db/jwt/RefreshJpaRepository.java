package com.tdd.secureflow.infra.db.jwt;

import com.tdd.secureflow.domain.refresh.doamin.model.Refresh;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshJpaRepository extends JpaRepository<Refresh, Long> {
    Boolean existsByEmail(String email);

    void deleteByEmail(String email);
}
