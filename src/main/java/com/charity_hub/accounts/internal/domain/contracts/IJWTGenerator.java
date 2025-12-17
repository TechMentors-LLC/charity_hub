package com.charity_hub.accounts.internal.domain.contracts;

import com.charity_hub.accounts.internal.domain.model.account.Account;
import com.charity_hub.accounts.internal.domain.model.device.Device;

public interface IJWTGenerator {
    String generateAccessToken(Account account, Device device);

    String generateRefreshToken(Account account, Device device);
}