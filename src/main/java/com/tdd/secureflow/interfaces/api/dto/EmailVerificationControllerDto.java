package com.tdd.secureflow.interfaces.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class EmailVerificationControllerDto {

    public record SendVerificationEmailRequest(
            @NotNull(message = "이메일을 입력해주세요.")
            @Pattern(regexp = "^[0-9a-zA-Z]+@[0-9a-zA-Z]+(\\.[a-zA-Z]{2,3}){1,2}$", message = "이메일 형식으로 작성해주세요")
            String email
    ) {

    }

    public record SendVerificationEmailResponse(
            String message,
            boolean success
    ) {

    }


}
