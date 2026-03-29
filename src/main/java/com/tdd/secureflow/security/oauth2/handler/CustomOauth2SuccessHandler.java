package com.tdd.secureflow.security.oauth2.handler;

import static com.tdd.secureflow.global.util.CookieUtil.createCookie;
import static com.tdd.secureflow.global.util.DomainUtil.extractDomain;
import static com.tdd.secureflow.interfaces.CommonCookieKey.REFRESH_TOKEN_KEY;
import static com.tdd.secureflow.interfaces.CommonHttpHeader.HEADER_AUTHORIZATION;
import static com.tdd.secureflow.interfaces.CommonSecurityScheme.BEARER_SCHEME;
import static com.tdd.secureflow.interfaces.api.controller.impl.ReIssueControllerImpl.LOGOUT_PATH;
import static com.tdd.secureflow.interfaces.api.controller.impl.ReIssueControllerImpl.TOKEN_REISSUE_PATH;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_ACCESS;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_REFRESH;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tdd.secureflow.domain.common.util.UUIDKeyGenerator;
import com.tdd.secureflow.domain.loginhistory.service.LoginHistoryService;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.CreateRefreshByEmailAndRefreshAndExpirationParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.repository.UserRepository;
import com.tdd.secureflow.security.jwt.JwtProvider;
import com.tdd.secureflow.security.oauth2.model.CustomOAuth2User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomOauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${FRONT_URL:http://localhost:3000}")
    private String frontUrl;
    private final String frontSignUpPath = "/sign"; // 프론트 회원가입 주소
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;
    private final UUIDKeyGenerator uuidKeyGenerator;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final LoginHistoryService loginHistoryService;

    public CustomOauth2SuccessHandler(
            JwtProvider jwtProvider,
            UserRepository userRepository,
            RefreshRepository refreshRepository,
            UUIDKeyGenerator uuidKeyGenerator,
            OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
            LoginHistoryService loginHistoryService
    ) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.refreshRepository = refreshRepository;
        this.uuidKeyGenerator = uuidKeyGenerator;
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
        this.loginHistoryService = loginHistoryService;
    }

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("onAuthenticationSuccess : Oauth 인증 성공");
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String email = customUserDetails.getEmail();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        // 이 시점에 이 code 와 state 값들은 이미 accessToken 과 교환해 버렸으므로  사용할 수 없는 값들이다.
        // String code = request.getParameter("code");
        // String state = request.getParameter("state");

        System.out.println("getTokens 호출");
        getTokens(authentication);

        log.info("onAuthenticationSuccess email: {}", email);
        log.info("onAuthenticationSuccess role: {}", role);
        log.info("onAuthenticationSuccess frontUrl: {}", frontUrl);
        //유저확인
        User user = userRepository.findByEmailOrNull(email);

        log.info("onAuthenticationSuccess email: {}", user.getEmail());
        log.info("onAuthenticationSuccess role: {}", user.getRole());

        // 리프레시 토큰 아이디 생성
        String refreshTokenId = uuidKeyGenerator.generate();

        // Authorization
        String accessToken = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, jwtProvider.getAccessTokenExpiration(), user.getEmail(), role, refreshTokenId);
        String refreshToken = jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, jwtProvider.getRefreshTokenExpiration(), user.getEmail(), role, refreshTokenId);

        // 기존 리프레시 토큰 삭제
        refreshRepository.revokeByEmail(new DeleteRefreshByEmailParam(user.getEmail()));
        // 새로운 리프레시 토큰 등록
        Date expiration = new Date(System.currentTimeMillis() + jwtProvider.getRefreshTokenExpiration().toMillis());
        refreshRepository.createRefresh(new CreateRefreshByEmailAndRefreshAndExpirationParam(user.getEmail(), refreshToken, refreshTokenId, expiration));

        response.addHeader(HEADER_AUTHORIZATION, String.format("%s %s", BEARER_SCHEME, accessToken));

        response.addCookie(createCookie(REFRESH_TOKEN_KEY, refreshTokenId, TOKEN_REISSUE_PATH, 24 * 60 * 60, true, extractDomain(request.getServerName())));
        response.addCookie(createCookie(REFRESH_TOKEN_KEY, refreshTokenId, LOGOUT_PATH, 24 * 60 * 60, true, extractDomain(request.getServerName())));

        // 로그인 이력 저장
        loginHistoryService.recordSuccessfulLogin(email, request);

        // 팝업 창에서 부모 창으로 메시지 전달
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");

        String frontOrigin = "http://localhost:8082";  // 실제 프론트엔드 URL로 설정
        String script = String.format(
                "<script>" +
                        "window.opener.postMessage({ accessToken: '%s', status: 'success' }, '%s');" +
                        "window.close();" +  // 메시지 전송 후 팝업 닫기
                        "</script>", accessToken, frontOrigin
        );

        response.getWriter().write(script);

        log.debug("Oauth 로그인에 성공하였습니다.");
    }

    /**
     * 현재 인증된 유저의 accessToken 과 refreshToken 을 가져온다.
     * @param authentication 인증된 유저
     * @return accessToken 과 refreshToken
     */
    public void getTokens(Authentication authentication) {
        // 2. 현재 인증된 유저의 '바구니'를 로드합니다.
        OAuth2AuthorizedClient client = oAuth2AuthorizedClientService.loadAuthorizedClient(
                "naver", // registrationId
                authentication.getName() // 유저 식별값
        );
    
        if (client != null) {
            // Access Token 꺼내기
            String access = client.getAccessToken().getTokenValue();
            log.info("accessToken: {}", access);

            // Refresh Token 꺼내기 (드디어 찾았습니다!)
            if (client.getRefreshToken() != null) {
                String refresh = client.getRefreshToken().getTokenValue();
                System.out.println("찾았다 리프레시 토큰: " + refresh);
            }
        }
    }
}
