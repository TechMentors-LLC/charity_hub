package com.charity_hub.cases.internal.application.commands.ChangeCaseStatus;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import io.micrometer.core.annotation.Timed;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Component;

@Component
public class ChangeCaseStatusHandler extends VoidCommandHandler<ChangeCaseStatus> {
    private final ICaseRepo caseRepo;

    public ChangeCaseStatusHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    @Timed(value = "charity_hub.handler.change_case_status", description = "Time taken by ChangeCaseStatusHandler")
    @Observed(name = "handler.change_case_status", contextualName = "change-case-status-handler")
    public void handle(ChangeCaseStatus command) {
            String action = command.isActionOpen() ? "OPEN" : "CLOSE";
            logger.info("Changing case status - CaseCode: {}, Action: {}", command.caseCode(), action);
            
            var case_ = caseRepo.getByCode(new CaseCode(command.caseCode()))
                    .orElseThrow(() -> {
                        logger.warn("Case not found for status change - CaseCode: {}", command.caseCode());
                        return new NotFoundException("This case is not found");
                    });

            if (command.isActionOpen()) {
                case_.open();
            } else {
                case_.close();
            }

            caseRepo.save(case_);
            logger.info("Case status changed successfully - CaseCode: {}, NewStatus: {}", command.caseCode(), action);
    }
}