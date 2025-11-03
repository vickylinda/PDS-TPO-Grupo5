package org.example.notifications.email;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

public class SendGridAdapter {
    private final String apiKey;
    private final String fromEmail;
    private final String fromName; // "eScrims Platform"

    public SendGridAdapter(String apiKey, String fromEmail, String fromName) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public boolean enviarEmail(String destinatario, String asunto, String cuerpoHtml) throws Exception {
        Email from = new Email(fromEmail, fromName);
        Email to   = new Email(destinatario);
        Content content = new Content("text/html", cuerpoHtml);
        Mail mail = new Mail(from, asunto, to, content);

        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);
        int code = response.getStatusCode();
        return code >= 200 && code < 300;
    }
}
