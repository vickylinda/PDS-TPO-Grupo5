package org.example.notifications.events;
import org.example.notifications.TipoEvento;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

public abstract class BaseEvent implements DomainEvent {
    private final TipoEvento type;
    private final String aggregateId;
    private final LocalDateTime ts;
    private final Map<String,String> payload;

    protected BaseEvent(TipoEvento type, String aggregateId, Map<String,String> payload) {
        this.type = type;
        this.aggregateId = aggregateId;
        this.ts = LocalDateTime.now();
        this.payload = (payload == null) ? Collections.emptyMap() : Map.copyOf(payload);
    }
    public TipoEvento getEventType() { return type; }
    public String getAggregateId() { return aggregateId; }
    public LocalDateTime getTimestamp() { return ts; }
    public Map<String, String> getPayload() { return payload; }
}
