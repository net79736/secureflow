package com.tdd.secureflow.domain.refresh.doamin.dto;

import java.util.Date;

public class RefreshRepositoryParam {

    public record ExistsRefreshByEmailParam(String email) {

    }

    public record CreateRefreshByEmailAndRefreshAndExpirationParam(String email, String refresh, String refreshTokenId, Date expiration) {

    }

    public record DeleteRefreshByEmailParam(String email) {

    }

}
