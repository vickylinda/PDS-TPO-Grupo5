package org.example.notifications.subscribers;
import org.example.notifications.NotificationService;
import org.example.notifications.bus.Subscriber;
import org.example.notifications.events.DomainEvent;
import org.example.notifications.TipoEvento;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class NotificationSubscriber implements Subscriber {

    private final NotificationService service;

    public NotificationSubscriber(NotificationService service) {
        this.service = service;
    }

    @Override
    public void onEvent(DomainEvent event) {
        List<String> destinatarios = extractRecipients(event.getPayload());
        var tipo = event.getEventType();
        var subject = buildSubject(tipo, event.getPayload());
        var html = buildHtml(tipo, event.getPayload());
        service.sendEmail(tipo, destinatarios, subject, html);
    }

    @Override
    public List<TipoEvento> getEventTypes() {
        // Te suscribís a todos los tipos que definiste
        return List.of(
                TipoEvento.SCRIM_CREADO,
                TipoEvento.LOBBY_ARMADO,
                TipoEvento.CONFIRMADO,
                TipoEvento.EN_JUEGO,
                TipoEvento.FINALIZADO,
                TipoEvento.CANCELADO
        );
    }

    private static List<String> extractRecipients(Map<String, String> payload) {
        List<String> result = new ArrayList<>();
        if (payload == null) return result;
        if (payload.containsKey("to") && !payload.get("to").isBlank()) {
            result.add(payload.get("to").trim());
        }
        if (payload.containsKey("toCsv") && !payload.get("toCsv").isBlank()) {
            result.addAll(Arrays.stream(payload.get("toCsv").split(","))
                    .map(String::trim).filter(s -> !s.isBlank()).toList());
        }
        return result;
    }

    private static String buildSubject(TipoEvento tipo, Map<String, String> p) {
        if (p != null && p.containsKey("subjectOverride") && !p.get("subjectOverride").isBlank()) {
            return p.get("subjectOverride");
        }
        String scrimId = (p != null) ? p.getOrDefault("scrimId", "scrim") : "scrim";
        return switch (tipo) {
            case SCRIM_CREADO -> "eScrims: Nuevo scrim creado (" + scrimId + ")";
            case LOBBY_ARMADO -> "eScrims: ¡Lobby completo! (" + scrimId + ")";
            case CONFIRMADO   -> "eScrims: Tu participación fue confirmada (" + scrimId + ")";
            case EN_JUEGO     -> "eScrims: La partida ha comenzado (" + scrimId + ")";
            case FINALIZADO   -> "eScrims: Resultado del scrim (" + scrimId + ")";
            case CANCELADO    -> "eScrims: Scrim cancelado (" + scrimId + ")";
        };
    }

    private static String buildHtml(TipoEvento tipo, Map<String, String> p) {
        if (p != null && p.containsKey("htmlOverride") && !p.get("htmlOverride").isBlank()) {
            return p.get("htmlOverride");
        }
        String scrimId   = (p != null) ? p.getOrDefault("scrimId", "scrim") : "scrim";
        String juego     = (p != null) ? p.getOrDefault("juego", "Juego") : "Juego";
        String region    = (p != null) ? p.getOrDefault("region", "REGION") : "REGION";
        String fechaHora = (p != null) ? p.getOrDefault("fechaHora", "fecha/hora") : "fecha/hora";

        return switch (tipo) {
            case SCRIM_CREADO -> """
                <h2>Nuevo scrim creado</h2>
                <p>Se creó un scrim para <b>%s</b> en <b>%s</b>.</p>
                <p><b>Inicio:</b> %s</p>
                <p><b>ID:</b> %s</p>
                """.formatted(juego, region, fechaHora, scrimId);

            case LOBBY_ARMADO -> """
                <h2>Lobby completo</h2>
                <p>Tu scrim <b>%s</b> ya tiene todos los jugadores.</p>
                <p>Prepará el equipo — Inicio previsto: %s</p>
                """.formatted(scrimId, fechaHora);

            case CONFIRMADO -> """
                <h2>Participación confirmada</h2>
                <p>Quedaste confirmado/a para el scrim <b>%s</b> (%s - %s).</p>
                <p>Nos vemos a las %s.</p>
                """.formatted(scrimId, juego, region, fechaHora);

            case EN_JUEGO -> """
                <h2>¡La partida comenzó!</h2>
                <p>Scrim <b>%s</b> en curso.</p>
                """.formatted(scrimId);

            case FINALIZADO -> {
                String ganador = (p != null) ? p.getOrDefault("ganador", "—") : "—";
                yield """
                    <h2>Scrim finalizado</h2>
                    <p>Resultado para <b>%s</b>:</p>
                    <p><b>Ganador:</b> %s</p>
                    """.formatted(scrimId, ganador);
            }

            case CANCELADO -> """
                <h2>Scrim cancelado</h2>
                <p>Lamentablemente el scrim <b>%s</b> fue cancelado.</p>
                """.formatted(scrimId);
        };
    }
}
