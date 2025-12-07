package com.charity_hub.accounts.internal.core.contracts;

import com.google.firebase.auth.FirebaseAuthException;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface IAuthProvider {
    String getVerifiedMobileNumberTemp(String idToken);
    CompletableFuture<String> getVerifiedMobileNumber(String idToken);
}