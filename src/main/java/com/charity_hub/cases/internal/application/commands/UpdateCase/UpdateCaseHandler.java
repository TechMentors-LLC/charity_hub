package com.charity_hub.cases.internal.application.commands.UpdateCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UpdateCaseHandler extends VoidCommandHandler<UpdateCase> {
    private final ICaseRepo caseRepo;

    public UpdateCaseHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    public void handle(UpdateCase command) {
        var case_ = caseRepo.getByCodeTemp(new CaseCode(command.caseCode()))
                .orElseThrow(() -> new NotFoundException("This case is not found"));

        case_.update(
                command.title(),
                command.description(),
                command.goal(),
                command.acceptZakat(),
                command.documents()
        );
        caseRepo.saveTemp(case_);
    }
}