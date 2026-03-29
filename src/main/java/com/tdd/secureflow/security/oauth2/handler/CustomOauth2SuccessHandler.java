package com.tdd.secureflow.security.oauth2.handler;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tdd.secureflow.domain.user.domain.model.User;
import com.tdd.secureflow.domain.user.repository.UserRepository;
import com.tdd.secureflow.security.login.LoginSessionIssuer;
import com.tdd.secureflow.security.login.LoginSessionIssuer.IssuedLoginSessionTokens;
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
    private final UserRepository userRepository;
    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;
    private final LoginSessionIssuer loginSessionIssuer;

    public CustomOauth2SuccessHandler(
            UserRepository userRepository,
            OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
            LoginSessionIssuer loginSessionIssuer
    ) {
        this.userRepository = userRepository;
        this.oAuth2AuthorizedClientService = oAuth2AuthorizedClientService;
        this.loginSessionIssuer = loginSessionIssuer;
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

        IssuedLoginSessionTokens issued = loginSessionIssuer.issue(request, response, user.getEmail(), role);
        String accessToken = issued.accessToken();

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
