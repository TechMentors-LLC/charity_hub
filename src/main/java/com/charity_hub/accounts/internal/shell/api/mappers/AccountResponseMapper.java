package com.charity_hub.accounts.internal.shell.api.mappers;

import com.charity_hub.accounts.internal.core.model.account.Account;
import com.charity_hub.accounts.internal.shell.api.dtos.AccountResponseDTO;
import com.charity_hub.shared.domain.model.Permission;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class AccountResponseMapper {
    
    public AccountResponseDTO toDTO(Account account) {
        if (account == null) return null;

        return new AccountResponseDTO(
            account.getId() != null ? account.getId().value().toString() : null,
            account.getMobileNumber() != null ? account.getMobileNumber().value() : null,
            account.getFullName() != null ? account.getFullName().value() : null,
            account.getPhotoUrl() != null ? account.getPhotoUrl().value() : null,
            account.getJoinedDate(),
            account.isBlocked(),
            account.getPermissions().stream()
                .map(Permission::toString)
                .collect(Collectors.toList()),
            account.getDevices().stream()
                .map(device -> new AccountResponseDTO.DeviceResponseDTO(
                    device.getDeviceId() != null ? device.getDeviceId().value() : null,
                    device.getDeviceType() != null ? device.getDeviceType().value() : null,
                    device.getFcmToken() != null ? device.getFcmToken().getValue() : null,
                    device.getLastAccessTime() != null ? device.getLastAccessTime().getTime() : 0L
                ))
                .collect(Collectors.toList())
        );
    }
}