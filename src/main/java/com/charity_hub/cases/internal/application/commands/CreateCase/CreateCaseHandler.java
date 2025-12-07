package com.charity_hub.cases.internal.application.commands.CreateCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.NewCaseProbs;
import com.charity_hub.cases.internal.domain.model.Case.Status;
import com.charity_hub.shared.abstractions.CommandHandlerTemp;
import org.springframework.stereotype.Component;

@Component
public class CreateCaseHandler extends CommandHandlerTemp<CreateCase, CaseResponse> {
    private final ICaseRepo caseRepo;

    public CreateCaseHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    public CaseResponse handle(CreateCase command) {
        var newCase = Case.newCase(
                new NewCaseProbs(
                        caseRepo.nextCaseCodeTemp(),
                        command.title(),
                        command.description(),
                        command.goal(),
                        command.publish() ? Status.OPENED : Status.DRAFT,
                        command.acceptZakat(),
                        command.documents()
                )
        );

        caseRepo.saveTemp(newCase);
        return new CaseResponse(newCase.getCaseCode().value());
    }
}