package com.fintechplatform.cardmanagement.repository;

import com.fintechplatform.cardmanagement.domain.Card;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByAccountId(UUID accountId);

    List<Card> findByCustomerId(UUID customerId);
}
