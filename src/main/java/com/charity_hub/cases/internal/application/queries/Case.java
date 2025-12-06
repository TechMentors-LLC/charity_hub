package com.charity_hub.cases.internal.application.queries;

import java.util.List;

public record Case(
        int code,
        String title,
        String description,
        int goal,
        int collected,
        String status, // Mapped from int status
        List<String> tags,
        long lastUpdated
) {}
