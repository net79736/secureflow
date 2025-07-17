package com.tdd.secureflow.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import lombok.Getter;

@Getter
@ConfigurationProperties("jwt")
public class JwtProperties {
    private final String issuer;
    private final String secretKey;
    private final AccessToken accessToken;
    private final RefreshToken refreshToken;
    private final Refresh refresh;

    @ConstructorBinding
    public JwtProperties(String issuer, String secretKey, AccessToken accessToken, RefreshToken refreshToken, Refresh refresh) {
        this.issuer = issuer;
        this.secretKey = secretKey;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.refresh = refresh;
    }

    @Getter
    public static class AccessToken {
        private final long expiration;

        @ConstructorBinding
        public AccessToken(long expiration) {
            this.expiration = expiration;
        }
    }

    @Getter
    public static class RefreshToken {
        private final long expiration;

        @ConstructorBinding
        public RefreshToken(long expiration) {
            this.expiration = expiration;
        }
    }

    @Getter
    public static class Refresh {
        private final long renewalThresholdMs;

        @ConstructorBinding
        public Refresh(long renewalThresholdMs) {
            this.renewalThresholdMs = renewalThresholdMs;
        }
    }
}