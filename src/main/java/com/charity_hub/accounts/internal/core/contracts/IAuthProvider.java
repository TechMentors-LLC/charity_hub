package com.charity_hub.accounts.internal.core.contracts;

import com.google.firebase.auth.FirebaseAuthException;

import java.util.Optional;

public interface IAuthProvider {
    String getVerifiedMobileNumber(String idToken);
}