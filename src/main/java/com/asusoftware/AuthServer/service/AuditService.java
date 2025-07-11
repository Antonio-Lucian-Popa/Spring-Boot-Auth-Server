package com.asusoftware.AuthServer.service;

import com.asusoftware.AuthServer.entity.AuditEventType;
import com.asusoftware.AuthServer.entity.AuthLog;
import com.asusoftware.AuthServer.repository.AuthLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuthLogRepository logRepository;

    public void logEvent(String email, AuditEventType type, boolean success, HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        AuthLog log = new AuthLog();
        log.setEmail(email);
        log.setEventType(type.name());
        log.setSuccess(success);
        log.setIpAddress(ip);
        log.setUserAgent(userAgent);
        logRepository.save(log);
    }
}

