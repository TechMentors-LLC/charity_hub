package com.charity_hub.cases.internal.application.commands.CreateCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.NewCaseProbs;
import com.charity_hub.cases.internal.domain.model.Case.Status;
import com.charity_hub.shared.abstractions.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateCaseHandler extends CommandHandler<CreateCase, CaseResponse> {
    private final ICaseRepo caseRepo;

    public CreateCaseHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    public CaseResponse handle(CreateCase command) {
        logger.info("Creating new case - Title: {}, Goal: {}, Publish: {}", 
                command.title(), command.goal(), command.publish());
        
        var newCase = Case.newCase(
                new NewCaseProbs(
                        caseRepo.nextCaseCode(),
                        command.title(),
                        command.description(),
                        command.goal(),
                        command.publish() ? Status.OPENED : Status.DRAFT,
                        command.acceptZakat(),
                        command.documents()
                )
        );

        caseRepo.save(newCase);
        logger.info("Case created successfully - CaseCode: {}, Status: {}", 
                newCase.getCaseCode().value(), command.publish() ? "OPENED" : "DRAFT");
        return new CaseResponse(newCase.getCaseCode().value());
    }
}