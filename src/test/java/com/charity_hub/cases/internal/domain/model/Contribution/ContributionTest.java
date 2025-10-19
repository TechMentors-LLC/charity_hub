package com.charity_hub.cases.internal.domain.model.Contribution;

import com.charity_hub.shared.exceptions.BusinessRuleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Contribution Domain Model Tests")
class ContributionTest {

    // ============= CREATION TESTS =============

    @Test
    @DisplayName("Should create new contribution without proof URL")
    void shouldCreateNewContributionWithoutProofUrl() {
        // Given
        UUID contributorId = UUID.randomUUID();
        int caseCode = 12345;
        int amount = 1000;

        // When
        Contribution contribution = Contribution.new_(contributorId, caseCode, amount);

        // Then
        assertThat(contribution).isNotNull();
        assertThat(contribution.getContributorId()).isEqualTo(contributorId);
        assertThat(contribution.getCaseId().value()).isEqualTo(caseCode);
        assertThat(contribution.getMoneyValue().value()).isEqualTo(amount);
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PLEDGED);
        assertThat(contribution.getPaymentProof()).isNull();
        assertThat(contribution.getContributionDate()).isNotNull();
    }

    @Test
    @DisplayName("Should create contribution with all parameters including proof URL")
    void shouldCreateContributionWithProofUrl() {
        // Given
        UUID id = UUID.randomUUID();
        UUID contributorId = UUID.randomUUID();
        int caseCode = 12345;
        int amount = 1000;
        ContributionStatus status = ContributionStatus.PAID;
        Date date = new Date();
        String proofUrl = "https://example.com/proof.jpg";

        // When
        Contribution contribution = Contribution.create(
                id, contributorId, caseCode, amount, status, date, proofUrl
        );

        // Then
        assertThat(contribution).isNotNull();
        assertThat(contribution.getId().value()).isEqualTo(id);
        assertThat(contribution.getContributorId()).isEqualTo(contributorId);
        assertThat(contribution.getCaseId().value()).isEqualTo(caseCode);
        assertThat(contribution.getMoneyValue().value()).isEqualTo(amount);
        assertThat(contribution.getContributionStatus()).isEqualTo(status);
        assertThat(contribution.getPaymentProof()).isEqualTo(proofUrl);
        assertThat(contribution.getContributionDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("Should create contribution without proof URL when not provided")
    void shouldCreateContributionWithoutProofUrlWhenNotProvided() {
        // Given
        UUID id = UUID.randomUUID();
        UUID contributorId = UUID.randomUUID();
        int caseCode = 12345;
        int amount = 1000;
        ContributionStatus status = ContributionStatus.PLEDGED;
        Date date = new Date();

        // When
        Contribution contribution = Contribution.create(
                id, contributorId, caseCode, amount, status, date
        );

        // Then
        assertThat(contribution.getPaymentProof()).isNull();
    }

    // ============= PAY TESTS =============

    @Test
    @DisplayName("Should pay pledged contribution with proof URL")
    void shouldPayPledgedContributionWithProofUrl() {
        // Given
        Contribution contribution = Contribution.new_(UUID.randomUUID(), 12345, 1000);
        String proofUrl = "https://example.com/proof.jpg";

        // When
        contribution.pay(proofUrl);

        // Then
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
        assertThat(contribution.getPaymentProof()).isEqualTo(proofUrl);
    }

    @Test
    @DisplayName("Should pay pledged contribution without proof URL")
    void shouldPayPledgedContributionWithoutProofUrl() {
        // Given
        Contribution contribution = Contribution.new_(UUID.randomUUID(), 12345, 1000);

        // When
        contribution.pay(null);

        // Then
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
        assertThat(contribution.getPaymentProof()).isNull();
    }

    @Test
    @DisplayName("Should update proof URL when paying with different proof URL")
    void shouldUpdateProofUrlWhenPayingWithDifferentProofUrl() {
        // Given
        UUID id = UUID.randomUUID();
        Contribution contribution = Contribution.create(
                id, UUID.randomUUID(), 12345, 1000,
                ContributionStatus.PLEDGED, new Date(), "https://old-proof.com"
        );
        String newProofUrl = "https://new-proof.com";

        // When
        contribution.pay(newProofUrl);

        // Then
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
        assertThat(contribution.getPaymentProof()).isEqualTo(newProofUrl);
    }

    @Test
    @DisplayName("Should throw exception when paying already paid contribution")
    void shouldThrowExceptionWhenPayingAlreadyPaidContribution() {
        // Given
        UUID id = UUID.randomUUID();
        Contribution contribution = Contribution.create(
                id, UUID.randomUUID(), 12345, 1000,
                ContributionStatus.PAID, new Date(), "https://proof.com"
        );

        // When & Then
        assertThatThrownBy(() -> contribution.pay("https://new-proof.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("The Contribution has been paid already");

        // Status should remain PAID
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
    }

    @Test
    @DisplayName("Should throw exception when paying confirmed contribution")
    void shouldThrowExceptionWhenPayingConfirmedContribution() {
        // Given
        UUID id = UUID.randomUUID();
        Contribution contribution = Contribution.create(
                id, UUID.randomUUID(), 12345, 1000,
                ContributionStatus.CONFIRMED, new Date(), "https://proof.com"
        );

        // When & Then
        assertThatThrownBy(() -> contribution.pay("https://new-proof.com"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("The Contribution has been paid already");

        // Status should remain CONFIRMED
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.CONFIRMED);
    }

    // ============= CONFIRM TESTS =============

    @Test
    @DisplayName("Should confirm paid contribution")
    void shouldConfirmPaidContribution() {
        // Given
        UUID id = UUID.randomUUID();
        Contribution contribution = Contribution.create(
                id, UUID.randomUUID(), 12345, 1000,
                ContributionStatus.PAID, new Date(), "https://proof.com"
        );

        // When
        contribution.confirm();

        // Then
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Should confirm paid contribution without proof URL")
    void shouldConfirmPaidContributionWithoutProofUrl() {
        // Given
        UUID id = UUID.randomUUID();
        Contribution contribution = Contribution.create(
                id, UUID.randomUUID(), 12345, 1000,
                ContributionStatus.PAID, new Date(), null
        );

        // When
        contribution.confirm();

        // Then
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.CONFIRMED);
        assertThat(contribution.getPaymentProof()).isNull();
    }

    @Test
    @DisplayName("Should throw exception when confirming pledged contribution")
    void shouldThrowExceptionWhenConfirmingPledgedContribution() {
        // Given
        Contribution contribution = Contribution.new_(UUID.randomUUID(), 12345, 1000);

        // When & Then
        assertThatThrownBy(() -> contribution.confirm())
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("The Contribution must be paid before it can be confirmed");

        // Status should remain PLEDGED
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PLEDGED);
    }

    @Test
    @DisplayName("Should throw exception when confirming already confirmed contribution")
    void shouldThrowExceptionWhenConfirmingAlreadyConfirmedContribution() {
        // Given
        UUID id = UUID.randomUUID();
        Contribution contribution = Contribution.create(
                id, UUID.randomUUID(), 12345, 1000,
                ContributionStatus.CONFIRMED, new Date(), "https://proof.com"
        );

        // When & Then
        assertThatThrownBy(() -> contribution.confirm())
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("The Contribution is already confirmed");

        // Status should remain CONFIRMED
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.CONFIRMED);
    }

    // ============= PROOF URL PERSISTENCE TESTS =============

    @Test
    @DisplayName("Should preserve proof URL when confirming")
    void shouldPreserveProofUrlWhenConfirming() {
        // Given
        UUID id = UUID.randomUUID();
        String proofUrl = "https://example.com/proof.jpg";
        Contribution contribution = Contribution.create(
                id, UUID.randomUUID(), 12345, 1000,
                ContributionStatus.PAID, new Date(), proofUrl
        );

        // When
        contribution.confirm();

        // Then
        assertThat(contribution.getPaymentProof()).isEqualTo(proofUrl);
    }

    @Test
    @DisplayName("Should allow empty string as proof URL")
    void shouldAllowEmptyStringAsProofUrl() {
        // Given
        Contribution contribution = Contribution.new_(UUID.randomUUID(), 12345, 1000);

        // When
        contribution.pay("");

        // Then
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
        assertThat(contribution.getPaymentProof()).isEqualTo("");
    }

    // ============= WORKFLOW TESTS =============

    @Test
    @DisplayName("Should complete full workflow: pledged -> paid with proof -> confirmed")
    void shouldCompleteFullWorkflowWithProof() {
        // Given
        Contribution contribution = Contribution.new_(UUID.randomUUID(), 12345, 1000);
        String proofUrl = "https://example.com/proof.jpg";

        // When - Pay
        contribution.pay(proofUrl);

        // Then - Verify paid state
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
        assertThat(contribution.getPaymentProof()).isEqualTo(proofUrl);

        // When - Confirm
        contribution.confirm();

        // Then - Verify confirmed state
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.CONFIRMED);
        assertThat(contribution.getPaymentProof()).isEqualTo(proofUrl);
    }

    @Test
    @DisplayName("Should complete full workflow: pledged -> paid without proof -> confirmed")
    void shouldCompleteFullWorkflowWithoutProof() {
        // Given
        Contribution contribution = Contribution.new_(UUID.randomUUID(), 12345, 1000);

        // When - Pay without proof
        contribution.pay(null);

        // Then - Verify paid state
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
        assertThat(contribution.getPaymentProof()).isNull();

        // When - Confirm
        contribution.confirm();

        // Then - Verify confirmed state
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.CONFIRMED);
        assertThat(contribution.getPaymentProof()).isNull();
    }
}
