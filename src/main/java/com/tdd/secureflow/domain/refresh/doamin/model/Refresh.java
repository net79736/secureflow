package com.tdd.secureflow.domain.refresh.doamin.model;

import com.tdd.secureflow.domain.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "refreshes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refresh extends BaseEntity {
    @Column(nullable = false)
    private String email;
    @Column(nullable = false, columnDefinition = "BLOB")
    private String refresh;
    @Column(nullable = false)
    private Instant expiration;

    @Builder
    public Refresh(String email, String refresh, Instant expiration) {
        this.email = email;
        this.refresh = refresh;
        this.expiration = expiration;
    }
}