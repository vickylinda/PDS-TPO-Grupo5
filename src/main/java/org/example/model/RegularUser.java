package org.example.model;

public class RegularUser extends User {
    public RegularUser() { super(); }
    public RegularUser(String id, String email, String passwordHash, String saltBase64) {
        super(id, email, passwordHash, saltBase64);
    }
    @Override public String getRoleName() { return "USER"; }
}
