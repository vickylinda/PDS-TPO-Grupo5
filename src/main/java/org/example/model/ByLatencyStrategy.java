package org.example.model;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ByLatencyStrategy implements MatchmakingStrategy {
    private int umbralLatencia;

    public ByLatencyStrategy(int umbralLatencia) {
        this.umbralLatencia = umbralLatencia;
    }

    @Override
    public List<User> seleccionar(List<User> candidatos, Scrim scrim) {
        return candidatos.stream()
                .filter(u -> obtenerLatencia(u, scrim.getRegion()) <= umbralLatencia)
                .collect(Collectors.toList());
    }

    @Override
    public double calcularPuntuacion(User usuario, Scrim scrim) {
        return obtenerLatencia(usuario, scrim.getRegion());
    }

    // Simula latencia con un número aleatorio según región
    private int obtenerLatencia(User usuario, String region) {
        return new Random().nextInt(150); // Simula ping entre 0 y 150ms
    }
}
