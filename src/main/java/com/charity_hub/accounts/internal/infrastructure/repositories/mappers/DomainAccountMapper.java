package com.charity_hub.accounts.internal.infrastructure.repositories.mappers;

import com.charity_hub.accounts.internal.domain.model.account.*;
import com.charity_hub.accounts.internal.domain.model.device.Device;
import com.charity_hub.accounts.internal.infrastructure.db.AccountEntity;
import com.charity_hub.accounts.internal.infrastructure.db.DeviceEntity;
import com.charity_hub.shared.domain.model.Permission;
import org.springframework.stereotype.Component;

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
                                        entity.fullName() != null ? FullName.create(entity.fullName()) : null,
                                        entity.photoUrl() != null ? PhotoUrl.create(entity.photoUrl()) : null,
                                        entity.blocked(),
                                        new Date(entity.joinedDate()));
                } catch (Exception exc) {
                        throw new RuntimeException("Could not map identity from the database - " + exc.getMessage());
                }
        }

        private Device fromDeviceEntity(DeviceEntity entity) {
                return Device.create(
                                entity.deviceId(),
                                entity.deviceType(),
                                entity.refreshToken(),
                                entity.fcmToken(),
                                entity.lastAccessTime());
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
                                                .collect(Collectors.toList()));
        }

        private DeviceEntity toDeviceEntity(Device domain) {
                return new DeviceEntity(
                                domain.getDeviceId().value(),
                                domain.getDeviceType().value(),
                                domain.getRefreshToken() != null ? domain.getRefreshToken().getValue() : null,
                                domain.getFcmToken() != null ? domain.getFcmToken().getValue() : null,
                                domain.getLastAccessTime().getTime());
        }
}