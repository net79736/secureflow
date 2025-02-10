package com.tdd.secureflow.domain.refresh.doamin.dto;

import java.util.Date;

public class RefreshRepositoryParam {

    public record ExistsRefreshByEmailParam(String email) {

    }

    public record CreateRefreshParam(String email, String refresh, Date expiration) {

    }

    public record DeleteRefreshParam(String email) {

    }

}
