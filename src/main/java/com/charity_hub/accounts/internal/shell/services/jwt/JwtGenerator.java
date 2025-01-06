package com.charity_hub.accounts.internal.shell.services.jwt;

import com.charity_hub.accounts.internal.core.contracts.IJWTGenerator;
import com.charity_hub.accounts.internal.core.model.account.Account;
import com.charity_hub.accounts.internal.core.model.device.Device;
import com.charity_hub.shared.auth.JWTPayload;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Component
public class JwtGenerator implements IJWTGenerator {

    private final TokenMapper tokenMapper;
    
    @Value("${auth.secretKey}")
    private String secretKey;

    public JwtGenerator(TokenMapper tokenMapper) {
        this.tokenMapper = tokenMapper;
    }

    @Override
    public String generateAccessToken(Account account, Device device) {
        Calendar expiryDate = Calendar.getInstance();
        expiryDate.add(Calendar.MINUTE, 5);
        JWTPayload jwtPayload = tokenMapper.toAccessToken(
            account, 
            device, 
            new Date(expiryDate.getTimeInMillis())
        );
        return createToken(jwtPayload);
    }

    @Override
    public String generateRefreshToken(Account account, Device device) {
        Calendar expiryDate = Calendar.getInstance();
        expiryDate.add(Calendar.YEAR, 1);
        JWTPayload jwtPayload = tokenMapper.toRefreshToken(
            account, 
            device, 
            new Date(expiryDate.getTimeInMillis())
        );
        return createToken(jwtPayload);
    }

    private String createToken(JWTPayload jwtPayload) {
        var builder = Jwts.builder()
            .subject(jwtPayload.getSubject())
            .issuedAt(jwtPayload.getIssuedAt())
            .issuer(jwtPayload.getIssuer())
            .audience().add(jwtPayload.getAudience()).and()
            .id(jwtPayload.getJwtId())
            .expiration(jwtPayload.getExpireAt())
            .id(jwtPayload.getJwtId())
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()));

        for (Map.Entry<String, Object> entry : jwtPayload.toMap().entrySet()) {
            builder.claims().add(entry.getKey(), entry.getValue());
        }

        return builder.compact();
    }
}