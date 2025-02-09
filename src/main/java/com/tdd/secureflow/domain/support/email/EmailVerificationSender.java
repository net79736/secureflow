package com.tdd.secureflow.domain.support.email;

import com.tdd.secureflow.domain.mail.model.EmailVerificationCode;
import com.tdd.secureflow.domain.support.error.CoreException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.tdd.secureflow.domain.support.error.CoreException.createErrorJson;
import static com.tdd.secureflow.domain.support.error.ErrorType.Email.*;
import static java.time.LocalDateTime.now;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationSender {
    @Value("${SENDER_EMAIL}")
    private String senderEmail;
    private final JavaMailSender javaMailSender;
    private static final int EXPIRATION_MINUTES = 5; // 유효 시간 (5분)
    private final Map<String, EmailVerificationCode> codeStorage = new ConcurrentHashMap<>();

    // 메일을 보낸다
    public void send(String email) {
        log.info("Sending email to {}", email);

        try {
            // 인증 코드 생성
            EmailVerificationCode certificationCode = createNumber();
            codeStorage.put(email, certificationCode); // 이메일별로 인증 코드 저장

            // 이메일 생성 및 발송
            MimeMessage message = createMail(email, certificationCode.getCode());
            javaMailSender.send(message);
            log.info("certificationCode ExpirationTime : {}", certificationCode.getExpirationTime());
        } catch (MailException e) {
            log.error("이메일 전송 중 에러가 발생하였습니다." + e.getMessage());
            throw new CoreException(EMAIL_SEND_FAILED, e.getMessage());
        }
    }

    // 인증번호를 생성한다.
    private EmailVerificationCode createNumber() {
        int code = (int) (Math.random() * 900000) + 100000; // 6자리 인증 코드
        LocalDateTime expirationTime = now().plusMinutes(EXPIRATION_MINUTES);
        return new EmailVerificationCode(String.valueOf(code), expirationTime);
    }

    // 메일 내용을 생성한다.
    private MimeMessage createMail(String email, String code) {
        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            message.setFrom(senderEmail);
            message.setRecipients(MimeMessage.RecipientType.TO, email);
            message.setSubject("이메일 인증");

            // 이메일 본문
            String body = buildEmailContent(code, codeStorage.get(email).getExpirationTime());
            message.setText(body, "UTF-8", "html");
        } catch (MessagingException e) {
            log.error("이메일 생성 중 에러가 발생하였습니다. 에러 메시지 : {}", e.getMessage());
            throw new CoreException(EMAIL_SEND_FAILED, e.getMessage());
        }

        return message;
    }

    // 인증번호가 유효한지 검사한다.
    public void validateVerificationCode(String email, String inputCode) {
        EmailVerificationCode storedCode = codeStorage.get(email);

        if (storedCode == null || now().isAfter(storedCode.getExpirationTime())) {
            // return false; // 코드가 없거나 만료된 경우
            throw new CoreException(EMAIL_CODE_NOT_FOUND, createErrorJson("verificationCode", EMAIL_CODE_NOT_FOUND.getMessage()));
        }

        log.debug("stored code {}", storedCode.getCode());
        log.debug("inputCode {}", inputCode);
        if (!storedCode.getCode().equals(inputCode)) {
            throw new CoreException(EMAIL_CODE_MISMATCH, createErrorJson("verificationCode", EMAIL_CODE_MISMATCH.getMessage()));
        }
    }

    // 메일 내용을 작성한다.
    public String buildEmailContent(String code, LocalDateTime expirationTime) {
        String expirationTimeStr = expirationTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 이메일 본문 생성
        String body = "<h3>요청하신 인증 번호입니다.</h3>";
        body += "<h1>" + code + "</h1>";
        body += "<h3>감사합니다.</h3>";
        body += "<p style=\"color: #555555; font-size: 12px; text-align: left; margin-top: 20px;\">";
        body += "인증 코드는 유효 기간은 5분이며 " + expirationTimeStr + "까지 유효합니다.";
        body += "</p>";
        return body;
    }

    // 만료된 코드 제거
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void clearExpiredCodes() {
        log.debug("만료된 코드 제거 로직 START");

        LocalDateTime now = now();
        codeStorage.entrySet().removeIf(entry -> now.isAfter(entry.getValue().getExpirationTime()));

        log.debug("만료된 코드 제거 로직 END");
    }

    // codeStorage 내용 출력
    public void print() {
        log.debug("codeStorage 내용 출력");
        log.debug(codeStorage.toString());
    }

}
