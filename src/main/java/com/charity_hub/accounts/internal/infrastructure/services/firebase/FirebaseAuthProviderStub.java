package com.charity_hub.accounts.internal.infrastructure.services.firebase;

import com.charity_hub.accounts.internal.domain.contracts.IAuthProvider;
import com.charity_hub.accounts.internal.domain.model.account.MobileNumber;
import com.charity_hub.shared.exceptions.UnAuthorized;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@Primary
@ConditionalOnProperty(name = "firebase.test-mode", havingValue = "true", matchIfMissing = false)
public class FirebaseAuthProviderStub implements IAuthProvider {

    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthProviderStub.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getVerifiedMobileNumber(String idToken) {
        // First, try to decode as a JWT token (Firebase ID token)
        if (idToken.contains(".")) {
            try {
                String[] parts = idToken.split("\\.");
                if (parts.length >= 2) {
                    String payload = parts[1];
                    // Add padding if needed for Base64 decoding
                    int padding = 4 - payload.length() % 4;
                    if (padding != 4) {
                        payload = payload + "=".repeat(padding);
                    }
                    byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
                    String decodedPayload = new String(decodedBytes);

                    JsonNode jsonNode = objectMapper.readTree(decodedPayload);
                    if (jsonNode.has("phone_number")) {
                        String phoneNumber = jsonNode.get("phone_number").asText();
                        log.info("Extracted phone number from Firebase token: {}", phoneNumber);
                        return MobileNumber.create(phoneNumber).value();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to decode JWT token, falling back to treating as mobile number: {}", e.getMessage());
            }
        }

        // Fallback: treat the token as a mobile number directly (for simple test cases)
        try {
            return MobileNumber.create(idToken).value();
        } catch (Exception e) {
            log.error("Failed to parse idToken as mobile number: {}", e.getMessage());
            throw new UnAuthorized("Invalid token format");
        }
    }
}