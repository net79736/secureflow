package com.tdd.secureflow.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 이메일 설정
 * 
 * JavaMailSender를 설정하여 이메일 전송 기능을 활성화합니다.
 */
@Configuration
public class MailConfig {
    
    @Value("${spring.mail.host:smtp.naver.com}")
    private String host;
    
    @Value("${spring.mail.port:587}")
    private int port;
    
    @Value("${spring.mail.username:}")
    private String username;
    
    @Value("${spring.mail.password:}")
    private String password;

    @Value("${spring.mail.from:testaccount@naver.com}")
    private String from;
    
    @Value("${spring.mail.properties.mail.smtp.auth:true}")
    private boolean auth; // 인증 여부
    
    @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}")
    private boolean starttlsEnable; // TLS 사용 여부
    
    @Value("${spring.mail.properties.mail.smtp.starttls.required:true}")
    private boolean starttlsRequired; // TLS 필수 여부
    
    @Value("${spring.mail.properties.mail.smtp.connectiontimeout:5000}")
    private int connectionTimeout; // 연결 시간 초과 시간
    
    @Value("${spring.mail.properties.mail.smtp.timeout:5000}")
    private int timeout; // 읽기 시간 초과 시간
    
    @Value("${spring.mail.properties.mail.smtp.writetimeout:5000}")
    private int writeTimeout; // 쓰기 시간 초과 시간

    /**
     * JavaMailSender 빈 설정
     * @return JavaMailSender
     * @throws Exception
     */
    @Bean
    public JavaMailSender javaMailSender() throws Exception {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setDefaultEncoding("UTF-8");
        
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", starttlsEnable);
        props.put("mail.smtp.starttls.required", starttlsRequired);
        props.put("mail.smtp.connectiontimeout", connectionTimeout);
        props.put("mail.smtp.timeout", timeout);
        props.put("mail.smtp.writetimeout", writeTimeout);

        return mailSender;
    }
}
