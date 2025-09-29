package com.charity_hub.cases.internal.application.commands.UpdateCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.CaseCode;
import com.charity_hub.shared.abstractions.CommandHandler;
import com.charity_hub.shared.exceptions.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class UpdateCaseHandler extends CommandHandler<UpdateCase, Void> {
    private final ICaseRepo caseRepo;

    public UpdateCaseHandler(ICaseRepo caseRepo) {
        this.caseRepo = caseRepo;
    }

    @Override
    public CompletableFuture<Void> handle(UpdateCase command) {
        return caseRepo.getByCode(new CaseCode(command.caseCode()))
                .thenCompose(case_ -> {
                    if (case_ == null) {
                        return CompletableFuture.failedFuture(
                                new NotFoundException("This case is not found")
                        );
                    }
                    return updateAndSaveCase(case_, command);
                });
    }

    private CompletableFuture<Void> updateAndSaveCase(Case case_, UpdateCase command) {
        case_.update(
                command.title(),
                command.description(),
                command.goal(),
                command.acceptZakat(),
                command.documents()
        );
        return caseRepo.save(case_);

    }
}