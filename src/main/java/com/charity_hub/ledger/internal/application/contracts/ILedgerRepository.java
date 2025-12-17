package com.charity_hub.ledger.internal.application.contracts;

import com.charity_hub.ledger.internal.domain.model.Ledger;
import com.charity_hub.ledger.internal.domain.model.LedgerId;
import com.charity_hub.ledger.internal.domain.model.MemberId;

/**
 * Repository interface for Ledger aggregate.
 * Provides persistence and retrieval operations for ledger state.
 */
public interface ILedgerRepository {

    /**
     * Finds a ledger by its ID.
     * 
     * @param id Ledger ID
     * @return Ledger if found, null otherwise
     */
    Ledger findById(LedgerId id);

    /**
     * Finds a ledger by member ID.
     * 
     * @param memberId Member ID
     * @return Ledger if found, null otherwise
     */
    Ledger findByMemberId(MemberId memberId);

    /**
     * Saves a ledger.
     * 
     * @param ledger Ledger to save
     */
    void save(Ledger ledger);

    /**
     * Checks if a ledger exists for a member.
     * 
     * @param memberId Member ID
     * @return true if exists, false otherwise
     */
    boolean existsByMemberId(MemberId memberId);
}
