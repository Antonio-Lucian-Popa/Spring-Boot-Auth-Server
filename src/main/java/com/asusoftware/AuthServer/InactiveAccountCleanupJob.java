package com.asusoftware.AuthServer;

import com.asusoftware.AuthServer.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class InactiveAccountCleanupJob {

    private final UserRepository userRepository;

    // Rulează la fiecare oră
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void deleteUnverifiedAccountsOlderThan24Hours() {
        Instant cutoff = Instant.now().minusSeconds(24 * 60 * 60);
        int deleted = userRepository.deleteByEnabledFalseAndCreatedAtBefore(Date.from(cutoff));
        if (deleted > 0) {
            log.info("Deleted {} unverified accounts older than 24h", deleted);
        }
    }
}