package com.charity_hub.accounts.internal.domain.model.device;

import com.charity_hub.shared.domain.extension.ValueValidator;
import com.charity_hub.shared.domain.model.ValueObject;

public record DeviceId(String value) implements ValueObject {
    private static final int MIN_LENGTH = 15;
    private static final int MAX_LENGTH = 50;

    public DeviceId {
        ValueValidator.assertWithinRange(getClass(), value, MIN_LENGTH, MAX_LENGTH);
    }

    public static DeviceId create(String value) {
        return new DeviceId(value);
    }

}