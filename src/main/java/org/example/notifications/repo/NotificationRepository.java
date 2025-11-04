package org.example.notifications.repo;
import org.example.notifications.model.Notificacion;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository {
    void save(Notificacion n);
    void update(Notificacion n);
    Optional<Notificacion> findById(String id);
    List<Notificacion> findAll();
}
