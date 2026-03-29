package com.tdd.secureflow.security.oauth2.attribute;

import java.util.Map;

import com.tdd.secureflow.security.oauth2.model.response.OAuth2Response;

/**
 * 제공자별 userInfo JSON 구조를 {@link OAuth2Response}로 변환한다.
 */
public interface OAuth2AttributeMapper {
    /** 이 매퍼가 담당하는 client registration id (예: "google", "naver"). */
    String getRegistrationId();

    /**
     * 제공자별 userInfo JSON 구조를 {@link OAuth2Response}로 변환한다.
     * @param attributes OAuth2User#getAttributes()
     * @return OAuth2Response
     */
    OAuth2Response map(Map<String, Object> attributes);
}