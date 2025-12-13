package com.charity_hub.ledger.internal.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Ledger Domain Model Tests")
class LedgerTest {

    private final UUID MEMBER_UUID = UUID.randomUUID();
    private final MemberId MEMBER_ID = new MemberId(MEMBER_UUID);

    @Nested
    @DisplayName("When creating new ledger")
    class LedgerCreation {

        @Test
        @DisplayName("createNew should create ledger with zero balances")
        void shouldCreateLedgerWithZeroBalances() {
            Ledger ledger = Ledger.createNew(MEMBER_ID);

            assertThat(ledger.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(ledger.getDueAmount().value()).isZero();
            assertThat(ledger.getDueNetworkAmount().value()).isZero();
            assertThat(ledger.getTransactions()).isEmpty();
        }

        @Test
        @DisplayName("Should throw exception when memberId is null")
        void shouldThrowExceptionWhenMemberIdNull() {
            assertThatThrownBy(() -> new Ledger(
                    LedgerId.fromMemberId(MEMBER_ID),
                    null,
                    Amount.forMember(0),
                    Amount.forNetwork(0),
                    Collections.emptyList()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("MemberId cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when dueAmount is null")
        void shouldThrowExceptionWhenDueAmountNull() {
            assertThatThrownBy(() -> new Ledger(
                    LedgerId.fromMemberId(MEMBER_ID),
                    MEMBER_ID,
                    null,
                    Amount.forNetwork(0),
                    Collections.emptyList()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DueAmount cannot be null");
        }

        @Test
        @DisplayName("Should throw exception when dueNetworkAmount is null")
        void shouldThrowExceptionWhenDueNetworkAmountNull() {
            assertThatThrownBy(() -> new Ledger(
                    LedgerId.fromMemberId(MEMBER_ID),
                    MEMBER_ID,
                    Amount.forMember(0),
                    null,
                    Collections.emptyList()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("DueNetworkAmount cannot be null");
        }
    }

    @Nested
    @DisplayName("When crediting/debiting due amount")
    class DueAmountOperations {

        @Test
        @DisplayName("creditDueAmount should increase dueAmount")
        void shouldCreditDueAmount() {
            Ledger ledger = Ledger.createNew(MEMBER_ID);
            Service service = createService();
            Amount amount = Amount.forMember(100);

            ledger.creditDueAmount(amount, service);

            assertThat(ledger.getDueAmount().value()).isEqualTo(100);
        }

        @Test
        @DisplayName("debitDueAmount should decrease dueAmount")
        void shouldDebitDueAmount() {
            Ledger ledger = new Ledger(
                    LedgerId.fromMemberId(MEMBER_ID),
                    MEMBER_ID,
                    Amount.forMember(200),
                    Amount.forNetwork(0),
                    Collections.emptyList());
            Service service = createService();
            Amount amount = Amount.forMember(50);

            ledger.debitDueAmount(amount, service);

            assertThat(ledger.getDueAmount().value()).isEqualTo(150);
        }

        @Test
        @DisplayName("Should throw exception when crediting with wrong amount type")
        void shouldThrowExceptionForWrongCreditAmountType() {
            Ledger ledger = Ledger.createNew(MEMBER_ID);
            Service service = createService();
            Amount networkAmount = Amount.forNetwork(100);

            assertThatThrownBy(() -> ledger.creditDueAmount(networkAmount, service))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid amount type");
        }

        @Test
        @DisplayName("Should throw exception when debiting with wrong amount type")
        void shouldThrowExceptionForWrongDebitAmountType() {
            Ledger ledger = new Ledger(
                    LedgerId.fromMemberId(MEMBER_ID),
                    MEMBER_ID,
                    Amount.forMember(200),
                    Amount.forNetwork(0),
                    Collections.emptyList());
            Service service = createService();
            Amount networkAmount = Amount.forNetwork(100);

            assertThatThrownBy(() -> ledger.debitDueAmount(networkAmount, service))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid amount type");
        }

        @Test
        @DisplayName("creditDueAmount should create transaction")
        void shouldCreateTransactionOnCredit() {
            Ledger ledger = Ledger.createNew(MEMBER_ID);
            Service service = createService();
            Amount amount = Amount.forMember(100);

            ledger.creditDueAmount(amount, service);

            assertThat(ledger.getTransactions()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("When crediting/debiting network amount")
    class NetworkAmountOperations {

        @Test
        @DisplayName("creditNetworkAmount should increase dueNetworkAmount")
        void shouldCreditNetworkAmount() {
            Ledger ledger = Ledger.createNew(MEMBER_ID);
            Service service = createService();
            Amount amount = Amount.forNetwork(100);

            ledger.creditNetworkAmount(amount, service);

            assertThat(ledger.getDueNetworkAmount().value()).isEqualTo(100);
        }

        @Test
        @DisplayName("debitNetworkAmount should decrease dueNetworkAmount")
        void shouldDebitNetworkAmount() {
            Ledger ledger = new Ledger(
                    LedgerId.fromMemberId(MEMBER_ID),
                    MEMBER_ID,
                    Amount.forMember(0),
                    Amount.forNetwork(200),
                    Collections.emptyList());
            Service service = createService();
            Amount amount = Amount.forNetwork(50);

            ledger.debitNetworkAmount(amount, service);

            assertThat(ledger.getDueNetworkAmount().value()).isEqualTo(150);
        }

        @Test
        @DisplayName("Should throw exception when crediting network with wrong amount type")
        void shouldThrowExceptionForWrongNetworkCreditAmountType() {
            Ledger ledger = Ledger.createNew(MEMBER_ID);
            Service service = createService();
            Amount memberAmount = Amount.forMember(100);

            assertThatThrownBy(() -> ledger.creditNetworkAmount(memberAmount, service))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid amount type");
        }
    }

    @Nested
    @DisplayName("When converting network amount to due amount")
    class ConvertToDueAmount {

        @Test
        @DisplayName("convertToDueAmount should credit due and debit network")
        void shouldConvertNetworkToDue() {
            Ledger ledger = new Ledger(
                    LedgerId.fromMemberId(MEMBER_ID),
                    MEMBER_ID,
                    Amount.forMember(100),
                    Amount.forNetwork(300),
                    Collections.emptyList());
            Service service = createService();
            Amount amount = Amount.forMember(100);

            ledger.convertToDueAmount(amount, service);

            assertThat(ledger.getDueAmount().value()).isEqualTo(200); // 100 + 100
            assertThat(ledger.getDueNetworkAmount().value()).isEqualTo(200); // 300 - 100
        }

        @Test
        @DisplayName("convertToDueAmount should create two transactions")
        void shouldCreateTwoTransactionsOnConvert() {
            Ledger ledger = new Ledger(
                    LedgerId.fromMemberId(MEMBER_ID),
                    MEMBER_ID,
                    Amount.forMember(100),
                    Amount.forNetwork(300),
                    Collections.emptyList());
            Service service = createService();
            Amount amount = Amount.forMember(100);

            ledger.convertToDueAmount(amount, service);

            assertThat(ledger.getTransactions()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("Transaction tracking")
    class TransactionTracking {

        @Test
        @DisplayName("Should track transactions immutably")
        void shouldReturnUnmodifiableTransactionList() {
            Ledger ledger = Ledger.createNew(MEMBER_ID);
            Service service = createService();
            Amount amount = Amount.forMember(100);

            ledger.creditDueAmount(amount, service);

            assertThatThrownBy(() -> ledger.getTransactions().add(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("Multiple operations should accumulate transactions")
        void shouldAccumulateTransactions() {
            Ledger ledger = Ledger.createNew(MEMBER_ID);
            Service service = createService();

            ledger.creditDueAmount(Amount.forMember(100), service);
            ledger.creditNetworkAmount(Amount.forNetwork(50), service);
            ledger.debitDueAmount(Amount.forMember(25), service);

            assertThat(ledger.getTransactions()).hasSize(3);
        }
    }

    private Service createService() {
        return new Service(
                ServiceType.CONTRIBUTION,
                ServiceTransactionId.from(UUID.randomUUID()));
    }
}
