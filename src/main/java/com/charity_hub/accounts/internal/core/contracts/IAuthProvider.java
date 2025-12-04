package com.charity_hub.accounts.internal.core.contracts;


public interface IAuthProvider {
    String getVerifiedMobileNumber(String idToken);
}