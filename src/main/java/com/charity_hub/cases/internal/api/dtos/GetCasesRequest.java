package com.charity_hub.cases.internal.api.dtos;

import com.charity_hub.shared.abstractions.Request;

public record GetCasesRequest(int offset, int limit, Integer code, String tag, String content, boolean onlyZakat)
        implements Request {

}