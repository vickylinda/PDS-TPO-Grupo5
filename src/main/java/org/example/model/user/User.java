package org.example.model.user;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.example.model.Moderator;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = RegularUser.class, name = "USER"),
        @JsonSubTypes.Type(value = Moderator.class,  name = "MOD"),
        @JsonSubTypes.Type(value = Admin.class,      name = "ADMIN")
})

public abstract class User {
    private String id;
    private String email;
    private String passwordHash;
    private String saltBase64;
    private boolean active = true;
    private Boolean inicioSesionConGoogle = false;

    private Perfil perfil;
    private String region;

    public User() {}

    public User(String id, String email, String passwordHash, String saltBase64) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.saltBase64 = saltBase64;
        this.active = true;
        this.inicioSesionConGoogle = false;
    }

    public User(String id, String email, String passwordHash, String saltBase64, boolean inicioSesionConGoogle) {
        this(id, email, passwordHash, saltBase64);
        this.inicioSesionConGoogle = inicioSesionConGoogle;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getSaltBase64() { return saltBase64; }
    public void setSaltBase64(String saltBase64) { this.saltBase64 = saltBase64; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getRegion() {return region;}
    public void setRegion(String region) {this.region = region;}

    public boolean isInicioSesionConGoogle() {
        return inicioSesionConGoogle != null ? inicioSesionConGoogle : false;
    }
    public void setInicioSesionConGoogle(Boolean inicioSesionConGoogle) {
        this.inicioSesionConGoogle = inicioSesionConGoogle;
    }
    //roles por herencia
    @com.fasterxml.jackson.annotation.JsonIgnore
    public abstract String getRoleName();

    public Perfil getPerfil() {return perfil;}
    public void setPerfil(Perfil perfil) {this.perfil = perfil;}


}
