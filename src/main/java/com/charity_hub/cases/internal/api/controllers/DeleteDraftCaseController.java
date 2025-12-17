package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.DeleteDraftCase.DeleteDraftCase;
import com.charity_hub.cases.internal.application.commands.DeleteDraftCase.DeleteDraftCaseHandler;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DeleteDraftCaseController {
    private static final Logger log = LoggerFactory.getLogger(DeleteDraftCaseController.class);
    private final DeleteDraftCaseHandler deleteDraftCaseHandler;

    public DeleteDraftCaseController(DeleteDraftCaseHandler deleteDraftCaseHandler) {
        this.deleteDraftCaseHandler = deleteDraftCaseHandler;
    }

    @DeleteMapping("/v1/cases/{caseCode}")
    @PreAuthorize("hasAnyAuthority('FULL_ACCESS')")
    @Observed(name = "cases.delete_draft", contextualName = "delete-draft-case")
    public ResponseEntity<Void> handle(@PathVariable int caseCode) {
        log.info("Deleting draft case: {}", caseCode);
        deleteDraftCaseHandler.handle(new DeleteDraftCase(caseCode));
        log.info("Draft case deleted successfully: {}", caseCode);
        return ResponseEntity.ok().build();
    }
}