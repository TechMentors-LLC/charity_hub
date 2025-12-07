package com.charity_hub.cases.internal.application.commands.ChangeCaseStatus;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ChangeCaseStatusHandler extends VoidCommandHandler<ChangeCaseStatus> {
    private final ICaseRepo caseRepo;

    public ChangeCaseStatusHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    public void handle(ChangeCaseStatus command) {
            var case_ = caseRepo.getByCodeTemp(new CaseCode(command.caseCode()))
                    .orElseThrow(() -> new NotFoundException("This case is not found"));

            if (command.isActionOpen()) {
                case_.open();
            } else {
                case_.close();
            }

            caseRepo.saveTemp(case_);

    }
}