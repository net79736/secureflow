package com.tdd.secureflow.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static com.tdd.secureflow.interfaces.CommonSecurityScheme.BEARER_SCHEME;

@Component
@AllArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;

    /**
     * 토큰 발급
     *
     * @param duration Duration 만료 기간
     * @param username String
     * @param role     String
     * @return String
     */
    public String generateToken(String category, Duration duration, String username, String role) {
        Date now = new Date();
        return makeToken(category, new Date(now.getTime() + duration.toMillis()), username, role);
    }

    /**
     * 토큰 생성
     *
     * @param category       토큰 종류 구분 (access | refresh)
     * @param expirationDate 만료 기간
     * @param username       이메일
     * @param role           권한
     * @return
     */
    private String makeToken(String category, Date expirationDate, String username, String role) {
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .issuedAt(new Date())
                .expiration(expirationDate)
                .claim("category", category)
                .claim("email", username)
                .claim("role", role)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 토큰 유효성 검사
     *
     * @param token String
     * @return boolean
     */
    public boolean validToken(final String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료된 토큰은 구조적으로 유효하다고 판단
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰으로부터 Authentication 객체를 가져옴
     *
     * @param token String
     * @return Authentication
     */
    public Authentication getAuthentication(final String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(
                new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class)));
        return new UsernamePasswordAuthenticationToken(UUID.fromString(claims.get("id", String.class)), token,
                authorities);
    }

    /**
     * 토큰으로부터 사용자 publicId를 추출
     *
     * @param token String
     * @return Email
     */
    public String getEmail(final String token) {
        Claims claims = getClaims(token);
        return claims.get("email", String.class);
    }

    /**
     * 토큰으로부터 사용자 권한(Authorities)을 추출
     *
     * @param token String
     * @return Set<SimpleGrantedAuthority>
     */
    public String getRole(final String token) {
        Claims claims = getClaims(token);
        return claims.get("role", String.class);
    }

    /**
     * 토큰으로부터 사용자 상태(status)을 추출
     *
     * @param token String
     * @return String
     */
    public String getStatus(final String token) {
        Claims claims = getClaims(token);
        return claims.get("status", String.class);
    }

    /**
     * 토큰으로부터 Claims를 가져옴
     *
     * @param token String
     * @return Claims
     */
    private Claims getClaims(String token) {
        Jws<Claims> claimsJws = Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
        return claimsJws.getPayload();
    }

    /**
     * 서명 키 생성
     *
     * @return SecretKey
     */
    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtProperties.getSecretKey()));
    }

    /**
     * authorization header 에서 access token 을 추출합니다.
     *
     * @param authorizationHeader : String authorization header
     * @return String access token
     */
    public String getAccessToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_SCHEME)) {
            // BEARER_SCHEME ("Bearer ") 제거 및 공백 제거
            return authorizationHeader.substring(BEARER_SCHEME.length()).trim();
        }
        return null; // Authorization 헤더가 없거나 잘못된 형식일 경우 null 반환
    }


    /**
     * 토큰으로부터 카테고리를 추출
     *
     * @param token String
     * @return UUID
     */
    public String getCategory(String token) {
        Claims claims = getClaims(token);
        return claims.get("category", String.class);
    }

    /**
     * 토큰이 만료되었는지 확인하는 메서드
     *
     * @param token JWT 토큰
     * @return 토큰이 만료되었으면 true, 그렇지 않으면 false
     */
    public Boolean isExpired(String token) {
        // throws JwtException, IllegalArgumentException
        try {
            return Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token).getPayload().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            // 토큰 만료로 인한 예외 처리
            return true; // 만료된 것으로 판단
        }
    }
}