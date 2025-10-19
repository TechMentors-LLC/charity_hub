package com.charity_hub.cases.internal.infrastructure.repositories.mappers;

import com.charity_hub.cases.internal.domain.model.Contribution.Contribution;
import com.charity_hub.cases.internal.domain.model.Contribution.ContributionStatus;
import com.charity_hub.cases.internal.infrastructure.db.ContributionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Contribution Mapper Tests")
class ContributionMapperTest {

    private ContributionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ContributionMapper();
    }

    // ============= TO DB TESTS =============

    @Test
    @DisplayName("Should map domain contribution to entity with proof URL")
    void shouldMapDomainToEntityWithProofUrl() {
        // Given
        UUID id = UUID.randomUUID();
        UUID contributorId = UUID.randomUUID();
        int caseCode = 12345;
        int amount = 1000;
        String proofUrl = "https://example.com/proof.jpg";
        Date date = new Date();

        Contribution contribution = Contribution.create(
                id, contributorId, caseCode, amount,
                ContributionStatus.PAID, date, proofUrl
        );

        // When
        ContributionEntity entity = mapper.toDB(contribution);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity._id()).isEqualTo(id.toString());
        assertThat(entity.contributorId()).isEqualTo(contributorId.toString());
        assertThat(entity.caseCode()).isEqualTo(caseCode);
        assertThat(entity.amount()).isEqualTo(amount);
        assertThat(entity.status()).isEqualTo(ContributionEntity.STATUS_PAID);
        assertThat(entity.contributionDate()).isEqualTo(date.getTime());
        assertThat(entity.paymentProof()).isEqualTo(proofUrl);
    }

    @Test
    @DisplayName("Should map domain contribution to entity without proof URL")
    void shouldMapDomainToEntityWithoutProofUrl() {
        // Given
        UUID id = UUID.randomUUID();
        UUID contributorId = UUID.randomUUID();
        int caseCode = 12345;
        int amount = 1000;
        Date date = new Date();

        Contribution contribution = Contribution.create(
                id, contributorId, caseCode, amount,
                ContributionStatus.PLEDGED, date, null
        );

        // When
        ContributionEntity entity = mapper.toDB(contribution);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.paymentProof()).isNull();
    }

    @Test
    @DisplayName("Should map pledged contribution status correctly")
    void shouldMapPledgedStatusCorrectly() {
        // Given
        Contribution contribution = Contribution.new_(UUID.randomUUID(), 12345, 1000);

        // When
        ContributionEntity entity = mapper.toDB(contribution);

        // Then
        assertThat(entity.status()).isEqualTo(ContributionEntity.STATUS_PLEDGED);
    }

    @Test
    @DisplayName("Should map paid contribution status correctly")
    void shouldMapPaidStatusCorrectly() {
        // Given
        Contribution contribution = Contribution.create(
                UUID.randomUUID(), UUID.randomUUID(), 12345, 1000,
                ContributionStatus.PAID, new Date(), "https://proof.com"
        );

        // When
        ContributionEntity entity = mapper.toDB(contribution);

        // Then
        assertThat(entity.status()).isEqualTo(ContributionEntity.STATUS_PAID);
    }

    @Test
    @DisplayName("Should map confirmed contribution status correctly")
    void shouldMapConfirmedStatusCorrectly() {
        // Given
        Contribution contribution = Contribution.create(
                UUID.randomUUID(), UUID.randomUUID(), 12345, 1000,
                ContributionStatus.CONFIRMED, new Date(), "https://proof.com"
        );

        // When
        ContributionEntity entity = mapper.toDB(contribution);

        // Then
        assertThat(entity.status()).isEqualTo(ContributionEntity.STATUS_CONFIRMED);
    }

    // ============= TO DOMAIN TESTS =============

    @Test
    @DisplayName("Should map entity to domain contribution with proof URL")
    void shouldMapEntityToDomainWithProofUrl() {
        // Given
        String id = UUID.randomUUID().toString();
        String contributorId = UUID.randomUUID().toString();
        int caseCode = 12345;
        int amount = 1000;
        int status = ContributionEntity.STATUS_PAID;
        long date = new Date().getTime();
        String proofUrl = "https://example.com/proof.jpg";

        ContributionEntity entity = new ContributionEntity(
                id, contributorId, caseCode, amount, status, date, proofUrl
        );

        // When
        Contribution contribution = mapper.toDomain(entity);

        // Then
        assertThat(contribution).isNotNull();
        assertThat(contribution.getId().value().toString()).isEqualTo(id);
        assertThat(contribution.getContributorId().toString()).isEqualTo(contributorId);
        assertThat(contribution.getCaseId().value()).isEqualTo(caseCode);
        assertThat(contribution.getMoneyValue().value()).isEqualTo(amount);
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
        assertThat(contribution.getContributionDate().getTime()).isEqualTo(date);
        assertThat(contribution.getPaymentProof()).isEqualTo(proofUrl);
    }

    @Test
    @DisplayName("Should map entity to domain contribution without proof URL")
    void shouldMapEntityToDomainWithoutProofUrl() {
        // Given
        String id = UUID.randomUUID().toString();
        String contributorId = UUID.randomUUID().toString();
        int caseCode = 12345;
        int amount = 1000;
        int status = ContributionEntity.STATUS_PLEDGED;
        long date = new Date().getTime();

        ContributionEntity entity = new ContributionEntity(
                id, contributorId, caseCode, amount, status, date, null
        );

        // When
        Contribution contribution = mapper.toDomain(entity);

        // Then
        assertThat(contribution).isNotNull();
        assertThat(contribution.getPaymentProof()).isNull();
    }

    @Test
    @DisplayName("Should map pledged status from entity to domain")
    void shouldMapPledgedStatusFromEntityToDomain() {
        // Given
        ContributionEntity entity = new ContributionEntity(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                12345, 1000,
                ContributionEntity.STATUS_PLEDGED,
                new Date().getTime(),
                null
        );

        // When
        Contribution contribution = mapper.toDomain(entity);

        // Then
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PLEDGED);
    }

    @Test
    @DisplayName("Should map paid status from entity to domain")
    void shouldMapPaidStatusFromEntityToDomain() {
        // Given
        ContributionEntity entity = new ContributionEntity(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                12345, 1000,
                ContributionEntity.STATUS_PAID,
                new Date().getTime(),
                "https://proof.com"
        );

        // When
        Contribution contribution = mapper.toDomain(entity);

        // Then
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.PAID);
    }

    @Test
    @DisplayName("Should map confirmed status from entity to domain")
    void shouldMapConfirmedStatusFromEntityToDomain() {
        // Given
        ContributionEntity entity = new ContributionEntity(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                12345, 1000,
                ContributionEntity.STATUS_CONFIRMED,
                new Date().getTime(),
                "https://proof.com"
        );

        // When
        Contribution contribution = mapper.toDomain(entity);

        // Then
        assertThat(contribution.getContributionStatus()).isEqualTo(ContributionStatus.CONFIRMED);
    }

    // ============= ROUNDTRIP TESTS =============

    @Test
    @DisplayName("Should maintain proof URL through roundtrip conversion")
    void shouldMaintainProofUrlThroughRoundtrip() {
        // Given
        UUID id = UUID.randomUUID();
        String proofUrl = "https://example.com/proof.jpg";
        Contribution originalContribution = Contribution.create(
                id, UUID.randomUUID(), 12345, 1000,
                ContributionStatus.PAID, new Date(), proofUrl
        );

        // When - Convert to entity and back
        ContributionEntity entity = mapper.toDB(originalContribution);
        Contribution roundtripContribution = mapper.toDomain(entity);

        // Then
        assertThat(roundtripContribution.getId().value()).isEqualTo(originalContribution.getId().value());
        assertThat(roundtripContribution.getPaymentProof()).isEqualTo(originalContribution.getPaymentProof());
        assertThat(roundtripContribution.getContributionStatus()).isEqualTo(originalContribution.getContributionStatus());
    }

    @Test
    @DisplayName("Should maintain null proof URL through roundtrip conversion")
    void shouldMaintainNullProofUrlThroughRoundtrip() {
        // Given
        UUID id = UUID.randomUUID();
        Contribution originalContribution = Contribution.create(
                id, UUID.randomUUID(), 12345, 1000,
                ContributionStatus.PLEDGED, new Date(), null
        );

        // When - Convert to entity and back
        ContributionEntity entity = mapper.toDB(originalContribution);
        Contribution roundtripContribution = mapper.toDomain(entity);

        // Then
        assertThat(roundtripContribution.getId().value()).isEqualTo(originalContribution.getId().value());
        assertThat(roundtripContribution.getPaymentProof()).isNull();
        assertThat(originalContribution.getPaymentProof()).isNull();
    }
}
