package com.tdd.secureflow.domain.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserRole {
    USER("일반회원"), ADMIN("관리자");
    private String value;
}
