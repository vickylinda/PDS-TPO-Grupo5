package org.example.notifications.events;
import org.example.notifications.TipoEvento;
import java.util.Map;

public class ScrimFinalizado extends BaseEvent {
    public ScrimFinalizado(String scrimId, Map<String,String> payload) {
        super(TipoEvento.FINALIZADO, scrimId, payload);
    }
}
