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
import com.tdd.secureflow.security.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

import static com.tdd.secureflow.domain.common.base.ResponseStatus.SUCCESS;
import static com.tdd.secureflow.global.util.CookieUtil.createCookie;
import static com.tdd.secureflow.global.util.DomainUtil.extractDomain;
import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static com.tdd.secureflow.interfaces.CommonHttpHeader.HEADER_AUTHORIZATION;
import static com.tdd.secureflow.interfaces.CommonSecurityScheme.BEARER_SCHEME;
import static com.tdd.secureflow.interfaces.api.controller.impl.ReIssueControllerImpl.LOGOUT_PATH;
import static com.tdd.secureflow.interfaces.api.controller.impl.ReIssueControllerImpl.TOKEN_REISSUE_PATH;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_ACCESS;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_REFRESH;

@Slf4j
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MyInfoControllerImpl implements MyInfoController {

    private final UserCommandService userCommandService;

    private final UserQueryService userQueryService;

    private final JwtProvider jwtProvider;

    @Override
    @PostMapping
    public ResponseEntity<ResponseDto> signUp(
            @RequestBody @Valid SignUpRequest request,
            BindingResult bindingResult,
            HttpServletRequest httpServletRequest,
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

        String accessToken = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofDays(1), user.getEmail(), user.getRole().name());
        // 응답 헤더 설정
        httpServletResponse.addHeader(HEADER_AUTHORIZATION, String.format("%s %s", BEARER_SCHEME, accessToken));
        String refreshToken = jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, Duration.ofDays(1), user.getEmail(), user.getRole().name());

        // Refresh Token 쿠키 추가
        httpServletResponse.addCookie(createCookie(
                REFRESH_TOKEN_KEY,
                refreshToken,
                TOKEN_REISSUE_PATH,
                24 * 60 * 60,
                true,
                extractDomain(httpServletRequest.getServerName())
        ));

        httpServletResponse.addCookie(createCookie(
                REFRESH_TOKEN_KEY,
                refreshToken,
                LOGOUT_PATH,
                24 * 60 * 60,
                true,
                extractDomain(httpServletRequest.getServerName())
        ));

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
