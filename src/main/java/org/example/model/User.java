package org.example.model;

public class User {
    private String id;
    private String email;
    private String passwordHash;
    private String saltBase64;
    private boolean active = true;
    private Boolean inicioSesionConGoogle = false;

    private Perfil perfil;

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

    public boolean isInicioSesionConGoogle() {
        return inicioSesionConGoogle != null ? inicioSesionConGoogle : false;
    }
    public void setInicioSesionConGoogle(Boolean inicioSesionConGoogle) {
        this.inicioSesionConGoogle = inicioSesionConGoogle;
    }

    public Perfil getPerfil() {return perfil;}
    public void setPerfil(Perfil perfil) {this.perfil = perfil;}
}
