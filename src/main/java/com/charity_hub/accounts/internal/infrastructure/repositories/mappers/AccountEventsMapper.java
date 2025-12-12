package com.charity_hub.accounts.internal.infrastructure.repositories.mappers;

import com.charity_hub.accounts.shared.AccountEventDto;
import com.charity_hub.accounts.internal.domain.events.*;

import java.util.stream.Collectors;

public class AccountEventsMapper {
    
    private AccountEventsMapper() {
        // Private constructor to prevent instantiation
    }

    public static AccountEventDto map(AccountEvent event) {
        if (event instanceof AccountCreated) {
            AccountCreated e = (AccountCreated) event;
            return new AccountEventDto.AccountCreatedDTO(
                e.id().value(),
                e.mobileNumber().value()
            );
        }
        
        if (event instanceof AccountAuthenticated) {
            AccountAuthenticated e = (AccountAuthenticated) event;
            return new AccountEventDto.AccountAuthenticated(
                e.id().value(),
                e.deviceId().value(),
                e.deviceType().value(),
                e.deviceFCMToken() != null ? e.deviceFCMToken().getValue() : null
            );
        }
        
        if (event instanceof BasicInfoUpdated) {
            BasicInfoUpdated e = (BasicInfoUpdated) event;
            return new AccountEventDto.BasicInfoUpdatedDTO(
                e.id().value(),
                e.deviceId().value(),
                e.deviceType().value(),
                e.deviceFCMToken() != null ? e.deviceFCMToken().getValue() : null
            );
        }
        
        if (event instanceof FCMTokenUpdated) {
            FCMTokenUpdated e = (FCMTokenUpdated) event;
            return new AccountEventDto.FCMTokenUpdatedDTO(
                e.deviceId().value(),
                e.deviceType().value(),
                e.deviceFCMToken() != null ? e.deviceFCMToken().getValue() : null
            );
        }
        
        if (event instanceof PermissionGranted) {
            PermissionGranted e = (PermissionGranted) event;
            return new AccountEventDto.PermissionAddedDTO(
                e.id().value(),
                e.permissions().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList())
            );
        }
        
        if (event instanceof PermissionRemoved) {
            PermissionRemoved e = (PermissionRemoved) event;
            return new AccountEventDto.PermissionRemovedDTO(
                e.id().value(),
                e.permissions().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList())
            );
        }
        
        if (event instanceof AccountBlocked) {
            AccountBlocked e = (AccountBlocked) event;
            return new AccountEventDto.AccountBlocked(
                e.id().value()
            );
        }
        
        if (event instanceof AccountUnBlocked) {
            AccountUnBlocked e = (AccountUnBlocked) event;
            return new AccountEventDto.AccountUnBlockedDTO(
                e.getId().value()
            );
        }

        throw new IllegalArgumentException("Unknown event type");
    }
}