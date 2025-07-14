package com.charity_hub.accounts.internal.shell.api.dtos;

import java.util.Date;
import java.util.List;

public record AccountResponseDTO(
    String id,
    String mobileNumber,
    String fullName,
    String photoUrl,
    Date joinedDate,
    boolean blocked,
    List<String> permissions,
    List<DeviceResponseDTO> devices
) {
    public record DeviceResponseDTO(
        String deviceId,
        String deviceType,
        String fcmToken,
        long lastAccessTime
    ) {}
}