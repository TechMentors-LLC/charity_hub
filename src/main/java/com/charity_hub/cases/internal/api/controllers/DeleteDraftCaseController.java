package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.DeleteDraftCase.DeleteDraftCase;
import com.charity_hub.cases.internal.application.commands.DeleteDraftCase.DeleteDraftCaseHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteDraftCaseController {

    private final DeleteDraftCaseHandler deleteDraftCaseHandler;

    public DeleteDraftCaseController(DeleteDraftCaseHandler deleteDraftCaseHandler) {
        this.deleteDraftCaseHandler = deleteDraftCaseHandler;
    }

    @DeleteMapping("/v1/cases/{caseCode}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    public ResponseEntity<Void> handle(@PathVariable int caseCode) {
        deleteDraftCaseHandler.handle(new DeleteDraftCase(caseCode));
        return ResponseEntity.ok().build();
    }
}