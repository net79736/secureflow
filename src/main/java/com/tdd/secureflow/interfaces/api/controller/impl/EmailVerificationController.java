package com.tdd.secureflow.interfaces.api.controller.impl;

import com.tdd.secureflow.domain.mail.service.EmailVerificationService;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.SendVerificationEmailRequest;
import com.tdd.secureflow.interfaces.api.dto.EmailVerificationControllerDto.SendVerificationEmailResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/verification")
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendVerification(@Valid @RequestBody SendVerificationEmailRequest request) {
        log.info("send-certification email: {}", request.email());
        emailVerificationService.send(request.email());
        return ResponseEntity.ok(new SendVerificationEmailResponse("인증 코드 이메일 전송 성공", true));
    }

}
