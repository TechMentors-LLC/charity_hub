package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.UpdateCase.UpdateCase;
import com.charity_hub.cases.internal.application.commands.UpdateCase.UpdateCaseHandler;
import com.charity_hub.cases.internal.api.dtos.UpdateCaseRequest;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateCaseController {
    private static final Logger log = LoggerFactory.getLogger(UpdateCaseController.class);
    private final UpdateCaseHandler updateCaseHandler;

    public UpdateCaseController(UpdateCaseHandler updateCaseHandler) {
        this.updateCaseHandler = updateCaseHandler;
    }

    @PutMapping("/v1/cases/{caseCode}")
    @Observed(name = "cases.update", contextualName = "update-case")
    public ResponseEntity<Void> handle(
            @PathVariable int caseCode,
            @RequestBody UpdateCaseRequest request) {
        log.info("Updating case: {}", caseCode);
        UpdateCase command = new UpdateCase(
                caseCode,
                request.title(),
                request.description(),
                request.goal(),
                request.acceptZakat(),
                request.documents()
        );
        updateCaseHandler.handle(command);
        log.info("Case updated successfully: {}", caseCode);
        return ResponseEntity.ok().build();
    }
}