package org.example.notifications.events;
import org.example.notifications.TipoEvento;
import java.util.Map;

public class ScrimConfirmado extends BaseEvent {
    public ScrimConfirmado(String scrimId, Map<String,String> payload) {
        super(TipoEvento.CONFIRMADO, scrimId, payload);
    }
}
