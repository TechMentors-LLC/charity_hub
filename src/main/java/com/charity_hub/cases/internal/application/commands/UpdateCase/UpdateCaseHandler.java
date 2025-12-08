package com.charity_hub.cases.internal.application.commands.UpdateCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

@Service
public class UpdateCaseHandler extends VoidCommandHandler<UpdateCase> {
    private final ICaseRepo caseRepo;

    public UpdateCaseHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    @Observed(name = "handler.update_case", contextualName = "update-case-handler")
    public void handle(UpdateCase command) {
        logger.info("Updating case - CaseCode: {}, Title: {}", command.caseCode(), command.title());
        
        var case_ = caseRepo.getByCode(new CaseCode(command.caseCode()))
                .orElseThrow(() -> {
                    logger.warn("Case not found for update - CaseCode: {}", command.caseCode());
                    return new NotFoundException("This case is not found");
                });

        case_.update(
                command.title(),
                command.description(),
                command.goal(),
                command.acceptZakat(),
                command.documents()
        );
        caseRepo.save(case_);
        logger.info("Case updated successfully - CaseCode: {}", command.caseCode());
    }
}