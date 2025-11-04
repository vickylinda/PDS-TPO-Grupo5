package org.example.model.scrim.matchmaking;

import org.example.model.Scrim;
import org.example.model.User;

import java.util.List;

public interface MatchmakingStrategy {

    /**
     * Selecciona los mejores candidatos para un scrim según el criterio de la estrategia.
     *
     * @param candidatos Lista de usuarios candidatos disponibles
     * @param scrim El scrim para el cual se buscan jugadores
     * @return Lista de usuarios seleccionados, ordenados por mejor compatibilidad
     */
    List<User> seleccionar(List<User> candidatos, Scrim scrim);

    /**
     * Calcula una puntuación de compatibilidad para un usuario con respecto a un scrim.
     * Mayor puntuación = mejor compatibilidad
     *
     * @param usuario Usuario a evaluar
     * @param scrim Scrim de referencia
     * @return Puntuación de compatibilidad (0.0 = incompatible, 100.0 = perfecto)
     */
    Double calcularPuntuacion(User usuario, Scrim scrim);
}