package com.charity_hub.cases.internal.application.commands.Contribute;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.CommandHandlerTemp;
import org.springframework.stereotype.Service;

import com.charity_hub.shared.exceptions.NotFoundException;

@Service
public class ContributeHandler extends CommandHandlerTemp<Contribute, ContributeDefaultResponse> {
    private final ICaseRepo caseRepo;

    public ContributeHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    public ContributeDefaultResponse handle(Contribute command) {
        var case_ = caseRepo.getByCodeTemp(new CaseCode(command.caseCode()))
                .orElseThrow(() -> new NotFoundException("This case is not found"));

        var contribution = case_.contribute(command.userId(), command.amount());
        
        caseRepo.saveTemp(case_);
        return new ContributeDefaultResponse(contribution.contributionId());
    }
}