package com.charity_hub.cases.internal.api.dtos;

import com.charity_hub.shared.abstractions.Request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateCaseRequest(
        @NotBlank(message = "Title is required") @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters") String title,

        @NotBlank(message = "Description is required") @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters") String description,

        @Min(value = 1, message = "Goal must be at least 1") int goal,

        boolean publish,

        boolean acceptZakat,

        List<String> documents) implements Request {
}