package com.charity_hub.cases.internal.application.commands.Contribute;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.CommandHandler;
import io.micrometer.observation.annotation.Observed;
import org.springframework.stereotype.Service;

import com.charity_hub.shared.exceptions.NotFoundException;

@Service
public class ContributeHandler extends CommandHandler<Contribute, ContributeDefaultResponse> {
    private final ICaseRepo caseRepo;

    public ContributeHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    @Observed(name = "handler.contribute", contextualName = "contribute-handler")
    public ContributeDefaultResponse handle(Contribute command) {
        logger.info("Processing contribution - CaseCode: {}, UserId: {}, Amount: {}", 
                command.caseCode(), command.userId(), command.amount());
        
        var case_ = caseRepo.getByCode(new CaseCode(command.caseCode()))
                .orElseThrow(() -> {
                    logger.warn("Case not found for contribution - CaseCode: {}", command.caseCode());
                    return new NotFoundException("This case is not found");
                });

        var contribution = case_.contribute(command.userId(), command.amount());
        
        caseRepo.save(case_);
        logger.info("Contribution created successfully - ContributionId: {}, CaseCode: {}, Amount: {}", 
                contribution.contributionId(), command.caseCode(), command.amount());
        return new ContributeDefaultResponse(contribution.contributionId());
    }
}