package com.charity_hub.cases.internal.api.controllers;

import com.charity_hub.cases.internal.application.commands.CreateCase.CreateCase;
import com.charity_hub.cases.internal.application.commands.CreateCase.CreateCaseHandler;
import com.charity_hub.cases.internal.application.commands.CreateCase.CaseResponse;
import com.charity_hub.cases.internal.api.dtos.CreateCaseRequest;
import com.charity_hub.shared.observability.metrics.BusinessMetrics;
import io.micrometer.observation.annotation.Observed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CreateCaseController {
    private static final Logger log = LoggerFactory.getLogger(CreateCaseController.class);
    private final CreateCaseHandler createCaseHandler;
    private final BusinessMetrics businessMetrics;

    public CreateCaseController(CreateCaseHandler createCaseHandler, BusinessMetrics businessMetrics) {
        this.createCaseHandler = createCaseHandler;
        this.businessMetrics = businessMetrics;
    }

    @PostMapping("/v1/cases")
    @PreAuthorize("hasAnyAuthority('CREATE_CASES', 'FULL_ACCESS')")
    @Observed(name = "cases.create", contextualName = "create-case")
    public ResponseEntity<CaseResponse> createCase(@RequestBody CreateCaseRequest request) {
        log.info("Creating new case: {}", request.title());
        CreateCase createCommand = new CreateCase(
                request.title(),
                request.description(),
                request.goal(),
                request.publish(),
                request.acceptZakat(),
                request.documents()
        );

        var response = createCaseHandler.handle(createCommand);
        businessMetrics.recordCaseCreation();
        log.info("Case created successfully with code: {}", response.caseCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}