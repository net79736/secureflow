package com.tdd.secureflow.domain.user.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.tdd.secureflow.domain.user.domain.model.UserRole.USER;
import static com.tdd.secureflow.domain.user.domain.model.UserType.LOCAL;

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
}
