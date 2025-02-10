package com.tdd.secureflow.interfaces.api.controller.impl;

import com.tdd.secureflow.domain.common.base.ResponseDto;
import com.tdd.secureflow.domain.common.base.ResponseStatus;
import com.tdd.secureflow.domain.refresh.doamin.model.Tokens;
import com.tdd.secureflow.domain.refresh.doamin.service.ReIssueCommandService;
import com.tdd.secureflow.domain.support.error.CoreException;
import com.tdd.secureflow.interfaces.api.controller.ReIssueController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tdd.secureflow.domain.support.error.ErrorType.INTERNAL_SERVER_ERROR;
import static com.tdd.secureflow.global.util.CookieUtil.createCookie;
import static com.tdd.secureflow.global.util.DomainUtil.extractDomain;
import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static com.tdd.secureflow.interfaces.CommonHttpHeader.HEADER_AUTHORIZATION;
import static com.tdd.secureflow.interfaces.CommonSecurityScheme.BEARER_SCHEME;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReIssueControllerImpl implements ReIssueController {

    private final ReIssueCommandService reIssueService;
    public final static String TOKEN_REISSUE_PATH = "/reissue";
    public final static String LOGOUT_PATH = "/logout";

    @Override
    @PostMapping(TOKEN_REISSUE_PATH)
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        log.info("ReIssueController reissue START");
        try {
            // 서비스 호출로 모든 로직을 위임
            Tokens tokens = reIssueService.reissueTokens(request);

            // Access Token 응답 헤더 추가
            response.addHeader(HEADER_AUTHORIZATION, String.format("%s %s", BEARER_SCHEME, tokens.getAccessToken()));

            // Refresh Token 쿠키 추가
            response.addCookie(createCookie(
                    REFRESH_TOKEN_KEY,
                    tokens.getRefreshToken(),
                    TOKEN_REISSUE_PATH,
                    24 * 60 * 60,
                    true,
                    extractDomain(request.getServerName())
            ));

            response.addCookie(createCookie(
                    REFRESH_TOKEN_KEY,
                    tokens.getRefreshToken(),
                    LOGOUT_PATH,
                    24 * 60 * 60,
                    true,
                    extractDomain(request.getServerName())
            ));
            return ResponseEntity.ok(new ResponseDto<>(ResponseStatus.SUCCESS.getValue(), "토큰 재발급 성공", null));
        } catch (CoreException e) {
            // 서비스에서 발생한 PlayHiveException 을 그대로 재던짐
            log.error("서비스에서 발생한 PlayHiveException");
            log.error(e.getMessage());
            throw e;
        } catch (Exception e) {
            // 일반 예외는 PlayHiveException 으로 에러를 던짐
            log.error("Unexpected error during token reissue :" + e.getMessage());
            throw new CoreException(INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
