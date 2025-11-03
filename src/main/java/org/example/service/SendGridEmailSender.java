//NOTA: esta clase la hice para probar el envio de los mails. no forma parte del tp en si.
package org.example.service;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class SendGridEmailSender {
    private final String apiKey;
    private final String fromAddress;

    public SendGridEmailSender() {
        this.apiKey = System.getenv("SENDGRID_API_KEY");
        this.fromAddress = System.getenv("SENDGRID_FROM");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Falta SENDGRID_API_KEY en variables de entorno.");
        }
        if (fromAddress == null || fromAddress.isBlank()) {
            throw new IllegalStateException("Falta SENDGRID_FROM en variables de entorno.");
        }
    }

    public void sendText(String to, String subject, String textBody) throws IOException {
        sendInternal(to, subject, textBody, "text/plain");
    }

    public void sendHtml(String to, String subject, String htmlBody) throws IOException {
        sendInternal(to, subject, htmlBody, "text/html");
    }

    private void sendInternal(String to, String subject, String body, String contentType) throws IOException {
        Email from = new Email(fromAddress);
        Email toEmail = new Email(to);
        Content content = new Content(contentType, body);

        Mail mail = new Mail(from, subject, toEmail, content);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        SendGrid sg = new SendGrid(apiKey);
        Response response = sg.api(request);

        int status = response.getStatusCode();
        String respBody = response.getBody() == null ? "" : response.getBody();
        if (status >= 200 && status < 300) {
            System.out.println("Email enviado OK (" + status + ")");
        } else {
            System.out.println("SendGrid error. Status=" + status);
            System.out.println("Detalle: " + new String(respBody.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        }
    }
}

