package com.charity_hub.cases.internal.application.commands.CreateCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.NewCaseProbs;
import com.charity_hub.cases.internal.domain.model.Case.Status;
import com.charity_hub.shared.abstractions.CommandHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CreateCaseHandler extends CommandHandler<CreateCase, CaseResponse> {
    private final ICaseRepo caseRepo;

    public CreateCaseHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    @Transactional
    public CaseResponse handle(CreateCase command) {

        var newCase = Case.newCase(
                new NewCaseProbs(
                        caseRepo.nextCaseCode()
                                .orElseThrow(() -> new IllegalStateException("Failed to Get the New Case code")),
                        command.title(),
                        command.description(),
                        command.goal(),
                        command.publish() ? Status.OPENED : Status.DRAFT,
                        command.acceptZakat(),
                        command.documents()
                )
        );

        caseRepo.save(newCase);
        return new CaseResponse(newCase.getCaseCode().value());
    }
}