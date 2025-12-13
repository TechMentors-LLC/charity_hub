package com.charity_hub.accounts.internal.infrastructure.services.firebase;

import com.charity_hub.accounts.internal.domain.contracts.IAuthProvider;
import com.charity_hub.shared.exceptions.UnAuthorized;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "firebase.test-mode", havingValue = "false", matchIfMissing = true)
public class FirebaseAuthProvider implements IAuthProvider {

    private final FirebaseAuth firebaseAuth;
    private static final Logger log = LoggerFactory.getLogger(FirebaseAuthProvider.class);

    public FirebaseAuthProvider(FirebaseAuth firebaseAuth) {
        this.firebaseAuth = firebaseAuth;
    }

    @Override
    public String getVerifiedMobileNumber(String idToken) {
        try {
            var firebaseToken = verify(idToken);
            var userRecord = firebaseAuth.getUser(firebaseToken.getUid());
            if (userRecord.getPhoneNumber() != null) {
                return userRecord.getPhoneNumber().replace("+", "");
            } else {
                log.error("Failed to verify mobile number");
                throw new UnAuthorized();
            }
        } catch (FirebaseAuthException e) {
            throw new RuntimeException(e);
        }
    }

    private FirebaseToken verify(String idToken) {
        try {
            return firebaseAuth.verifyIdToken(idToken);
        } catch (Exception authError) {
            log.error("Failed to verify Id token: {}", idToken, authError);
            throw new UnAuthorized();
        }
    }
}