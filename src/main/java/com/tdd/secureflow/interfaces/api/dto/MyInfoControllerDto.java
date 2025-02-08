package com.tdd.secureflow.interfaces.api.dto;

import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.domain.model.UserRole;
import com.tdd.secureflow.domain.user.domain.model.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MyInfoControllerDto {

    public record SignUpRequest(
            @NotBlank
            @Pattern(regexp = "^[0-9a-zA-Z]+@[0-9a-zA-Z]+(\\.[a-zA-Z]{2,3}){1,2}$", message = "이메일 형식으로 작성해주세요")
            String email,

            @NotBlank
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{4,10}$", message = "비밀번호는 영문, 숫자 조합 4 ~ 10자 이내로 입력해주세요")
            String password,

            @Pattern(regexp = "^[a-zA-Z가-힣0-9]{1,10}$", message = "한글/영문/_- 1~10자 이내로 작성해주세요")
            String name
    ) {

    }

    public record SignUpResponse(
            UserResponse userResponse
    ) {

    }

    public record UserResponse(
            @Schema(description = "이메일 계정", example = "korea@naver.com")
            String email,

            @Schema(description = "비밀번호", example = "jongwook1234")
            String password,

            @Schema(description = "닉네임", example = "우욱히")
            String name,

            @Schema(description = "유저 권한", example = "USER")
            UserRole role,

            @Schema(description = "회원 유형", example = "LOCAL")
            UserType type
    ) {
        public UserResponse(User user) {
            this(user.getEmail(), user.getPassword(), user.getName(), user.getRole(), user.getType());
        }
    }

}
