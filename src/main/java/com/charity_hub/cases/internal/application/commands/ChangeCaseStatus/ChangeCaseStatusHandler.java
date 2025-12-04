package com.charity_hub.cases.internal.application.commands.ChangeCaseStatus;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangeCaseStatusHandler extends CommandHandler<ChangeCaseStatus, Void> {
    private final ICaseRepo caseRepo;

    public ChangeCaseStatusHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    @Transactional
    public Void handle(ChangeCaseStatus command) {

        var case_ = caseRepo.getByCode(new CaseCode(command.caseCode()))
                .orElseThrow(() -> new NotFoundException("Case not found with code: " + command.caseCode()));

        if (command.isActionOpen()) {
            case_.open();
        } else {
            case_.close();
        }

        caseRepo.save(case_);
        return null;
    }
}