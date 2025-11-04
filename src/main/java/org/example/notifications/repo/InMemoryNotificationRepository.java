package org.example.notifications.repo;
import org.example.notifications.model.Notificacion;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryNotificationRepository implements NotificationRepository {
    private final Map<String, Notificacion> db = new ConcurrentHashMap<>();

    public void save(Notificacion n)   { db.put(n.getId(), n); }
    public void update(Notificacion n) { db.put(n.getId(), n); }
    public Optional<Notificacion> findById(String id){ return Optional.ofNullable(db.get(id)); }
    public List<Notificacion> findAll(){ return new ArrayList<>(db.values()); }
}
