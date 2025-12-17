package com.charity_hub.notifications.internal;

import com.charity_hub.notifications.shared.INotificationsAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Stub implementation of INotificationsAPI for test mode.
 * Loads when app.firebase.enabled=false
 */
@Service
@Primary
@ConditionalOnProperty(name = "app.firebase.enabled", havingValue = "false", matchIfMissing = true)
public class FCMServiceStub implements INotificationsAPI {
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
