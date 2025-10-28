package org.example.model;

import org.example.model.user.User;

public class Moderator extends User {
    public Moderator() { super(); }
    public Moderator(String id, String email, String passwordHash, String saltBase64) {
        super(id, email, passwordHash, saltBase64);
    }
    @Override public String getRoleName() { return "MOD"; }
}
