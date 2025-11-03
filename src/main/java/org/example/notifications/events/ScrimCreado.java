package org.example.notifications.events;
import org.example.notifications.TipoEvento;
import java.util.Map;

public class ScrimCreado extends BaseEvent {
    public ScrimCreado(String scrimId, Map<String,String> payload) {
        super(TipoEvento.SCRIM_CREADO, scrimId, payload);
    }
}
