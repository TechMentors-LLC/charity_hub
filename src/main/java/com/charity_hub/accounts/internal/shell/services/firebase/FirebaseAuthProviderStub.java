package com.charity_hub.accounts.internal.shell.services.firebase;

import com.charity_hub.accounts.internal.core.contracts.IAuthProvider;
import com.charity_hub.accounts.internal.core.model.account.MobileNumber;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@Primary
@ConditionalOnProperty(
        name = "firebase.test-mode",
        havingValue = "true",
        matchIfMissing = false
)
public class FirebaseAuthProviderStub implements IAuthProvider {

    private final FirebaseAuthProvider firebaseAuthProvider;

    public FirebaseAuthProviderStub(FirebaseAuthProvider firebaseAuthProvider) {
        this.firebaseAuthProvider = firebaseAuthProvider;
    }

    @Override
    public CompletableFuture<String> getVerifiedMobileNumber(String idToken) {
        try {
            return CompletableFuture.completedFuture(MobileNumber.create(idToken).value());
        } catch (Exception e) {
            return firebaseAuthProvider.getVerifiedMobileNumber(idToken);
        }
    }
    @Override
    public String getVerifiedMobileNumberTemp(String idToken) {
        try {
            return MobileNumber.create(idToken).value();
        } catch (Exception e) {
            return firebaseAuthProvider.getVerifiedMobileNumberTemp(idToken);
        }
    }

}