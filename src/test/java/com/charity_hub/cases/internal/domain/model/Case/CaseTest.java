package com.charity_hub.cases.internal.domain.model.Case;

import com.charity_hub.cases.internal.domain.contracts.ICaseRepo;
import com.charity_hub.cases.internal.domain.exceptions.CaseExceptions;
import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Case Domain Model Tests")
class CaseTest {

    @Mock
    private ICaseRepo caseRepo;

    private Case createOpenedCase() {
        return Case.create(new CaseProps(
                1,
                "Test Case",
                "Test Description",
                10000,
                Status.OPENED,
                true,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new Date(),
                new Date()
        ));
    }

    private Case createDraftCase() {
        return Case.create(new CaseProps(
                2,
                "Draft Case",
                "Draft Description",
                5000,
                Status.DRAFT,
                false,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new Date(),
                new Date()
        ));
    }

    @Nested
    @DisplayName("When creating a new case")
    class CaseCreation {

        @Test
        @DisplayName("Should create case with correct properties")
        void shouldCreateCaseWithCorrectProperties() {
            Case case_ = createOpenedCase();

            assertThat(case_.getTitle()).isEqualTo("Test Case");
            assertThat(case_.getDescription()).isEqualTo("Test Description");
            assertThat(case_.getGoal()).isEqualTo(10000);
            assertThat(case_.getAcceptZakat()).isTrue();
            assertThat(case_.getStatus()).isEqualTo(Status.OPENED);
        }

        @Test
        @DisplayName("Should create case with empty contributions")
        void shouldCreateCaseWithEmptyContributions() {
            Case case_ = createOpenedCase();

            assertThat(case_.getContributions()).isEmpty();
            assertThat(case_.totalContributions()).isZero();
            assertThat(case_.numberOfContributions()).isZero();
        }
    }

    @Nested
    @DisplayName("When updating a case")
    class CaseUpdate {

        @Test
        @DisplayName("Should update case details")
        void shouldUpdateCaseDetails() {
            Case case_ = createOpenedCase();

            case_.update("Updated Title", "Updated Description", 20000, false, List.of("https://doc.com"));

            assertThat(case_.getTitle()).isEqualTo("Updated Title");
            assertThat(case_.getDescription()).isEqualTo("Updated Description");
            assertThat(case_.getGoal()).isEqualTo(20000);
            assertThat(case_.getAcceptZakat()).isFalse();
        }
    }

    @Nested
    @DisplayName("When changing case status")
    class CaseStatusChanges {

        @Test
        @DisplayName("Should open a draft case")
        void shouldOpenDraftCase() {
            Case case_ = createDraftCase();

            case_.open();

            assertThat(case_.getStatus()).isEqualTo(Status.OPENED);
        }

        @Test
        @DisplayName("Should close an opened case")
        void shouldCloseOpenedCase() {
            Case case_ = createOpenedCase();

            case_.close();

            assertThat(case_.getStatus()).isEqualTo(Status.CLOSED);
        }

        @Test
        @DisplayName("Should throw when opening already opened case")
        void shouldThrowWhenOpeningAlreadyOpenedCase() {
            Case case_ = createOpenedCase();

            assertThatThrownBy(case_::open)
                    .isInstanceOf(CaseExceptions.CaseAlreadyOpenedException.class);
        }
    }

    @Nested
    @DisplayName("When contributing to a case")
    class CaseContributions {

        @Test
        @DisplayName("Should create contribution for opened case")
        void shouldCreateContributionForOpenedCase() {
            Case case_ = createOpenedCase();
            UUID contributorId = UUID.randomUUID();

            Contribution contribution = case_.contribute(contributorId, 500);

            assertThat(contribution).isNotNull();
            assertThat(contribution.getContributorId()).isEqualTo(contributorId);
            assertThat(contribution.getMoneyValue().value()).isEqualTo(500);
        }

        @Test
        @DisplayName("Should throw when contributing to closed case")
        void shouldThrowWhenContributingToClosedCase() {
            Case case_ = createOpenedCase();
            case_.close();
            UUID contributorId = UUID.randomUUID();

            assertThatThrownBy(() -> case_.contribute(contributorId, 500))
                    .isInstanceOf(CaseExceptions.CannotContributeException.class);
        }
    }

    @Nested
    @DisplayName("When deleting a case")
    class CaseDeletion {

        @Test
        @DisplayName("Should delete draft case")
        void shouldDeleteDraftCase() {
            Case case_ = createDraftCase();

            case_.delete(caseRepo);

            verify(caseRepo).delete(case_.getId());
        }

        @Test
        @DisplayName("Should throw when deleting non-draft case")
        void shouldThrowWhenDeletingNonDraftCase() {
            Case case_ = createOpenedCase();

            assertThatThrownBy(() -> case_.delete(caseRepo))
                    .isInstanceOf(CaseExceptions.CannotDeleteCaseException.class);
        }
    }
}
