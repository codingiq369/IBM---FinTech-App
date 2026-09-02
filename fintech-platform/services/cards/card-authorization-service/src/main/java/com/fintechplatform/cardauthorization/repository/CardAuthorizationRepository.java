package com.fintechplatform.cardauthorization.repository;

import com.fintechplatform.cardauthorization.domain.CardAuthorization;
import com.fintechplatform.cardauthorization.domain.CardAuthorizationStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CardAuthorizationRepository extends JpaRepository<CardAuthorization, UUID> {

    List<CardAuthorization> findByCardIdOrderByCreatedAtDesc(UUID cardId);

    /** Sum of everything APPROVED for this card since {@code since} — the
     * running total {@link com.fintechplatform.cardauthorization.service.CardAuthorizationService}
     * checks a new purchase against for the card's daily limit. Recomputed
     * from history every time, the same "balance is derived, never cached"
     * philosophy ledger-service applies to account balances. */
    @Query("select coalesce(sum(a.amount), 0) from CardAuthorization a "
            + "where a.cardId = :cardId and a.status = :status and a.createdAt >= :since")
    BigDecimal sumApprovedAmountSince(
            @Param("cardId") UUID cardId, @Param("status") CardAuthorizationStatus status, @Param("since") Instant since);
}
