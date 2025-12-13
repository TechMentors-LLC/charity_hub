package com.charity_hub.accounts.internal.domain.contracts;

public interface IAuthProvider {
    String getVerifiedMobileNumber(String idToken);
}