package org.example.controller;

import org.example.model.user.User;

public class Session {
    private String token;
    private User currentUser;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getCurrentUser() { return currentUser; }
    public void setCurrentUser(User currentUser) { this.currentUser = currentUser; }

    public void clear() { token = null; currentUser = null; }
}