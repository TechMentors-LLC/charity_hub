package com.charity_hub.cases.internal.domain.model.Case;

import com.charity_hub.cases.internal.domain.exceptions.CaseExceptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CaseStatus Value Object Tests")
class CaseStatusTest {

    @Test
    @DisplayName("Should create status from enum value")
    void shouldCreateStatusFromEnumValue() {
        CaseStatus status = CaseStatus.of(Status.OPENED);

        assertThat(status.value()).isEqualTo(Status.OPENED);
        assertThat(status.isOpened()).isTrue();
        assertThat(status.isClosed()).isFalse();
        assertThat(status.isDraft()).isFalse();
    }

    @Test
    @DisplayName("Should transition from DRAFT to OPENED")
    void shouldTransitionFromDraftToOpened() {
        CaseStatus draft = CaseStatus.of(Status.DRAFT);

        CaseStatus opened = draft.open();

        assertThat(opened.isOpened()).isTrue();
    }

    @Test
    @DisplayName("Should transition from OPENED to CLOSED")
    void shouldTransitionFromOpenedToClosed() {
        CaseStatus opened = CaseStatus.of(Status.OPENED);

        CaseStatus closed = opened.close();

        assertThat(closed.isClosed()).isTrue();
    }

    @Test
    @DisplayName("Should throw when opening already opened status")
    void shouldThrowWhenOpeningAlreadyOpenedStatus() {
        CaseStatus opened = CaseStatus.of(Status.OPENED);

        assertThatThrownBy(opened::open)
                .isInstanceOf(CaseExceptions.CaseAlreadyOpenedException.class);
    }

    @Test
    @DisplayName("Should throw when closing already closed status")
    void shouldThrowWhenClosingAlreadyClosedStatus() {
        CaseStatus closed = CaseStatus.of(Status.CLOSED);

        assertThatThrownBy(closed::close)
                .isInstanceOf(CaseExceptions.CaseAlreadyClosedException.class);
    }

    @Test
    @DisplayName("Should correctly identify draft status")
    void shouldCorrectlyIdentifyDraftStatus() {
        CaseStatus draft = CaseStatus.of(Status.DRAFT);

        assertThat(draft.isDraft()).isTrue();
        assertThat(draft.isOpened()).isFalse();
        assertThat(draft.isClosed()).isFalse();
    }
}
