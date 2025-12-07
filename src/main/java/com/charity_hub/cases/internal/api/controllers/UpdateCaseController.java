package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.UpdateCase.UpdateCase;
import com.charity_hub.cases.internal.application.commands.UpdateCase.UpdateCaseHandler;
import com.charity_hub.cases.internal.api.dtos.UpdateCaseRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateCaseController {

    private final UpdateCaseHandler updateCaseHandler;

    public UpdateCaseController(UpdateCaseHandler updateCaseHandler) {
        this.updateCaseHandler = updateCaseHandler;
    }

    @PutMapping("/v1/cases/{caseCode}")
    public ResponseEntity<Void> handle(
            @PathVariable int caseCode,
            @RequestBody UpdateCaseRequest request) {
        UpdateCase command = new UpdateCase(
                caseCode,
                request.title(),
                request.description(),
                request.goal(),
                request.acceptZakat(),
                request.documents()
        );
        updateCaseHandler.handle(command);
        return ResponseEntity.ok().build();
    }
}