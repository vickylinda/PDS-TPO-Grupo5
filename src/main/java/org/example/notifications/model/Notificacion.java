package org.example.notifications.model;
import org.example.notifications.TipoEvento;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class Notificacion {
    private final String id = UUID.randomUUID().toString();
    private final TipoEvento tipoEvento;
    private final List<String> destinatarios;
    private final String asunto;
    private final String cuerpoHtml;
    private final LocalDateTime ts = LocalDateTime.now();

    // estado como string para evitar enum ("PENDING", "SENT", "ERROR")
    private String estado = "PENDING";
    private String errorMsg;

    public Notificacion(TipoEvento tipoEvento, List<String> destinatarios, String asunto, String cuerpoHtml) {
        this.tipoEvento = tipoEvento;
        this.destinatarios = destinatarios;
        this.asunto = asunto;
        this.cuerpoHtml = cuerpoHtml;
    }

    public String getId() { return id; }
    public TipoEvento getTipoEvento() { return tipoEvento; }
    public List<String> getDestinatarios() { return destinatarios; }
    public String getAsunto() { return asunto; }
    public String getCuerpoHtml() { return cuerpoHtml; }
    public LocalDateTime getTs() { return ts; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getErrorMsg() { return errorMsg; }
    public void setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }
}
