package com.charity_hub.accounts.internal.domain.model.device;

import com.charity_hub.shared.domain.model.Entity;
import com.charity_hub.shared.exceptions.UnAuthorized;
import lombok.Getter;

import java.util.Date;
import java.util.Objects;

@Getter
public class Device extends Entity<DeviceId> {
    // Getters

    private final DeviceId deviceId;
    private final DeviceType deviceType;
    private RefreshToken refreshToken;
    private FCMToken fcmToken;
    private Date lastAccessTime;

    private Device(
            DeviceId deviceId,
            DeviceType deviceType,
            Date lastAccessTime,
            RefreshToken refreshToken,
            FCMToken fcmToken
    ) {
        super(deviceId);
        this.deviceId = deviceId;
        this.deviceType = deviceType;
        this.lastAccessTime = lastAccessTime;
        this.refreshToken = refreshToken;
        this.fcmToken = fcmToken;
    }

    public static Device createNew(String aDeviceId, String aDeviceType, String aRefreshToken) {
        return create(aDeviceId, aDeviceType, aRefreshToken);
    }

    public static Device createNew(String aDeviceId, String aDeviceType) {
        return create(aDeviceId, aDeviceType, null);
    }

    public static Device create(
            String aDeviceId,
            String aDeviceType,
            String aRefreshToken,
            String aFCMToken,
            Long aLastAccessTime
    ) {
        DeviceType deviceType = DeviceType.create(aDeviceType);
        DeviceId deviceId = DeviceId.create(aDeviceId);

        RefreshToken refreshToken = aRefreshToken != null ? RefreshToken.create(aRefreshToken) : null;

        FCMToken fcmToken = aFCMToken != null ? FCMToken.create(aFCMToken) : null;

        Date lastAccessTime = aLastAccessTime != null ? new Date(aLastAccessTime) : new Date();

        return new Device(deviceId, deviceType, lastAccessTime, refreshToken, fcmToken);
    }

    public static Device create(String aDeviceId, String aDeviceType, String aRefreshToken) {
        return create(aDeviceId, aDeviceType, aRefreshToken, null, null);
    }

    public void validateRefreshToken(String aRefreshToken) {
        RefreshToken refreshToken = RefreshToken.create(aRefreshToken);

        if (!Objects.equals(this.refreshToken, refreshToken)) {
            throw new UnAuthorized();
        }
    }

    public void updateRefreshToken(String newRefreshToken) {
        this.refreshToken = RefreshToken.create(newRefreshToken);
        this.lastAccessTime = new Date();
    }

    public void updateFCMToken(String fcmToken) {
        this.fcmToken = FCMToken.create(fcmToken);
        this.lastAccessTime = new Date();
    }

    public Date getLastAccessTime() {
        return new Date(lastAccessTime.getTime());
    }
}