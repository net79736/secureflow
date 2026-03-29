package com.tdd.secureflow.security.login;

import static com.tdd.secureflow.global.util.CookieUtil.createCookie;
import static com.tdd.secureflow.global.util.DomainUtil.extractDomain;
import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static com.tdd.secureflow.interfaces.CommonHttpHeader.HEADER_AUTHORIZATION;
import static com.tdd.secureflow.interfaces.CommonSecurityScheme.BEARER_SCHEME;
import static com.tdd.secureflow.interfaces.api.controller.impl.ReIssueControllerImpl.LOGOUT_PATH;
import static com.tdd.secureflow.interfaces.api.controller.impl.ReIssueControllerImpl.TOKEN_REISSUE_PATH;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_ACCESS;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_REFRESH;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tdd.secureflow.domain.common.util.UUIDKeyGenerator;
import com.tdd.secureflow.domain.loginhistory.service.LoginHistoryService;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.CreateRefreshByEmailAndRefreshAndExpirationParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.domain.user.dto.UserCommand.RecordLoginSuccessCommand;
import com.tdd.secureflow.domain.user.service.UserCommandService;
import com.tdd.secureflow.security.jwt.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 로그인 성공 후 JWT·리프레시 저장·쿠키·성공 이력까지 한 흐름으로 처리합니다.
 * (ch01 극장 예제의 Theater → TicketSeller 위임과 같이, 웹 계층은 이 서비스에게만 맡깁니다.)
 * <p>
 * 쿠키는 {@code TOKEN_REISSUE_PATH}, {@code LOGOUT_PATH}에 대해 설정되며, 브라우저에 저장되려면 해당 경로로 요청이 있어야
 * 개발자 도구에 보일 수 있습니다. CORS 환경에서는 클라이언트 {@code withCredentials}와 서버
 * {@code Access-Control-Allow-Credentials: true}가 필요합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSessionIssuer {

    private final JwtProvider jwtProvider;
    private final RefreshRepository refreshRepository;
    private final UUIDKeyGenerator uuidKeyGenerator;
    private final LoginHistoryService loginHistoryService;
    private final UserCommandService userCommandService;

    /**
     * 액세스/리프레시 토큰 발급, 저장소 반영, 응답 헤더·쿠키, 로그인 성공 이력을 수행합니다.
     *
     * @param email 로그인 주체 이메일(식별자)
     * @param role  Spring Security {@code GrantedAuthority#getAuthority()} 값 (예: ROLE_USER)
     */
    @Transactional
    public IssuedLoginSessionTokens issue(HttpServletRequest request, HttpServletResponse response, String email, String role) {
        // 리프레시 토큰 아이디 생성
        String refreshTokenId = uuidKeyGenerator.generate();

        // Authroization 토큰 발급
        String accessToken = jwtProvider.generateToken(
                TOKEN_CATEGORY_ACCESS,
                jwtProvider.getAccessTokenExpiration(),
                email,
                role,
                refreshTokenId);
        String refreshToken = jwtProvider.generateToken(
                TOKEN_CATEGORY_REFRESH,
                jwtProvider.getRefreshTokenExpiration(),
                email,
                role,
                refreshTokenId);

        // 기존 리프레시 토큰 삭제
        refreshRepository.revokeByEmail(new DeleteRefreshByEmailParam(email));

        // 새로운 리프레시 토큰 등록
        Date expiration = new Date(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpiration().toMillis());
        refreshRepository.createRefresh(
                new CreateRefreshByEmailAndRefreshAndExpirationParam(email, refreshToken, refreshTokenId, expiration));

        response.addHeader(HEADER_AUTHORIZATION, String.format("%s %s", BEARER_SCHEME, accessToken));

        String domain = extractDomain(request.getServerName());
        int cookieMaxAgeSeconds = (int) Math.min(Integer.MAX_VALUE, jwtProvider.getRefreshTokenExpiration().toSeconds());

        /**
         * 쿠키 생성 및 저장되는 과정 설명:
         * 1. /api/auth/login API 실행 시 response.addCookie()를 통해 응답 헤더에 Set-Cookie 가 설정된다.
         * 2. 클라이언트(브라우저)가 해당 응답을 수신해야 쿠키가 실제로 브라우저에 저장된다.
         * 3. 저장된 쿠키는 쿠키의 path와 일치하는 요청이 있을 때 자동으로 전송된다.
         *    (예: TOKEN_REISSUE_PATH, LOGOUT_PATH에 접근 시 자동 포함됨)
         * 4. 단, path가 "/reissue"로 설정된 쿠키는 "/reissue" 또는 그 하위 경로에만 전송되며, 다른 경로에서는 보이지 않는다.
         *
         * 🔍 이슈) 브라우저 개발자 도구에서 Application > Cookies 보려면:
         * 해당 경로로 실제 요청(fetch, axios, 브라우저 주소창 등)이 한 번 이상 발생해야 그 경로 기준의 쿠키가 그 탭에서 노출됨.
         *
         * ⚠️ 프론트엔드(CORS 환경) 주의사항:
         * - 서버에서 쿠키를 보내더라도, 클라이언트가 withCredentials: true 설정을 하지 않으면 쿠키가 저장되지 않음.
         * - 예: axios.defaults.withCredentials = true;
         * - 서버에서도 응답 헤더에 Access-Control-Allow-Credentials: true 가 설정되어야 함.
         */
        response.addCookie(createCookie(REFRESH_TOKEN_KEY, refreshTokenId, TOKEN_REISSUE_PATH, cookieMaxAgeSeconds, true, domain));
        response.addCookie(createCookie(REFRESH_TOKEN_KEY, refreshTokenId, LOGOUT_PATH, cookieMaxAgeSeconds, true, domain));

        loginHistoryService.recordSuccessfulLogin(email, request); // 로그인 성공 이력 저장
        userCommandService.recordLoginSuccess(new RecordLoginSuccessCommand(email)); // 로그인 성공 시 실패 횟수 초기화

        return new IssuedLoginSessionTokens(accessToken, refreshTokenId);
    }

    public record IssuedLoginSessionTokens(String accessToken, String refreshTokenId) {}
}
