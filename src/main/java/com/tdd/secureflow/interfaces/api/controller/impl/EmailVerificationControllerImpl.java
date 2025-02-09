package com.tdd.secureflow.interfaces.api.controller.impl;

import com.tdd.secureflow.domain.common.base.ResponseDto;
import com.tdd.secureflow.domain.mail.service.EmailVerificationService;
import com.tdd.secureflow.interfaces.api.controller.EmailVerificationController;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.SendVerificationEmailRequest;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.VerifyCodeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tdd.secureflow.domain.common.base.ResponseStatus.SUCCESS;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/verification")
public class EmailVerificationControllerImpl implements EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public ResponseEntity<ResponseDto> sendVerification(@Valid @RequestBody SendVerificationEmailRequest request, BindingResult bindingResult) {
        log.info("send-certification email: {}", request.email());
        emailVerificationService.send(request.email());

        return ResponseEntity.ok(new ResponseDto(SUCCESS.getValue(), "이메일 인증 코드 전송 성공", null));
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@Valid @RequestBody VerifyCodeRequest verifyCodeRequest, BindingResult bindingResult) {
        String code = verifyCodeRequest.code(); // 인증 코드
        String email = verifyCodeRequest.email(); // 이메일
        emailVerificationService.validateVerifyCode(email, code);

        log.info("certify email: {} success", email);
        return ResponseEntity.ok(new ResponseDto(SUCCESS.getValue(), "인증 코드 확인", null));
    }
}
