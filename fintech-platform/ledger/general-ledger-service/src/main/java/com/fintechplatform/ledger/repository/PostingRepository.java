package com.fintechplatform.ledger.repository;

import com.fintechplatform.ledger.domain.Direction;
import com.fintechplatform.ledger.domain.Posting;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostingRepository extends JpaRepository<Posting, UUID> {

    @Query("select coalesce(sum(p.amount), 0) from Posting p "
            + "where p.ledgerAccountId = :ledgerAccountId and p.direction = :direction")
    BigDecimal sumAmountByLedgerAccountIdAndDirection(@Param("ledgerAccountId") UUID ledgerAccountId, @Param("direction") Direction direction);

    List<Posting> findByLedgerAccountIdOrderByCreatedAtDesc(UUID ledgerAccountId);
}
