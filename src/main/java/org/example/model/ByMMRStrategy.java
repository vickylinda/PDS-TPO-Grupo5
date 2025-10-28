package org.example.model;

import java.util.List;
import java.util.stream.Collectors;

public class ByMMRStrategy implements MatchmakingStrategy {
    private int diferenciaMaxima;

    public ByMMRStrategy(int diferenciaMaxima) {
        this.diferenciaMaxima = diferenciaMaxima;
    }

    @Override
    public List<User> seleccionar(List<User> candidatos, Scrim scrim) {
        if (candidatos.isEmpty()) return candidatos;

        // Calcular promedio de puntaje (MMR)
        double promedio = candidatos.stream()
                .mapToInt(u -> u.getPerfil().getPuntaje())
                .average()
                .orElse(0);

        return candidatos.stream()
                .filter(u -> Math.abs(u.getPerfil().getPuntaje() - promedio) <= diferenciaMaxima)
                .collect(Collectors.toList());
    }

    @Override
    public double calcularPuntuacion(User usuario, Scrim scrim) {
        return usuario.getPerfil().getPuntaje();
    }
}
