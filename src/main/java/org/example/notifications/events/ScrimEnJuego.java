package org.example.notifications.events;
import org.example.notifications.TipoEvento;
import java.util.Map;

public class ScrimEnJuego extends BaseEvent {
    public ScrimEnJuego(String scrimId, Map<String,String> payload) {
        super(TipoEvento.EN_JUEGO, scrimId, payload);
    }
}
