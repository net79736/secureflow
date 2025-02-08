package com.tdd.secureflow.domain.user.dto;

import com.tdd.secureflow.domain.user.domain.model.UserRole;
import com.tdd.secureflow.domain.user.domain.model.UserType;

public class UserRepositoryParam {

    public record CreateUserParam(
            String email,
            String password,
            String name,
            UserRole role,
            UserType type
    ) {

    }

}
