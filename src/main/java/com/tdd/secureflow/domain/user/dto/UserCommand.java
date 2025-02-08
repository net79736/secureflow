package com.tdd.secureflow.domain.user.dto;

public class UserCommand {

    public record CreateUserCommand(
            String email,
            String password,
            String name
    ) {

    }

}
