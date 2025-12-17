package com.charity_hub.cases.internal.application.queries.GetAllCases;

import com.charity_hub.shared.abstractions.Query;

public record GetAllCasesQuery(
                Integer code,
                String tag,
                String content,
                int offset,
                int limit,
                boolean onlyZakat) implements Query {

}