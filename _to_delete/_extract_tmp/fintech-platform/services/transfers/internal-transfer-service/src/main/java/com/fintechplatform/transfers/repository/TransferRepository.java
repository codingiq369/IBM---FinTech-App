package com.fintechplatform.transfers.repository;

import com.fintechplatform.transfers.domain.Transfer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
    List<Transfer> findBySourceAccountIdOrDestinationAccountIdOrderByCreatedAtDesc(UUID sourceAccountId, UUID destinationAccountId);
}
