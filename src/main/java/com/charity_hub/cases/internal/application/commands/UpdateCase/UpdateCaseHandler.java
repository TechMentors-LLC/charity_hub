package com.charity_hub.cases.internal.application.commands.UpdateCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCaseHandler extends CommandHandler<UpdateCase, Void> {
    private final ICaseRepo caseRepo;

    public UpdateCaseHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    @Transactional
    public Void handle(UpdateCase command) {
        var case_ =  caseRepo.getByCode(new CaseCode(command.caseCode()))
                .orElseThrow(() -> new NotFoundException("This case is not found"));

        updateAndSaveCase(case_, command);
        return null;
    }

    private void updateAndSaveCase(Case case_, UpdateCase command) {
        case_.update(
                command.title(),
                command.description(),
                command.goal(),
                command.acceptZakat(),
                command.documents()
        );
        caseRepo.save(case_);
    }
}