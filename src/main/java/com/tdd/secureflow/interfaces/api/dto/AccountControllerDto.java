package com.tdd.secureflow.interfaces.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class AccountControllerDto {
    public record CheckDuplicateAccountRequest(
            @Pattern(regexp = "^[0-9a-zA-Z]+@[0-9a-zA-Z]+(\\.[a-zA-Z]{2,3}){1,2}$", message = "이메일 형식으로 작성해주세요")
            @NotEmpty(message = "이메일은 필수 값 입니다.")
            String email
    ) {

    }
}
