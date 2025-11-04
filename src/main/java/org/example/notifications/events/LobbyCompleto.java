package org.example.notifications.events;
import org.example.notifications.TipoEvento;
import java.util.Map;

public class LobbyCompleto extends BaseEvent {
    public LobbyCompleto(String scrimId, Map<String,String> payload) {
        super(TipoEvento.LOBBY_ARMADO, scrimId, payload);
    }
}
