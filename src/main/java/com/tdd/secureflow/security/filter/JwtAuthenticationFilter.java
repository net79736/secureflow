package com.tdd.secureflow.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdd.secureflow.security.dto.CustomUserDetails;
import com.tdd.secureflow.security.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static com.tdd.secureflow.interfaces.CommonHttpHeader.HEADER_AUTHORIZATION;
import static com.tdd.secureflow.interfaces.CommonSecurityScheme.BEARER_SCHEME;
import static com.tdd.secureflow.security.jwt.model.JwtCategory.TOKEN_CATEGORY_ACCESS;


@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        setFilterProcessesUrl("/auth/login");
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // JSON 요청 본문에서 username과 password 추출
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, String> credentials = objectMapper.readValue(request.getInputStream(), Map.class);

            String username = credentials.get("username");
            String password = credentials.get("password");

            log.info("로그인 요청 - username: {}, password: {}", username, password);

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            log.error("로그인 요청 JSON 파싱 오류", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        try {
            // UserDetails
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

            String username = customUserDetails.getUsername();

            log.info("successfulAuthentication > username : {}", username);

            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
            GrantedAuthority auth = iterator.next();

            // 권한 획득
            String role = auth.getAuthority();

            // Authorization
            String accessToken = jwtProvider.generateToken(TOKEN_CATEGORY_ACCESS, Duration.ofDays(1), username, role);

            log.debug("print accessToken: {}", accessToken);
            log.debug("print role: {}", role);

            response.addHeader(HEADER_AUTHORIZATION, String.format("%s %s", BEARER_SCHEME, accessToken));
            response.setStatus(HttpStatus.OK.value());

            log.info("자체 서비스 로그인에 성공하였습니다.");
        } catch (InternalAuthenticationServiceException e) {
            System.out.println("successfulAuthentication 메서드 에러 발생 : " + e.getMessage());
        }
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {

        String errorMessage;

        // 예외 유형에 따라 적절한 메시지 설정
        if (failed instanceof UsernameNotFoundException) {
            System.out.println(failed.getMessage());
            System.out.println(failed.getMessage());
            System.out.println(failed.getMessage());
            System.out.println(failed.getMessage());
            System.out.println(failed.getMessage());
            System.out.println(failed.getMessage());
            System.out.println(failed.getMessage());
            System.out.println(failed.getMessage());
            System.out.println(failed.getMessage());
            System.out.println(failed.getMessage());
            errorMessage = !StringUtils.isEmpty(failed.getMessage()) ? failed.getMessage() : "존재하지 않는 계정입니다.";
        } else if (failed instanceof BadCredentialsException) {
            errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다.";
        } else if (failed instanceof DisabledException) {
            errorMessage = "비활성화된 계정입니다. 관리자에게 문의하세요.";
        } else if (failed instanceof LockedException) {
            errorMessage = "계정이 잠겨 있습니다. 관리자에게 문의하세요.";
        } else if (failed instanceof CredentialsExpiredException) {
            errorMessage = "비밀번호 유효 기간이 만료되었습니다. 비밀번호를 재설정하세요.";
        } else if (failed instanceof AccountExpiredException) {
            errorMessage = "계정 유효 기간이 만료되었습니다.";
        } else {
            // 기타 알 수 없는 에러
            errorMessage = "로그인에 실패했습니다. 다시 시도해주세요.";
        }

        // 로그로 실패 메시지 출력
        log.warn("Authentication failed: {}", errorMessage);

        // 401 상태 코드 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // JSON 형식으로 에러 메시지 응답
        response.getWriter().write(new ObjectMapper().writeValueAsString(Map.of("error", errorMessage)));
    }

}
