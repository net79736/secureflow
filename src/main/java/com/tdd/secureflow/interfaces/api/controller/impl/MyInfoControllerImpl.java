package com.tdd.secureflow.interfaces.api.controller.impl;

import com.tdd.secureflow.domain.common.base.ResponseDto;
import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.dto.UserCommand;
import com.tdd.secureflow.domain.user.service.UserCommandService;
import com.tdd.secureflow.domain.user.service.UserQueryService;
import com.tdd.secureflow.interfaces.api.controller.MyInfoController;
import com.tdd.secureflow.interfaces.api.dto.MyInfoControllerDto.SignUpRequest;
import com.tdd.secureflow.interfaces.api.dto.MyInfoControllerDto.UserResponse;
import com.tdd.secureflow.security.dto.CustomUserDetails;
import com.tdd.secureflow.security.jwt.JwtResponseHandler;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static com.tdd.secureflow.domain.common.base.ResponseStatus.SUCCESS;

@Slf4j
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MyInfoControllerImpl implements MyInfoController {

    private final UserCommandService userCommandService;

    private final UserQueryService userQueryService;

    private final JwtResponseHandler jwtResponseHandler;

    @Override
    @PostMapping
    public ResponseEntity<ResponseDto> signUp(
            @RequestBody @Valid SignUpRequest request,
            BindingResult bindingResult,
            HttpServletResponse httpServletResponse
    ) {
        User user = userCommandService.createBasicUser(
                new UserCommand.CreateUserCommand(
                        request.email(),
                        request.password(),
                        request.confirmPassword(),
                        request.nickname()
                )
        );

        jwtResponseHandler.addAuthorizationHeader(httpServletResponse, user.getEmail(), user.getRole().name());
        UserResponse response = new UserResponse(user);
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "회원 가입 성공", response));
    }

    @Override
    @GetMapping
    public ResponseEntity<ResponseDto> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("MyInfoController getMyInfo 메서드 실행");
        
        String email = userDetails.getUsername();
        User user = userQueryService.getUserByEmail(email);
        UserResponse response = new UserResponse(user);
        return ResponseEntity.ok(new ResponseDto<>(SUCCESS.getValue(), "로그인 회원 정보 조회 성공", response));
    }
}
