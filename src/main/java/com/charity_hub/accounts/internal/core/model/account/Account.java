package com.charity_hub.accounts.internal.core.model.account;

import com.charity_hub.accounts.internal.core.contracts.IJWTGenerator;
import com.charity_hub.accounts.internal.core.events.*;
import com.charity_hub.accounts.internal.core.model.device.Device;
import com.charity_hub.accounts.internal.core.model.device.DeviceId;
import com.charity_hub.shared.domain.model.AggregateRoot;
import com.charity_hub.shared.domain.model.Pair;
import com.charity_hub.shared.domain.model.Permission;
import com.charity_hub.shared.exceptions.NotFoundException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Getter
public class Account extends AggregateRoot<AccountId> {
    // Private fields with getters
    private final MobileNumber mobileNumber;
    private final List<Permission> permissions;
    private FullName fullName;
    private PhotoUrl photoUrl;
    private final Date joinedDate;
    private final List<Device> devices;
    private boolean blocked;
    private final Connection connections;

    public Account(
            AccountId id,
            MobileNumber mobileNumber,
            List<Device> devices,
            List<Permission> permissions,
            FullName fullName,
            PhotoUrl photoUrl,
            boolean blocked,
            Date joinedDate, Connection connections
    ) {
        super(id);
        this.mobileNumber = mobileNumber;
        this.devices = new ArrayList<>(devices);
        this.permissions = permissions != null ? new ArrayList<>(permissions) :
                new ArrayList<>(Collections.singletonList(Permission.VIEW));
        this.fullName = fullName;
        this.photoUrl = photoUrl;
        this.blocked = blocked;
        this.joinedDate = joinedDate != null ? joinedDate : new Date();
        this.connections = connections != null ? connections : new Connection(null, null); 
    }

    public static Account newAccount(
            AccountId id,
            String aMobileNumber,
            String deviceId,
            String deviceType,
            boolean isAdmin,
            Connection connections
    ) {
        MobileNumber mobileNumber = MobileNumber.create(aMobileNumber);
        Device device = Device.createNew(deviceId, deviceType);
        Permission userPermission = isAdmin ? Permission.FULL_ACCESS : Permission.VIEW;

        Account account = new Account(
                id,
                mobileNumber,
                new ArrayList<>(Collections.singletonList(device)),
                new ArrayList<>(Collections.singletonList(userPermission)),
                null,
                null,
                false,
                new Date(),
                connections
        );

        account.raiseEvent(AccountCreated.from(account));
        return account;
    }

    public Pair<String, String> authenticate(String deviceId, String deviceType, IJWTGenerator jwtGenerator) {
        Device usedDevice = getDevice(deviceId);
        if (usedDevice == null) {
            usedDevice = Device.createNew(deviceId, deviceType);
        }

        addDevice(usedDevice);

        String refreshToken = jwtGenerator.generateRefreshToken(this, usedDevice);
        usedDevice.updateRefreshToken(refreshToken);
        raiseEvent(AccountAuthenticated.from(this, usedDevice));

        return new Pair<>(jwtGenerator.generateAccessToken(this, usedDevice), refreshToken);
    }

    public String refreshAccessToken(String deviceId, String refreshToken, IJWTGenerator jwtGenerator) {
        Device usedDevice = getDeviceOrThrow(deviceId);
        usedDevice.validateRefreshToken(refreshToken);
        return jwtGenerator.generateAccessToken(this, usedDevice);
    }

    public void registerFCMToken(String deviceId, String fcmToken) {
        Device usedDevice = getDeviceOrThrow(deviceId);
        usedDevice.updateFCMToken(fcmToken);
        raiseEvent(FCMTokenUpdated.from(usedDevice));
    }

    public void addPermission(String aPermission) {
        Permission permission = Permission.fromString(aPermission);
        this.permissions.add(permission);
        raiseEvent(PermissionGranted.from(this));
    }

    public void removePermission(String aPermission) {
        Permission permission = Permission.fromString(aPermission);
        this.permissions.remove(permission);
        raiseEvent(PermissionRemoved.from(this));
    }

    public void block() {
        this.blocked = true;
        raiseEvent(new AccountBlocked(getId()));
    }

    public void unBlock() {
        this.blocked = false;
        raiseEvent(new AccountUnBlocked(getId()));
    }

    public String updateBasicInfo(
            String deviceId,
            String fullName,
            String photoUrl,
            IJWTGenerator jwtGenerator
    ) {
        Device usedDevice = getDeviceOrThrow(deviceId);
        this.fullName = FullName.create(fullName);
        this.photoUrl = PhotoUrl.create(photoUrl);
        raiseEvent(BasicInfoUpdated.from(this, usedDevice));
        return jwtGenerator.generateAccessToken(this, usedDevice);
    }

    // Private helper methods
    private Account addDevice(Device device) {
        if (!devices.contains(device)) {
            devices.add(device);
        }
        return this;
    }

    private Device getDevice(String aDeviceId) {
        DeviceId deviceId = DeviceId.create(aDeviceId);
        return devices.stream()
                .filter(device -> device.getDeviceId().equals(deviceId))
                .findFirst()
                .orElse(null);
    }

    private Device getDeviceOrThrow(String deviceId) {
        Device device = getDevice(deviceId);
        if (device == null) {
            throw new NotFoundException("Device not found");
        }
        return device;
    }

    // Getters
    public List<Permission> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    public Date getJoinedDate() {
        return new Date(joinedDate.getTime());
    }

    public List<Device> getDevices() {
        return Collections.unmodifiableList(devices);
    }

}