package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.ChangeCaseStatus.ChangeCaseStatus;
import com.charity_hub.cases.internal.application.commands.ChangeCaseStatus.ChangeCaseStatusHandler;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChangeCaseStatusController {
    private static final Logger log = LoggerFactory.getLogger(ChangeCaseStatusController.class);
    private final ChangeCaseStatusHandler changeCaseStatusHandler;

    public ChangeCaseStatusController(ChangeCaseStatusHandler changeCaseStatusHandler) {
        this.changeCaseStatusHandler = changeCaseStatusHandler;
    }

    @PostMapping("/v1/cases/{caseCode}/open")
    @PreAuthorize("hasAnyAuthority('CREATE_CASES', 'FULL_ACCESS')")
    @Timed(value = "charity_hub.cases.open", description = "Time taken to open a case")
    @Observed(name = "cases.open", contextualName = "open-case")
    public ResponseEntity<Void> open(@PathVariable int caseCode) {
        log.info("Opening case: {}", caseCode);
        ChangeCaseStatus command = new ChangeCaseStatus(caseCode, true);
        changeCaseStatusHandler.handle(command);
        log.info("Case opened successfully: {}", caseCode);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/v1/cases/{caseCode}/close")
    @PreAuthorize("hasAnyAuthority('CREATE_CASES', 'FULL_ACCESS')")
    @Timed(value = "charity_hub.cases.close", description = "Time taken to close a case")
    @Observed(name = "cases.close", contextualName = "close-case")
    public ResponseEntity<Void> close(@PathVariable int caseCode) {
        log.info("Closing case: {}", caseCode);
        ChangeCaseStatus command = new ChangeCaseStatus(caseCode, false);
        changeCaseStatusHandler.handle(command);
        log.info("Case closed successfully: {}", caseCode);
        return ResponseEntity.ok().build();
    }
}