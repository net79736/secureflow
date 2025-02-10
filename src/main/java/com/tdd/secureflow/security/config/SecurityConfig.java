package com.tdd.secureflow.security.config;

import com.tdd.secureflow.domain.refresh.doamin.repository.RefreshRepository;
import com.tdd.secureflow.domain.user.domain.model.UserRole;
import com.tdd.secureflow.interfaces.WebConfig;
import com.tdd.secureflow.security.filter.JwtAuthenticationFilter;
import com.tdd.secureflow.security.filter.TokenAuthenticationFilter;
import com.tdd.secureflow.security.handler.AuthenticationEntryPointHandler;
import com.tdd.secureflow.security.handler.CustomAccessDeniedHandler;
import com.tdd.secureflow.security.handler.CustomLogoutSuccessHandler;
import com.tdd.secureflow.security.jwt.JwtProvider;
import com.tdd.secureflow.security.service.CustomUserDetailsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /* 권한 제외 대상 */
    private static final String[] PERMIT_ALL_URLS = new String[]{
            "/", "/index", "/login", "/signup"
    };
    /* Admin 접근 권한 */
    private static final String[] PERMIT_ADMIN_URLS = new String[]{};
    /* member 접근 권한 */
    private static final String[] PERMIT_MEMBER_URLS = new String[]{};

    @Value("${FRONT_URL:http://localhost:8080}")
    private String frontUrl;
    private final JwtProvider jwtProvider;
    private final WebConfig webConfig;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshRepository refreshRepository;
//    private final CustomOAuth2UserService customOAuth2UserService;
//    private final CustomOauth2SuccessHandler customOauth2SuccessHandler;
//    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
//    private final RefreshJpaRepository refreshJpaRepository;

    @PostConstruct
    public void init() {
        log.debug("init security config");
        log.debug("frontUrl = {}", frontUrl);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("BCryptPasswordEncoder 빈 등록됨");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // HTTP 헤더 설정
        http.headers(headers -> headers
                .httpStrictTransportSecurity(HeadersConfigurer.HstsConfig::disable) // HSTS 비활성화
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::disable)        // FrameOptions 비활성화
        );

        // 기본 보안 설정 비활성화
        http.logout((auth) -> auth.disable()) // 로그아웃 비활성화
                .csrf((auth) -> auth.disable()) // csrf disable
                .formLogin((auth) -> auth.disable()) // From 로그인 방식 disable
                .httpBasic((auth) -> auth.disable()); // HTTP Basic 인증 방식 disable

        // 세션 관리: Stateless
        http.sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );

        // OAuth2 로그인 설정
//        http.oauth2Login(oauth2 -> oauth2
//                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
//                .successHandler(customOauth2SuccessHandler)
//                .failureHandler(oAuth2LoginFailureHandler)
//        );

        // JWT 인증 및 토큰 검증 필터 추가
        http.addFilterAt(
                        new JwtAuthenticationFilter(authenticationManager(), jwtProvider, refreshRepository),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterAfter(new TokenAuthenticationFilter(jwtProvider), JwtAuthenticationFilter.class)
                .addFilter(webConfig.corsFilter()); // CORS 필터 추가

        // 로그아웃 설정
        http.logout(logout -> logout
                .logoutUrl("/auth/logout")
                .invalidateHttpSession(true)
                .logoutSuccessHandler(new CustomLogoutSuccessHandler(jwtProvider))
                .permitAll()
        );

        // 예외 처리 핸들러 설정
        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new AuthenticationEntryPointHandler())
                .accessDeniedHandler(new CustomAccessDeniedHandler())
        );

        // 경로별 인가 작업
        http.authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                        .requestMatchers(PERMIT_ALL_URLS).permitAll()
                        .requestMatchers(PERMIT_ADMIN_URLS).hasAnyAuthority(UserRole.ADMIN.name())
                        .anyRequest().permitAll()                   // 나머지 요청은 모두 허용
        );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(customUserDetailsService);
        // 로그인 실패 이유를 구체적으로 구분하고 싶을 때 사용하는 설정
        provider.setHideUserNotFoundExceptions(false);  // 예외 숨김 해제 (별도 Exception 으로 처리하기)
        return new ProviderManager(provider);
    }
}

