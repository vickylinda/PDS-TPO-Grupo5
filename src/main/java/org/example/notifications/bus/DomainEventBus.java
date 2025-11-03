package org.example.notifications.bus;

import org.example.notifications.TipoEvento;
import org.example.notifications.events.DomainEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class DomainEventBus {

    private static final DomainEventBus INSTANCE = new DomainEventBus();

    // Seguro para concurrencia: cada tipo de evento tiene una lista thread-safe de subscriptores
    private final Map<TipoEvento, Set<Subscriber>> subs =
            new ConcurrentHashMap<>();

    private DomainEventBus() {}

    public static DomainEventBus getInstance() {
        return INSTANCE;
    }

    /** Registra un subscriber para TODOS los tipos que devuelve getEventTypes() */
    public synchronized void subscribe(Subscriber s) {
        for (var t : s.getEventTypes()) {
            subs.computeIfAbsent(t, k -> new LinkedHashSet<>()).add(s);
        }
    }

    /** Desregistra el subscriber de TODOS los tipos donde esté inscripto */
    public synchronized void unsubscribe(Subscriber s) {
        for (var set : subs.values()) set.remove(s);
    }

    /** Publica el evento a todos los suscriptores del tipo; evita que un error de uno corte a los demás */
    public void publish(DomainEvent event) {
        var set = subs.getOrDefault(event.getEventType(), Set.of());
        var payload = event.getPayload();
        int n = set.size();
        int toCount = 0;
        if (payload != null) {
            if (payload.get("to") != null && !payload.get("to").isBlank()) toCount++;
            if (payload.get("toCsv") != null && !payload.get("toCsv").isBlank())
                toCount += (int) Arrays.stream(payload.get("toCsv").split(","))
                        .map(String::trim).filter(s -> !s.isBlank()).count();
        }
        System.out.printf("[EventBus] publish %s agg=%s subs=%d recipients≈%d%n",
                event.getEventType(), event.getAggregateId(), n, toCount);

        for (var s : set) s.onEvent(event);
    }
}
