package com.tdd.secureflow.oauth2.handler;

import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.CreateRefreshByEmailAndRefreshAndExpirationParam;
import com.tdd.secureflow.domain.refresh.doamin.dto.RefreshRepositoryParam.DeleteRefreshByEmailParam;
import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.repository.UserRepository;
import com.tdd.secureflow.oauth2.model.CustomOAuth2User;
import com.tdd.secureflow.security.jwt.JwtProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

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
@Component
public class CustomOauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    @Value("${FRONT_URL:http://localhost:3000}")
    private String frontUrl;
    private final String frontSignUpPath = "/sign"; // 프론트 회원가입 주소
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshRepository refreshRepository;

    public CustomOauth2SuccessHandler(JwtProvider jwtProvider, UserRepository userRepository, RefreshRepository refreshRepository) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.refreshRepository = refreshRepository;
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

        log.info("onAuthenticationSuccess email: {}", email);
        log.info("onAuthenticationSuccess role: {}", role);
        log.info("onAuthenticationSuccess frontUrl: {}", frontUrl);
        //유저확인
        User user = userRepository.findByEmailOrNull(email);

        log.info("onAuthenticationSuccess email: {}", user.getEmail());
        log.info("onAuthenticationSuccess role: {}", user.getRole());

        // Authorization
        String accessToken = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofSeconds(30), user.getEmail(), role);
        String refreshToken = jwtProvider.generateToken(TOKEN_CATEGORY_REFRESH, Duration.ofDays(1), user.getEmail(), role);

        // 기존 리프레시 토큰 삭제
        refreshRepository.deleteRefresh(new DeleteRefreshByEmailParam(user.getEmail()));
        // 새로운 리프레시 토큰 등록
        Date expiration = new Date(System.currentTimeMillis() + Duration.ofHours(24).toMillis());
        refreshRepository.createRefresh(new CreateRefreshByEmailAndRefreshAndExpirationParam(user.getEmail(), refreshToken, expiration));

        response.addHeader(HEADER_AUTHORIZATION, String.format("%s %s", BEARER_SCHEME, accessToken));

        response.addCookie(createCookie(REFRESH_TOKEN_KEY, refreshToken, TOKEN_REISSUE_PATH, 24 * 60 * 60, true, extractDomain(request.getServerName())));
        response.addCookie(createCookie(REFRESH_TOKEN_KEY, refreshToken, LOGOUT_PATH, 24 * 60 * 60, true, extractDomain(request.getServerName())));

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
}
