package com.exotech.kindmap.service;

import com.exotech.kindmap.dto.PinDTO;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.TopicManagementResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FCMService {

    private static final Logger log = LoggerFactory.getLogger(FCMService.class);
    private final FirebaseMessaging firebaseMessaging;

    public FCMService(FirebaseMessaging firebaseMessaging) {
        this.firebaseMessaging = firebaseMessaging;
    }

    /**
     * Subscribe a device token to a topic
     */
    public boolean subscribeToTopic(String token, String topic) {
        if (token == null || token.isEmpty()) {
            log.warn("Cannot subscribe - token is null or empty");
            return false;
        }

        try {
            TopicManagementResponse response = firebaseMessaging.subscribeToTopic(
                    List.of(token), sanitizeTopic(topic));

            log.info("✅ Subscribed token {} to topic {}. Success count: {}",
                    maskToken(token), topic, response.getSuccessCount());

            return response.getSuccessCount() > 0;
        } catch (FirebaseMessagingException e) {
            log.error("❌ Failed to subscribe token {} to topic {}: {}",
                    maskToken(token), topic, e.getMessage());

            // Handle specific error codes
            if ("UNREGISTERED".equals(e.getErrorCode())) {
                log.warn("Token {} is no longer valid - should be removed", maskToken(token));
                // TODO: Trigger token cleanup
            }
            return false;
        }
    }

    /**
     * Unsubscribe a device token from a topic
     */
    public boolean unsubscribeFromTopic(String token, String topic) {
        if (token == null || token.isEmpty()) {
            log.warn("Cannot unsubscribe - token is null or empty");
            return false;
        }

        try {
            TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(
                    List.of(token), sanitizeTopic(topic));

            log.info("✅ Unsubscribed token {} from topic {}. Success count: {}",
                    maskToken(token), topic, response.getSuccessCount());

            return response.getSuccessCount() > 0;
        } catch (FirebaseMessagingException e) {
            log.error("❌ Failed to unsubscribe token {} from topic {}: {}",
                    maskToken(token), topic, e.getMessage());
            return false;
        }
    }

    /**
     * Send notification about new pin to grid topic
     */
    public void sendNewPinNotification(PinDTO pinDTO) {
        String topic = sanitizeTopic(pinDTO.getGridId());

        // ✅ FIXED: Exactly as requested
        String title = "Someone nearby needs help";

        // Build the body based on available content
        String body = buildNotificationBody(pinDTO);

        // Prepare data payload for additional context
        Map<String, String> data = new HashMap<>();
        data.put("pinId", pinDTO.getPinId());
        data.put("gridId", pinDTO.getGridId());
        data.put("type", "new_pin");
        data.put("latitude", String.valueOf(pinDTO.getLatitude()));
        data.put("longitude", String.valueOf(pinDTO.getLongitude()));
        data.put("createdBy", pinDTO.getCreatedBy());
        data.put("createdAt", pinDTO.getCreatedAt().toString());

        // Add content flags
        data.put("hasDetails", String.valueOf(pinDTO.getDetails() != null && !pinDTO.getDetails().isEmpty()));
        data.put("hasNote", String.valueOf(pinDTO.getNote() != null && !pinDTO.getNote().isEmpty()));

        if (pinDTO.getDetails() != null) {
            data.put("details", pinDTO.getDetails());
        }
        if (pinDTO.getNote() != null) {
            data.put("note", pinDTO.getNote());
        }

        Message message = Message.builder()
                .setTopic(topic)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putAllData(data)
                .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("✅ New pin notification sent to topic {}: {}", topic, response);
            log.debug("Notification body: {}", body);
        } catch (FirebaseMessagingException e) {
            log.error("❌ Failed to send notification to topic {}: {}", topic, e.getMessage());
        }
    }

    /**
     * Build notification body based on pin content
     */
    private String buildNotificationBody(PinDTO pinDTO) {
        boolean hasDetails = pinDTO.getDetails() != null && !pinDTO.getDetails().isEmpty();
        boolean hasNote = pinDTO.getNote() != null && !pinDTO.getNote().isEmpty();

        // ✅ CASE 1: Both details and note provided
        if (hasDetails && hasNote) {
            return String.format("📝 Note: %s\n📍 Details: %s",
                    truncate(pinDTO.getNote(), 50),
                    truncate(pinDTO.getDetails(), 50));
        }

        // ✅ CASE 2: Only details provided
        if (hasDetails && !hasNote) {
            return String.format("📍 Details: %s",
                    truncate(pinDTO.getDetails(), 100));
        }

        // ✅ CASE 3: Only note provided
        if (!hasDetails && hasNote) {
            return String.format("📝 Note: %s",
                    truncate(pinDTO.getNote(), 100));
        }

        // ✅ CASE 4: No details or note - FALLBACK MESSAGE
        String[] fallbackMessages = {
                "Someone in your area needs assistance. Can you help? ❤️",
                "A neighbor nearby could use your help right now 🤝",
                "Help needed in your community! 🏘️",
                "Someone nearby is reaching out for help 🆘",
                "Your help could make a difference today ✨",
                "A fellow community member needs you 🙏",
                "Be a hero - someone nearby needs help 🦸",
                "Community alert: Assistance requested near you 📢"
        };

        // Use pinId hash to randomly select a message (consistent for same pin)
        int index = Math.abs(pinDTO.getPinId().hashCode() % fallbackMessages.length);
        return fallbackMessages[index];
    }

    /**
     * Truncate text to max length with ellipsis
     */
    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Sanitize topic name for Firebase (only a-zA-Z0-9-_.~%)
     */
    private String sanitizeTopic(String topic) {
        if (topic == null) return "default";
        // Replace invalid characters with underscore
        return topic.replaceAll("[^a-zA-Z0-9-_.~%]", "_");
    }

    /**
     * Mask token for logging (show first 4 and last 4 chars)
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) return "***";
        return token.substring(0, 4) + "..." + token.substring(token.length() - 4);
    }
}