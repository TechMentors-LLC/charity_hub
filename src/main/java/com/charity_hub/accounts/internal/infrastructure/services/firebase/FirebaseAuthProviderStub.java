package com.charity_hub.accounts.internal.infrastructure.services.firebase;

import com.charity_hub.accounts.internal.domain.contracts.IAuthProvider;
import com.charity_hub.accounts.internal.domain.model.account.MobileNumber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@ConditionalOnProperty(
        name = "firebase.test-mode",
        havingValue = "true",
        matchIfMissing = false
)
public class FirebaseAuthProviderStub implements IAuthProvider {

    @Override
    public String getVerifiedMobileNumber(String idToken) {
        // In test mode, treat the token as a mobile number directly
        return MobileNumber.create(idToken).value();
    }
}