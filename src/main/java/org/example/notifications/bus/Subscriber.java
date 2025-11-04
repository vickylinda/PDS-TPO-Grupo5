package org.example.notifications.bus;

import org.example.notifications.TipoEvento;
import org.example.notifications.events.DomainEvent;

import java.util.List;

public interface Subscriber {
    void onEvent(DomainEvent event);
    List<TipoEvento> getEventTypes();
}
