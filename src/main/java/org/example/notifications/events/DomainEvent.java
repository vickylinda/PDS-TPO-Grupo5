package org.example.notifications.events;
import org.example.notifications.TipoEvento;
import java.time.LocalDateTime;
import java.util.Map;

public interface DomainEvent {
    TipoEvento getEventType();
    String getAggregateId();                // id del scrim u otro agregado
    LocalDateTime getTimestamp();
    Map<String,String> getPayload();        // parametros dinámicos (asunto, cuerpo)
}
