package com.exotech.kindmap.service;

import com.exotech.kindmap.repository.PinRepo;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.*;

@Component
public class PinDeletionScheduler {

    private static final Logger log = LoggerFactory.getLogger(PinDeletionScheduler.class);

    @Autowired
    private PinRepo pinRepo;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    public void schedulePinDeletion(String pinId, LocalDateTime createdAt, int delayHours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deletionTime = createdAt.plusHours(delayHours);

        long delayMillis = ChronoUnit.MILLIS.between(now, deletionTime);

        if (delayMillis <= 0) {
            log.info("⚠️ Pin {} should have been deleted at {}. Will delete after transaction commits.",
                    pinId, deletionTime);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        deletePinIfExists(pinId);
                    }
                });
            } else {
                deletePinIfExists(pinId);
            }
            return;
        }

        log.info("⏰ Scheduling deletion for pin {} at {} (in {} Hours from creation)",
                pinId, deletionTime, delayHours);

        scheduler.schedule(() -> deletePinIfExists(pinId), delayMillis, TimeUnit.MILLISECONDS);
    }

    @Transactional
    public void deletePinIfExists(String pinId) {
        try {
            if (pinRepo.existsById(pinId)) {
                pinRepo.deleteById(pinId);
                log.info("✅ Pin {} automatically deleted", pinId);
            } else {
                log.info("⏭️ Pin {} was already deleted, skipping", pinId);
            }
        } catch (DataIntegrityViolationException e) {
            log.error("❌ Data integrity violation while deleting pin {}: {}", pinId, e.getMessage());
        } catch (Exception e) {
            log.error("❌ Failed to delete pin {}: {}", pinId, e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        log.info("🔄 Shutting down PinDeletionScheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("✅ PinDeletionScheduler shut down");
    }
}