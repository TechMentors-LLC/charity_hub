package com.charity_hub.notifications.internal;

import com.charity_hub.notifications.NotificationApi;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "firebase.test-mode", havingValue = "false", matchIfMissing = true)
public class FCMService implements NotificationApi {
    private static final Logger logger = LoggerFactory.getLogger(FCMService.class);
    private final ObjectMapper objectMapper;

    public FCMService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void notifyDevices(List<String> tokens, String title, String body) {
        logger.debug("Sending notification to {} devices - Title: {}", tokens.size(), title);
        int successCount = 0;
        int failureCount = 0;
        
        for (String token : tokens) {
            Message message = buildMessage(title, body)
                    .setToken(token)
                    .build();
            try {
                String messageId = FirebaseMessaging.getInstance().send(message);
                logger.debug("Notification sent successfully to token: {} - MessageId: {}", token.substring(0, Math.min(10, token.length())) + "...", messageId);
                successCount++;
            } catch (FirebaseMessagingException e) {
                logger.error("Failed to send notification to token: {} - Error: {}", token.substring(0, Math.min(10, token.length())) + "...", e.getMessage());
                failureCount++;
                throw new RuntimeException(e);
            }
        }
        logger.info("Successfully sent notifications to {} devices (success: {}, failed: {})", tokens.size(), successCount, failureCount);
    }

    @Override
    public void notifyTopicSubscribers(String topic, String event, Object extraJsonData, String title, String body) {
        logger.debug("Sending topic notification - Topic: {}, Event: {}, Title: {}", topic, event, title);
        Message message;
        String payload;
        try {
            payload = objectMapper.writeValueAsString(extraJsonData);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize notification payload, using empty object: {}", e.getMessage());
            payload = "{}";
        }
        message = buildMessage(title, body)
                .setTopic(topic)
                .putData("topic", topic)
                .putData("event", event)
                .putData("data", payload)
                .build();


        try {
            String messageId = FirebaseMessaging.getInstance().send(message);
            logger.info("Topic notification sent successfully - Topic: {}, Event: {}, MessageId: {}", topic, event, messageId);
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to send topic notification - Topic: {}, Event: {}, Error: {}", topic, event, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Subscribe tokens to a topic.
     *
     * @param topic  The topic to subscribe to
     * @param tokens The list of tokens to subscribe
     */
    @Override
    public void subscribeToTopic(String topic, List<String> tokens) {
        logger.debug("Subscribing {} tokens to topic: {}", tokens.size(), topic);
        TopicManagementResponse response;
        try {
            response = FirebaseMessaging.getInstance().subscribeToTopic(tokens, topic);
        } catch (FirebaseMessagingException e) {
            logger.error("Failed to subscribe tokens to topic: {} - Error: {}", topic, e.getMessage());
            throw new RuntimeException(e);
        }
        if (response.getFailureCount() > 0) {
            logger.warn("Topic subscription partially failed - Topic: {}, Success: {}, Failed: {}", 
                    topic, response.getSuccessCount(), response.getFailureCount());
        } else {
            logger.info("{} tokens were subscribed successfully to topic: {}", response.getSuccessCount(), topic);
        }
    }

    private Message.Builder buildMessage(String title, String body) {
        Message.Builder builder = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());
        androidConfig(builder);
        apnsConfig(builder);
        return builder;
    }

    private void apnsConfig(Message.Builder builder) {
        builder.setApnsConfig(
                ApnsConfig.builder()
                        .setAps(Aps.builder().build())
                        .build()
        );
    }

    private void androidConfig(Message.Builder builder) {
        builder.setAndroidConfig(
                AndroidConfig.builder()
                        .setTtl(3600 * 1000L)
                        .setNotification(AndroidNotification.builder()
                                .setIcon("stock_ticker_update")
                                .setColor("#f45342")
                                .build()
                        ).build()
        );
    }
}