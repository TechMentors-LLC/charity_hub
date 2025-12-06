package com.charity_hub.cases.internal.application.queries;

public record CaseCriteria(
        Integer code,
        String tag,
        String content
) {}