package org.example.service;

import org.example.model.*;
import java.util.List;

public class MatchmakingService {
    private MatchmakingStrategy strategy;

    public void setStrategy(MatchmakingStrategy strategy) {
        this.strategy = strategy;
    }

    public List<User> emparejarJugadores(Scrim scrim, List<User> candidatos) {
        return strategy.seleccionar(candidatos, scrim);
    }

    public List<Equipo> armarEquipos(List<User> jugadores, Scrim scrim) {
        // División simple 50/50 en dos equipos
        int mitad = jugadores.size() / 2;
        List<User> equipoA = jugadores.subList(0, mitad);
        List<User> equipoB = jugadores.subList(mitad, jugadores.size());

        return List.of(new Equipo("Equipo A", equipoA), new Equipo("Equipo B", equipoB));
    }
}

