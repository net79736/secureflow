package com.tdd.secureflow.domain.support.email;

import static com.tdd.secureflow.domain.support.error.CoreException.createErrorJson;
import static com.tdd.secureflow.domain.support.error.ErrorType.Email.EMAIL_CODE_MISMATCH;
import static com.tdd.secureflow.domain.support.error.ErrorType.Email.EMAIL_CODE_NOT_FOUND;
import static com.tdd.secureflow.domain.support.error.ErrorType.Email.EMAIL_SEND_FAILED;
import static java.time.LocalDateTime.now;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.tdd.secureflow.domain.mail.model.EmailVerificationCode;
import com.tdd.secureflow.domain.support.error.CoreException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 이메일 인증번호 발송·검증을 담당합니다.
 * {@code src/main/resources/templates/mail/email-verification.html} 을 Thymeleaf로 렌더링한 HTML을 사용합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationSender {

    // Thymeleaf 템플릿 이름 — 실제 파일은 classpath:templates/mail/email-verification.html
    private static final String MAIL_TEMPLATE_VERIFICATION = "mail/email-verification";
    // 수신자 메일함에 보이는 제목
    private static final String MAIL_SUBJECT = "Secureflow 이메일 인증";
    // 템플릿에 넘기는 만료 시각 문자열 포맷
    private static final DateTimeFormatter EXPIRATION_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 발신 주소
    @Value("${spring.mail.from}")
    private String senderEmail;

    // MailConfig 자동설정으로 주입
    private final JavaMailSender javaMailSender;
    // HTML 템플릿 엔진
    private final SpringTemplateEngine templateEngine;

    // 인증번호 유효 시간(분)
    private static final int EXPIRATION_MINUTES = 1;
    // 이메일 주소 → (코드 + 만료시각). 동시 요청이 있을 수 있어 ConcurrentHashMap 사용
    private final Map<String, EmailVerificationCode> codeStorage = new ConcurrentHashMap<>();

    /**
     * 인증번호를 만들고 저장한 뒤, Thymeleaf로 HTML을 만든 다음 SMTP로 발송합니다.
     *
     * @param email 수신자 이메일
     */
    public void send(String email) {
        // [디버그] 브레이크포인트 ⑥ — ⑤와 같은 스레드(실제 발송·Thymeleaf·SMTP)
        log.info("[mail-async-flow] ⑥ EmailVerificationSender.send | thread={} | to={}", Thread.currentThread().getName(), email);
        log.info("Sending email to {}", email);

        try {
            // 1) 6자리 코드 생성 + 만료 시각 부여
            EmailVerificationCode certificationCode = createNumber();
            codeStorage.put(email, certificationCode); // 이메일별로 인증 코드 저장

            // 2) templates/mail/email-verification.html 에 code, email, expirationTime 주입 → HTML 문자열
            String html = renderVerificationHtml(email, certificationCode);
            // 3) MIME HTML 메시지 구성 후 전송
            MimeMessage message = createMail(email, html);
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

    /**
     * Thymeleaf 템플릿을 처리해 완성된 HTML 문자열을 반환합니다.
     * <p>
     * 템플릿 안에서는 {@code th:text="${code}"} 처럼 아래 변수명을 사용합니다.
     * </p>
     */
    private String renderVerificationHtml(String email, EmailVerificationCode certificationCode) {
        Context context = new Context();
        context.setVariable("code", certificationCode.getCode());
        context.setVariable("email", email);
        context.setVariable("expirationTime", certificationCode.getExpirationTime().format(EXPIRATION_FORMAT));
        return templateEngine.process(MAIL_TEMPLATE_VERIFICATION, context);
    }

    /**
     * HTML 본문을 UTF-8 MIME 메일로 감쌉니다.
     * {@link MimeMessageHelper#MimeMessageHelper(jakarta.mail.internet.MimeMessage, boolean, String)} 의
     * 두 번째 인자 {@code true} 는 multipart 허용(첨부·inline 이미지 등 확장 시 필요).
     * 네 번째 인자 {@code setText(htmlBody, true)} 의 {@code true} 가 “본문이 HTML” 이라는 뜻입니다.
     */
    private MimeMessage createMail(String email, String htmlBody) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(senderEmail);
            helper.setTo(email);
            helper.setSubject(MAIL_SUBJECT); // 제목
            helper.setText(htmlBody, true);
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
            throw new CoreException(EMAIL_CODE_NOT_FOUND, createErrorJson("verificationCode", EMAIL_CODE_NOT_FOUND.getMessage()));
        }

        log.debug("stored code {}", storedCode.getCode());
        log.debug("inputCode {}", inputCode);
        if (!storedCode.getCode().equals(inputCode)) {
            throw new CoreException(EMAIL_CODE_MISMATCH, createErrorJson("verificationCode", EMAIL_CODE_MISMATCH.getMessage()));
        }
    }

    // 만료된 코드를 제거
    @Scheduled(fixedRate = 60000)
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
