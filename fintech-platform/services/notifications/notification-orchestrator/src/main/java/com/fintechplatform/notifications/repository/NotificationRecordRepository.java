package com.fintechplatform.notifications.repository;

import com.fintechplatform.notifications.domain.NotificationRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRecordRepository extends JpaRepository<NotificationRecord, UUID> {

    boolean existsByEventId(UUID eventId);

    List<NotificationRecord> findTop50ByOrderByReceivedAtDesc();
}
