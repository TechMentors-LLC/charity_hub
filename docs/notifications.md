# Notifications APIs

## 1. Internal Notification Service

> **Note:**
> The notifications module currently exposes only internal service methods, not public HTTP endpoints.

### Methods

#### notifyDevices
- **Description:** Sends a notification to a list of device tokens.
- **Parameters:**
    - `tokens` (List<String>): Device tokens to notify.
    - `title` (String): Notification title.
    - `body` (String): Notification body.

#### notifyTopicSubscribers
- **Description:** Sends a notification to all subscribers of a topic.
- **Parameters:**
    - `topic` (String): Topic name.
    - `event` (String): Event type.
    - `extraJsonData` (Object): Additional data.
    - `title` (String): Notification title.
    - `body` (String): Notification body.

#### subscribeToTopic
- **Description:** Subscribes a list of device tokens to a topic.
- **Parameters:**
    - `topic` (String): Topic name.
    - `tokens` (List<String>): Device tokens to subscribe. 