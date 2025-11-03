package org.example.notifications.events;
import org.example.notifications.TipoEvento;
import java.util.Map;

public class ScrimCancelado extends BaseEvent {
    public ScrimCancelado(String scrimId, Map<String,String> payload) {
        super(TipoEvento.CANCELADO, scrimId, payload);
    }
}

