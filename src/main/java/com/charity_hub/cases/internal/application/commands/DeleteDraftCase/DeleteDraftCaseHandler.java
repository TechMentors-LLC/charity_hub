package com.charity_hub.cases.internal.application.commands.DeleteDraftCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

@Service
public class DeleteDraftCaseHandler extends VoidCommandHandler<DeleteDraftCase> {
    private final ICaseRepo caseRepo;

    public DeleteDraftCaseHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    @Observed(name = "handler.delete_draft_case", contextualName = "delete-draft-case-handler")
    public void handle(DeleteDraftCase command) {
        logger.info("Deleting draft case - CaseCode: {}", command.caseCode());
        
        var case_ = caseRepo.getByCode(new CaseCode(command.caseCode()))
                .orElseThrow(() -> {
                    logger.warn("Draft case not found for deletion - CaseCode: {}", command.caseCode());
                    return new NotFoundException("This case is not found");
                });
        
        case_.delete(caseRepo);
        logger.info("Draft case deleted successfully - CaseCode: {}", command.caseCode());
    }
}