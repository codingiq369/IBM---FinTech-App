package com.fintechplatform.ledger.repository;

import com.fintechplatform.ledger.domain.JournalEntry;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {}
