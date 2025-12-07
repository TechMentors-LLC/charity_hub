package com.charity_hub.notifications.internal;

import com.charity_hub.notifications.NotificationApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Stub implementation of NotificationApi for test mode.
 * This is used when firebase.test-mode=true to avoid requiring
 * real Firebase credentials during development/testing.
 */
@Component
@ConditionalOnProperty(
        name = "firebase.test-mode",
        havingValue = "true"
)
public class FCMServiceStub implements NotificationApi {
    private static final Logger logger = LoggerFactory.getLogger(FCMServiceStub.class);

    public FCMServiceStub() {
        logger.info("FCMServiceStub initialized - Firebase notifications are disabled in test mode");
    }

    @Override
    public void notifyDevices(List<String> tokens, String title, String body) {
        logger.debug("[TEST MODE] Would send notification to {} devices - Title: {}, Body: {}", 
                tokens.size(), title, body);
    }

    @Override
    public void notifyTopicSubscribers(String topic, String event, Object extraJsonData, String title, String body) {
        logger.debug("[TEST MODE] Would send topic notification - Topic: {}, Event: {}, Title: {}", 
                topic, event, title);
    }

    @Override
    public void subscribeToTopic(String topic, List<String> tokens) {
        logger.debug("[TEST MODE] Would subscribe {} tokens to topic: {}", tokens.size(), topic);
    }
}
