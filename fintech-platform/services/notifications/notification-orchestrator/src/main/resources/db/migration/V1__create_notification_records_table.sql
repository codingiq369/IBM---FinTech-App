CREATE TABLE notification_records (
    id UUID PRIMARY KEY,
    event_id UUID NOT NULL,
    event_type VARCHAR(60) NOT NULL,
    reference_id UUID NOT NULL,
    summary VARCHAR(500) NOT NULL,
    payload TEXT NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_notification_records_event_id UNIQUE (event_id)
);

CREATE INDEX idx_notification_records_received_at ON notification_records (received_at DESC);
CREATE INDEX idx_notification_records_reference_id ON notification_records (reference_id);
