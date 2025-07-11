package com.asusoftware.AuthServer.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Retryable(
            value = { MessagingException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 3000)
    )
    public void sendVerificationEmail(String to, String token, String name) throws MessagingException {
        String verificationUrl = "http://localhost:8080/api/auth/verify?token=" + token;

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("verificationLink", verificationUrl);
        context.setVariable("email", to);

        String htmlContent = templateEngine.process("email/verification-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Please verify your email");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
}

