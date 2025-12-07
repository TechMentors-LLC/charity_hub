package com.charity_hub.cases.internal.application.commands.DeleteDraftCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.VoidCommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DeleteDraftCaseHandler extends VoidCommandHandler<DeleteDraftCase> {
    private final ICaseRepo caseRepo;

    public DeleteDraftCaseHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    public void handle(DeleteDraftCase command) {
        var case_ = caseRepo.getByCode(new CaseCode(command.caseCode()))
                .orElseThrow(() -> new NotFoundException("This case is not found"));
        
        case_.delete(caseRepo);
    }
}