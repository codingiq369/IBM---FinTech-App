package com.fintechplatform.notifications.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fintechplatform.notifications.event.ParsedTransactionEvent;
import com.fintechplatform.notifications.repository.NotificationRecordRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRecordRepository repository;

    @Test
    void aNewEventIsSaved() {
        NotificationService service = new NotificationService(repository);
        ParsedTransactionEvent event =
                new ParsedTransactionEvent(UUID.randomUUID(), "TransferCompleted", UUID.randomUUID(), "Transfer of 25.00 USD completed");
        when(repository.existsByEventId(event.eventId())).thenReturn(false);

        service.recordEvent(event, "{}");

        verify(repository).save(any());
    }

    @Test
    void anAlreadyRecordedEventIdIsNotSavedTwice() {
        NotificationService service = new NotificationService(repository);
        ParsedTransactionEvent event =
                new ParsedTransactionEvent(UUID.randomUUID(), "TransferCompleted", UUID.randomUUID(), "Transfer of 25.00 USD completed");
        when(repository.existsByEventId(event.eventId())).thenReturn(true);

        service.recordEvent(event, "{}");

        verify(repository, never()).save(any());
    }
}
