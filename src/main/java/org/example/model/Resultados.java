package org.example.model;

import org.example.model.user.User;

import java.util.*;

public class Resultados {
    private String ganadorEquipo;
    private Map<User, Estadisticas> estadisticas;

    public Resultados() {
        this.estadisticas = new HashMap<>();
    }

    public void registrarGanador(String equipo) {
        this.ganadorEquipo = equipo;
    }

    public void agregarEstadistica(User usuario, Estadisticas stats) {
        this.estadisticas.put(usuario, stats);
    }



    public String getGanadorEquipo() {
        return ganadorEquipo;
    }

    public Map<User, Estadisticas> getEstadisticas() {
        return estadisticas;
    }

}