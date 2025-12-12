package com.charity_hub.ledger.internal.domain.model;

import com.charity_hub.ledger.internal.domain.events.DueAmountChanged;
import com.charity_hub.ledger.internal.domain.events.NetworkDueAmountChanged;
import com.charity_hub.ledger.internal.domain.events.TransactionCreated;
import com.charity_hub.shared.domain.model.AggregateRoot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ledger aggregate root managing member balances and transactions.
 * 
 * Tracks two types of amounts:
 * - dueAmount: What this member owes/should send UP to their parent
 * - dueNetworkAmount: What this member expects to receive from their network
 * (children/descendants)
 * 
 * Business rules:
 * - When ContributionConfirmed: Credit contributor's dueAmount and parent's
 * dueNetworkAmount
 * - When ContributionPaid: Debit contributor's dueAmount and parent's
 * dueNetworkAmount, Credit parent's dueAmount
 */
public class Ledger extends AggregateRoot<LedgerId> {

    private final MemberId memberId;
    private Amount dueAmount;
    private Amount dueNetworkAmount;
    private final List<Transaction> transactions;

    public Ledger(
            LedgerId id,
            MemberId memberId,
            Amount dueAmount,
            Amount dueNetworkAmount,
            List<Transaction> transactions) {
        super(id);
        if (memberId == null) {
            throw new IllegalArgumentException("MemberId cannot be null");
        }
        if (dueAmount == null) {
            throw new IllegalArgumentException("DueAmount cannot be null");
        }
        if (dueNetworkAmount == null) {
            throw new IllegalArgumentException("DueNetworkAmount cannot be null");
        }

        this.memberId = memberId;
        this.dueAmount = dueAmount;
        this.dueNetworkAmount = dueNetworkAmount;
        this.transactions = transactions != null ? new ArrayList<>(transactions) : new ArrayList<>();
    }

    /**
     * Creates a new ledger for a member with zero balances.
     */
    public static Ledger createNew(MemberId memberId) {
        return new Ledger(
                LedgerId.fromMemberId(memberId),
                memberId,
                Amount.forMember(0),
                Amount.forNetwork(0),
                new ArrayList<>());
    }

    /**
     * Debits the member's due amount (reduces what they owe to parent).
     * Called when a member pays their obligation.
     */
    public void debitDueAmount(Amount amount, Service service) {
        validateAmountType(amount, AmountType.MEMBER_DUE_AMOUNT);
        this.dueAmount = this.dueAmount.minus(amount);
        raiseEvent(new DueAmountChanged(getId(), this.dueAmount));
        addDebitTransaction(service, amount);
    }

    /**
     * Credits the member's due amount (increases what they owe to parent).
     * Called when a member confirms they will contribute.
     */
    public void creditDueAmount(Amount amount, Service service) {
        validateAmountType(amount, AmountType.MEMBER_DUE_AMOUNT);
        this.dueAmount = this.dueAmount.plus(amount);
        raiseEvent(new DueAmountChanged(getId(), this.dueAmount));
        addCreditTransaction(service, amount);
    }

    /**
     * Debits the network due amount (reduces what this member expects from
     * network).
     * Called when a child pays and the parent receives the funds.
     */
    public void debitNetworkAmount(Amount amount, Service service) {
        validateAmountType(amount, AmountType.NETWORK_DUE_AMOUNT);
        this.dueNetworkAmount = this.dueNetworkAmount.minus(amount);
        raiseEvent(new NetworkDueAmountChanged(getId(), this.dueNetworkAmount));
        addDebitTransaction(service, amount);
    }

    /**
     * Credits the network due amount (increases what this member expects from
     * network).
     * Called when a child confirms they will contribute.
     */
    public void creditNetworkAmount(Amount amount, Service service) {
        validateAmountType(amount, AmountType.NETWORK_DUE_AMOUNT);
        this.dueNetworkAmount = this.dueNetworkAmount.plus(amount);
        raiseEvent(new NetworkDueAmountChanged(getId(), this.dueNetworkAmount));
        addCreditTransaction(service, amount);
    }

    /**
     * Converts network amount to due amount.
     * Used when aggregating network funds to forward to parent.
     */
    public void convertToDueAmount(Amount amount, Service service) {
        // Validate both amounts
        Amount memberAmount = Amount.forMember(amount.value());
        Amount networkAmount = Amount.forNetwork(amount.value());

        // Credit due amount (member now owes to parent)
        this.dueAmount = this.dueAmount.plus(memberAmount);
        raiseEvent(new DueAmountChanged(getId(), this.dueAmount));
        addCreditTransaction(service, memberAmount);

        // Debit network amount (funds collected from network)
        this.dueNetworkAmount = this.dueNetworkAmount.minus(networkAmount);
        raiseEvent(new NetworkDueAmountChanged(getId(), this.dueNetworkAmount));
        addDebitTransaction(service, networkAmount);
    }

    private void addCreditTransaction(Service service, Amount amount) {
        Transaction transaction = Transaction.createCredit(this.memberId, service, amount);
        transactions.add(transaction);
    }

    private void addDebitTransaction(Service service, Amount amount) {
        Transaction transaction = Transaction.createDebit(this.memberId, service, amount);
        transactions.add(transaction);
        raiseEvent(new TransactionCreated(transaction));
    }

    private void validateAmountType(Amount amount, AmountType expectedType) {
        if (amount.type() != expectedType) {
            throw new IllegalArgumentException(
                    String.format("Invalid amount type. Expected %s but got %s", expectedType, amount.type()));
        }
    }

    // Getters
    public MemberId getMemberId() {
        return memberId;
    }

    public Amount getDueAmount() {
        return dueAmount;
    }

    public Amount getDueNetworkAmount() {
        return dueNetworkAmount;
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
}
