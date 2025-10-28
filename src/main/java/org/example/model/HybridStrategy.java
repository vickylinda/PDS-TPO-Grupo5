package org.example.model;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class HybridStrategy implements MatchmakingStrategy {
    private double pesoAbandonos;
    private double pesoFairPlay;
    private final Random random = new Random();

    public HybridStrategy(double pesoAbandonos, double pesoFairPlay) {
        this.pesoAbandonos = pesoAbandonos;
        this.pesoFairPlay = pesoFairPlay;
    }

    @Override
    public List<User> seleccionar(List<User> candidatos, Scrim scrim) {
        return candidatos.stream()
                .sorted((u1, u2) -> Double.compare(
                        calcularPuntuacion(u2, scrim),
                        calcularPuntuacion(u1, scrim)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public double calcularPuntuacion(User usuario, Scrim scrim) {
        double fairPlay = random.nextDouble(0.5, 1.0);  // simula reputación
        double abandonos = random.nextDouble(0, 0.3);   // simula abandonos

        return (pesoFairPlay * fairPlay) - (pesoAbandonos * abandonos);
    }
}
