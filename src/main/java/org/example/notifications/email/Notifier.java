package org.example.notifications.email;
import org.example.notifications.model.Notificacion;

public interface Notifier {
    boolean send(Notificacion n) throws Exception;
}
