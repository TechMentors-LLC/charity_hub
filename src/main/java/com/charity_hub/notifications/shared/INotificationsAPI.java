package com.charity_hub.notifications.shared;

import java.util.List;

public interface INotificationsAPI {
    void notifyDevices(List<String> tokens, String title, String body);

    void notifyTopicSubscribers(String topic, String event, Object extraJsonData, String title, String body);

    void subscribeToTopic(String topic, List<String> tokens);
}
