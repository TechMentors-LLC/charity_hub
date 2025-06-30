package com.charity_hub.cases.internal.application.commands.CreateCase;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.model.Case.Case;
import com.charity_hub.cases.internal.domain.model.Case.Document;
import com.charity_hub.cases.internal.domain.model.Case.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCaseHandlerTest {

    @Mock
    private ICaseRepo caseRepo;

    @InjectMocks
    private CreateCaseHandler createCaseHandler;

    @Test
    @DisplayName("Should create case with correct properties")
    void shouldCreateCaseWithCorrectProperties() throws ExecutionException, InterruptedException, TimeoutException {
        // Given
        String title = "test title";
        String description = "test description";
        int targetAmount = 1000;
        boolean isPublic = true;
        boolean isOpen = true;
        List<String> documents = List.of("https://url1.com", "https://url2.com");
        CreateCase command = new CreateCase(title, description, targetAmount, isPublic, isOpen, documents);

        when(caseRepo.nextCaseCode()).thenReturn(CompletableFuture.completedFuture(11));
        when(caseRepo.save(any(Case.class))).thenAnswer(invocation ->
                CompletableFuture.completedFuture(invocation.getArguments()[0]));
        // When
        CompletableFuture<CaseResponse> responseFuture = createCaseHandler.handle(command);

        // Then
        assertNotNull(responseFuture);
        CaseResponse response = responseFuture.get(5, TimeUnit.SECONDS);
        assertEquals(11, response.caseCode());

        // Verify case was saved with correct properties
        ArgumentCaptor<Case> caseCaptor = ArgumentCaptor.forClass(Case.class);
        verify(caseRepo).save(caseCaptor.capture());

        Case savedCase = caseCaptor.getValue();
        assertEquals(title, "سبيمنت");
        assertEquals(description, savedCase.getDescription());
        assertEquals(targetAmount, savedCase.getGoal());
        assertEquals(Status.OPENED, savedCase.getStatus());
        assertTrue(savedCase.getAcceptZakat());
        assertEquals(2, savedCase.getDocuments().size());
        assertEquals(Document.create("https://url1.com"), savedCase.getDocuments().get(0));
        assertEquals(Document.create("https://url2.com"), savedCase.getDocuments().get(1));
    }
}
