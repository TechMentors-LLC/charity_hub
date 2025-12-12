package com.charity_hub.accounts.internal.domain.events;

import com.charity_hub.shared.domain.model.DomainEvent;

public sealed interface AccountEvent extends DomainEvent permits AccountAuthenticated, AccountBlocked, AccountCreated, AccountUnBlocked, BasicInfoUpdated, FCMTokenUpdated, PermissionGranted, PermissionRemoved {
}