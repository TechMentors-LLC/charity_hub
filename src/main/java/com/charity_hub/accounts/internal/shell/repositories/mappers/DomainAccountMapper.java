package com.charity_hub.accounts.internal.shell.repositories.mappers;

import com.charity_hub.accounts.internal.core.model.account.*;
import com.charity_hub.accounts.internal.core.model.device.Device;
import com.charity_hub.accounts.internal.shell.db.AccountEntity;
import com.charity_hub.accounts.internal.shell.db.ConnectionEntity;
import com.charity_hub.accounts.internal.shell.db.DeviceEntity;
import com.charity_hub.shared.domain.model.Permission;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class DomainAccountMapper {

    public Account toDomain(AccountEntity entity) {
        try {
            return new Account(
                    new AccountId(UUID.fromString(entity.accountId())),
                    MobileNumber.create(entity.mobileNumber()),
                    entity.devices().stream()
                            .map(this::fromDeviceEntity)
                            .collect(Collectors.toList()),
                    entity.permissions().stream()
                            .map(Permission::fromString)
                            .collect(Collectors.toList()),
                    entity.fullName() != null ?
                            FullName.create(entity.fullName()) : null,
                    entity.photoUrl() != null ?
                            PhotoUrl.create(entity.photoUrl()) : null,
                    entity.blocked(),
                    new Date(entity.joinedDate()),
                    entity.connections() != null ?
                            fromConnectionEntity(entity.connections()) : new Connection(null, null)
            );
        } catch (Exception exc) {
            throw new RuntimeException("Could not map identity from the database - " + exc.getMessage());
        }
    }

    private Connection fromConnectionEntity(ConnectionEntity connections) {
        return new Connection(
                connections.parent() != null ? new AccountId(UUID.fromString(connections.parent())) : null,
                connections.children() != null
                        ? connections.children().stream()
                        .map(id -> new AccountId(UUID.fromString(id)))
                        .collect(Collectors.toList())
                        : new ArrayList<>()
        );
    }

    private Device fromDeviceEntity(DeviceEntity entity) {
        return Device.create(
                entity.deviceId(),
                entity.deviceType(),
                entity.refreshToken(),
                entity.fcmToken(),
                entity.lastAccessTime()
        );
    }

    public AccountEntity toDB(Account domain) {
        return new AccountEntity(
                domain.getId().value().toString(),
                domain.getMobileNumber().value(),
                domain.getFullName() != null ? domain.getFullName().value() : null,
                domain.getPhotoUrl() != null ? domain.getPhotoUrl().value() : null,
                domain.isBlocked(),
                domain.getJoinedDate().getTime(),
                new Date().getTime(),
                domain.getPermissions().stream()
                        .map(Permission::name)
                        .collect(Collectors.toList()),
                domain.getDevices().stream()
                        .map(this::toDeviceEntity)
                        .collect(Collectors.toList()),
                toConnectionEntity(domain.getConnections())

        );
    }

    private DeviceEntity toDeviceEntity(Device domain) {
        return new DeviceEntity(
                domain.getDeviceId().value(),
                domain.getDeviceType().value(),
                domain.getRefreshToken() != null ? domain.getRefreshToken().getValue() : null,
                domain.getFcmToken() != null ? domain.getFcmToken().getValue() : null,
                domain.getLastAccessTime().getTime()
        );
    }

    private ConnectionEntity toConnectionEntity(Connection domain){
        return new ConnectionEntity(
                domain.getParent() != null ? domain.getParent().value().toString() : null,
                domain.getChildren().stream().map(childId -> childId.value().toString())
                        .collect(Collectors.toList())
        );
    }
}