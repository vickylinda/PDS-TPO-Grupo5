package org.example.notifications.email;
import org.example.notifications.model.Notificacion;

public class EmailNotifier implements Notifier {
    private final SendGridAdapter adapter;

    public EmailNotifier(SendGridAdapter adapter) { this.adapter = adapter; }

    @Override
    public boolean send(Notificacion n) throws Exception {
        boolean allOk = true;

        for (String to : n.getDestinatarios()) {
            boolean ok = adapter.enviarEmail(to, n.getAsunto(), n.getCuerpoHtml());
            if (!ok) {
                allOk = false;
                n.setEstado("ERROR");
                n.setErrorMsg("Fallo envío a: " + to);
            }
        }

        if (allOk) n.setEstado("ENVIADO");
        return allOk;
    }
}
