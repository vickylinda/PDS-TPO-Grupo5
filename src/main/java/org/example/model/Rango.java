package org.example.model;

public class Rango {
    private String nombre;
    private int nivel;

    public Rango(String nombre, int nivel) {
        this.nombre = nombre;
        this.nivel = nivel;
    }

    public String getNombre() { return nombre; }
    public int getNivel() { return nivel; }
}
