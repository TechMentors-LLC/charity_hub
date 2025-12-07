package com.charity_hub.accounts.internal.shell.services.jwt;

import com.charity_hub.accounts.internal.core.model.account.Account;
import com.charity_hub.accounts.internal.core.model.device.Device;
import com.charity_hub.shared.auth.AccessTokenPayload;
import com.charity_hub.shared.auth.JWTPayload;
import com.charity_hub.shared.auth.RefreshTokenPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TokenMapper {
    private static final Logger logger = LoggerFactory.getLogger(TokenMapper.class);

    public JWTPayload toAccessToken(Account account, Device device, Date expireAt) {
        logger.debug("jwt id {}", UUID.randomUUID());

        return new AccessTokenPayload(
                device.getDeviceType().value(),           // audience
                UUID.randomUUID().toString(),             // jwtId
                expireAt,                                 // expireAt
                new Date(),                               // issuedAt
                account.getId().value().toString(),       // uuid
                account.getFullName() != null ?           // fullName
                        account.getFullName().value() : null,
                account.getPhotoUrl() != null ?           // photoUrl
                        account.getPhotoUrl().value() : null,
                account.isBlocked(),                      // blocked
                account.getMobileNumber().value(),        // mobileNumber
                device.getDeviceId().value(),             // deviceId
                account.getPermissions().stream()         // permissions
                        .map(Enum::name)
                        .collect(Collectors.toList())
        );
    }

    public JWTPayload toRefreshToken(Account account, Device device, Date expireAt) {
        return new RefreshTokenPayload(
                device.getDeviceType().value(),           // audience
                UUID.randomUUID().toString(),             // jwtId
                expireAt,                                 // expireAt
                new Date(),                               // issuedAt
                account.getId().value().toString(),       // uuid
                account.getMobileNumber().value(),        // mobileNumber
                device.getDeviceId().value()             // deviceId
        );
    }
}