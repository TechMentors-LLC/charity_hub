package com.charity_hub.cases.internal.application.queries.GetAllCases;

public interface IGetAllCasesHandler {
    GetCasesQueryResult handle(GetAllCasesQuery query);
}
