package com.charity_hub.accounts.internal.api.dtos;

import com.charity_hub.shared.abstractions.Request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record InviteUserRequest(
        @NotBlank(message = "Mobile number is required") @Pattern(regexp = "^[0-9]{10,15}$", message = "Mobile number must be 10-15 digits") String mobileNumber)
        implements Request {
}