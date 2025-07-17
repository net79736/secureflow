package com.tdd.secureflow.domain.common.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class UUIDKeyGenerator {

    public String generate() {
        return generateRandomSha256Key();
    }

    private String sha256Base64Url(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public String generateRandomSha256Key() {
        String raw = UUID.randomUUID().toString() + System.nanoTime();
        return sha256Base64Url(raw);
    }
}
