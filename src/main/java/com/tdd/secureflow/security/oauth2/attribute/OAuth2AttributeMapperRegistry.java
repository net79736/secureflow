package com.tdd.secureflow.security.oauth2.attribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Component
public class OAuth2AttributeMapperRegistry {
    private final Map<String, OAuth2AttributeMapper> mappersByRegistrationId;

    /**
     * 생성자
     * 주입받은 OAuth2AttributeMapper 목록을 Map으로 변환하여 초기화합니다.
     * @param mappers
     */
    public OAuth2AttributeMapperRegistry(List<OAuth2AttributeMapper> mappers) {
        log.info("OAuth2AttributeMapperRegistry > mappers: {}", mappers);

        Map<String, OAuth2AttributeMapper> map = new HashMap<>();
        for (OAuth2AttributeMapper mapper : mappers) {
            OAuth2AttributeMapper previous = map.putIfAbsent(
                mapper.getRegistrationId(),
                mapper
            );
            if (previous != null) {
                throw new IllegalStateException(
                        "Duplicate OAuth2AttributeMapper for registrationId: "
                                + mapper.getRegistrationId()
                );
            }
        }
        this.mappersByRegistrationId = Collections.unmodifiableMap(map);
    }

    public OAuth2AttributeMapper getOrNull(String registrationId) {
        return mappersByRegistrationId.get(registrationId);
    }
}
