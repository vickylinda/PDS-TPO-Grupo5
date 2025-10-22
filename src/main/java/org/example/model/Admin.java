package org.example.model;

public class Admin extends User {
    public Admin() { super(); }
    public Admin(String id, String email, String passwordHash, String saltBase64) {
        super(id, email, passwordHash, saltBase64);
    }
    @Override public String getRoleName() { return "ADMIN"; }
}

