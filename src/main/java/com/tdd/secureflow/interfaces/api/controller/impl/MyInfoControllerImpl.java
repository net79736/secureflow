package com.tdd.secureflow.interfaces.api.controller.impl;

import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.dto.UserCommand;
import com.tdd.secureflow.domain.user.service.UserCommandService;
import com.tdd.secureflow.interfaces.api.controller.MyInfoController;
import com.tdd.secureflow.interfaces.api.dto.MyInfoControllerDto.SignUpRequest;
import com.tdd.secureflow.interfaces.api.dto.MyInfoControllerDto.SignUpResponse;
import com.tdd.secureflow.interfaces.api.dto.MyInfoControllerDto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MyInfoControllerImpl implements MyInfoController {

    private final UserCommandService userCommandService;

    @Override
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@RequestBody @Valid SignUpRequest request, BindingResult bindingResult) {
        User user = userCommandService.createBasicUser(
                new UserCommand.CreateUserCommand(
                        request.email(),
                        request.password(),
                        request.name()
                )
        );

        UserResponse response = new UserResponse(user);

        return ResponseEntity.ok(new SignUpResponse("SUCCESS", "회원 가입 성공", response));
    }

}
