package com.charity_hub.accounts.internal.application.queries;

import java.util.List;

public record Account(String uuid, String fullName, String photoUrl, List<String> permissions) {
    public Account(String uuid, String fullName, String photoUrl, List<String> permissions) {
        this.uuid = uuid;
        this.fullName = fullName;
        this.photoUrl = photoUrl;
        this.permissions = List.copyOf(permissions); // Create immutable copy
    }
}
