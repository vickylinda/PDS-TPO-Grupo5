package org.example.notifications;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.example.notifications.TipoEvento;
import org.example.notifications.model.Notificacion;
import org.example.notifications.repo.NotificationRepository;

import java.io.IOException;
import java.util.List;

public class NotificationService {
    private final NotificationRepository repo;
    private final SendGrid sg;
    private final String fromEmail;
    private final String fromName; // "eScrims Platform"

    public NotificationService(NotificationRepository repo, String sendgridApiKey, String fromEmail, String fromName) {
        this.repo = repo;
        this.sg = new SendGrid(sendgridApiKey);
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public Notificacion sendEmail(TipoEvento tipo, List<String> toList, String subject, String html) {
        Notificacion n = new Notificacion(tipo, toList, subject, html);
        repo.save(n);

        Mail mail = new Mail();
        Email from = new Email(fromEmail, fromName);
        mail.setFrom(from);
        mail.setSubject(subject);

        Personalization p = new Personalization();
        for (String to : toList) {
            p.addTo(new Email(to));
        }
        Content content = new Content("text/html", html);
        mail.addContent(content);
        mail.addPersonalization(p);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                n.setEstado("SENT");
            } else {
                n.setEstado("ERROR");
                n.setErrorMsg("HTTP " + response.getStatusCode() + " - " + response.getBody());
            }
        } catch (IOException ex) {
            n.setEstado("ERROR");
            n.setErrorMsg(ex.getMessage());
        }
        repo.update(n);
        return n;
    }
}
