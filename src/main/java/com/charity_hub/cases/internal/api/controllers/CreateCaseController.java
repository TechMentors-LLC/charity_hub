package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.CreateCase.CreateCase;
import com.charity_hub.cases.internal.application.commands.CreateCase.CreateCaseHandler;
import com.charity_hub.cases.internal.application.commands.CreateCase.CaseResponse;
import com.charity_hub.cases.internal.api.dtos.CreateCaseRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CreateCaseController {

    private final CreateCaseHandler createCaseHandler;

    public CreateCaseController(CreateCaseHandler createCaseHandler) {
        this.createCaseHandler = createCaseHandler;
    }

    @PostMapping("/v1/cases")
    @PreAuthorize("hasAnyAuthority('CREATE_CASES', 'FULL_ACCESS')")
    public ResponseEntity<CaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        CreateCase createCommand = new CreateCase(
                request.title(),
                request.description(),
                request.goal(),
                request.publish(),
                request.acceptZakat(),
                request.documents()
        );

        var response = createCaseHandler.handle(createCommand);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}