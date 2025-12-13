package com.charity_hub.accounts.shared;

import com.charity_hub.accounts.internal.infrastructure.db.AccountEntity;
import com.charity_hub.accounts.internal.infrastructure.db.DeviceEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DTOAccountMapper {

    public AccountDTO toDTO(AccountEntity entity) {
        return new AccountDTO(
                entity.accountId(),
                entity.mobileNumber(),
                entity.fullName() != null ? entity.fullName() : "بدون إسم",
                entity.photoUrl() != null ? entity.photoUrl() : "",
                entity.devices().stream().map(DeviceEntity::fcmToken).collect(Collectors.toList()));
    }
}