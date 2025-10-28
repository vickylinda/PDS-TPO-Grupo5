package org.example.model;

import java.util.List;

public interface MatchmakingStrategy {
    List<User> seleccionar(List<User> candidatos, Scrim scrim);
    double calcularPuntuacion(User usuario, Scrim scrim);
}
