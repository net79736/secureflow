package com.tdd.secureflow.domain.user.domain.model;

import static com.tdd.secureflow.domain.user.domain.model.UserRole.USER;
import static com.tdd.secureflow.domain.user.domain.model.UserType.LOCAL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "\"users\"")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @Column(nullable = false, unique = true, length = 60)
    private String email; // 계정

    @Column(nullable = false, length = 60) // 패스워드 인코딩(BCrypt)
    private String password; // 비밀번호

    @Column(length = 60)
    private String name;

    @Column(length = 200)
    private String refreshTokenId; // 리프레시 토큰 아이디

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private UserType type = LOCAL;

//    @Enumerated(EnumType.STRING)
//    @Column(nickname = "status", nullable = false)
//    private MemberStatus status = PENDING;

    @Builder
    public User(String email, String password, String name, UserRole role, UserType type) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.type = type;
    }

    @Builder
    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public void setRefreshTokenId(String refreshTokenId) {
        this.refreshTokenId = refreshTokenId;
    }
}
