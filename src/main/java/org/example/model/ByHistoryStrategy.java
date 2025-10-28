package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class ByHistoryStrategy implements MatchmakingStrategy {
    private List<MatchmakingStrategy> strategies = new ArrayList<>();

    public void agregarEstrategia(MatchmakingStrategy strategy) {
        strategies.add(strategy);
    }

    @Override
    public List<User> seleccionar(List<User> candidatos, Scrim scrim) {
        for (MatchmakingStrategy s : strategies) {
            candidatos = s.seleccionar(candidatos, scrim);
        }
        return candidatos;
    }

    @Override
    public double calcularPuntuacion(User usuario, Scrim scrim) {
        return strategies.stream()
                .mapToDouble(s -> s.calcularPuntuacion(usuario, scrim))
                .average()
                .orElse(0);
    }
}
