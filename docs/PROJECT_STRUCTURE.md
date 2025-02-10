# 프로젝트 구조

## 기술 스택

### 주요 기술

- Java 17
- Spring Boot 3.3.4
- Spring Data JPA
- Spring Security
- JWT (jjwt 0.12.6)
- Thymeleaf 3.0.11

### 빌드 도구

- Gradle(Kotlin DSL) 8.10.2

### 데이터베이스

- MySQL 8.0

### 추가 라이브러리

- commons-io 2.14.0
- Spring Boot Mail 3.3.4
- Lombok

## 패키지 구조

```
com.example.tdd.secureflow/
├── interface/                                      # 인터페이스 계층
│   ├── api/                                        # API 계층
│   │   ├── filter/                                 # 필터
│   │   ├── interceptor/                            # 인터셉터
│   │   ├── controller/                             # 컨트롤러
│   │   └── dto/                                    # DTO (Request, Response)
│   └── scheduler/                                  # 스케줄러
│
├── domain/                                         # 도메인 계층
│   ├── common/                                     # 공통
│   │   └── exception/                              # 예외
│   └── [domain]/                                   # 도메인
│       ├── model/                                  # 모델
│       ├── repository/                             # 리포지토리
│       ├── service/                                # 서비스
│       └── error/                                  # 에러
│
├── application/                                    # 응용 계층
│   └── [domain]/                                   # 도메인
│       └── facade/                                 # 퍼사드
│
├── infra/                                          # 인프라스트럭처 계층
│    └── db/                                         # 데이터베이스
│        └── [domain]/                               # 도메인
│            └── impl/                               # 구현체
│            
├── oauth2/                                        # OAuth2 모듈
│   ├── exception/                                 # OAuth2 예외
│   ├── handler/                                   # OAuth2 핸들러
│   ├── model/                                     # OAuth2 모델
│   └── service/                                   # OAuth2 서비스
│       
└── security/                                      # 보안 모듈
    ├── config/                                    # 보안 설정
    ├── dto/                                       # 보안 DTO
    ├── filter/                                    # 보안 필터
    ├── handler/                                   # 보안 핸들러
    ├── jwt/                                       # JWT 처리
    │   └── model/                                 # JWT 모델
    └── service/                                   # 보안 서비스
```