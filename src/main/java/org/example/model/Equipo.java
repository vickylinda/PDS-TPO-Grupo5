package org.example.model;

import java.util.List;

public class Equipo {
    private String nombre;
    private List<User> jugadores;

    public Equipo(String nombre, List<User> jugadores) {
        this.nombre = nombre;
        this.jugadores = jugadores;
    }

    public String getNombre() { return nombre; }
    public List<User> getJugadores() { return jugadores; }
}
