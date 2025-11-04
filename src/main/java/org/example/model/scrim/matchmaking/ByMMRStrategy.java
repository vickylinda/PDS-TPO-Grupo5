package org.example.model.scrim.matchmaking;


import org.example.model.Scrim;
import org.example.model.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Estrategia de emparejamiento basada en el rango/MMR de los jugadores.
 * Filtra jugadores dentro de una diferencia máxima de MMR y prioriza los más cercanos.
 */
public class ByMMRStrategy implements MatchmakingStrategy {

    private int diferenciaMaxima;

    /**
     * Constructor con diferencia máxima de MMR configurable.
     *
     * @param diferenciaMaxima Diferencia máxima permitida de puntaje MMR
     */
    public ByMMRStrategy(int diferenciaMaxima) {
        if (diferenciaMaxima < 0) {
            throw new IllegalArgumentException("La diferencia máxima no puede ser negativa");
        }
        this.diferenciaMaxima = diferenciaMaxima;
    }

    @Override
    public List<User> seleccionar(List<User> candidatos, Scrim scrim) {
        if (candidatos == null || candidatos.isEmpty()) {
            return new ArrayList<>();
        }

        // Calcular el MMR promedio esperado del scrim
        int mmrPromedio = calcularMMRPromedio(scrim);

        // Filtrar y ordenar candidatos
        return candidatos.stream()
                .filter(usuario -> esDentroRango(usuario, scrim))
                .filter(usuario -> estaDentroRangoMMR(usuario, mmrPromedio))
                .sorted(Comparator.comparingDouble(u -> -calcularPuntuacion(u, scrim)))
                .collect(Collectors.toList());
    }

    @Override
    public Double calcularPuntuacion(User usuario, Scrim scrim) {
        if (usuario == null || usuario.getPerfil() == null || scrim == null) {
            return 0.0;
        }

        int mmrUsuario = usuario.getPerfil().getPuntaje();
        int mmrPromedio = calcularMMRPromedio(scrim);

        // Si está fuera del rango permitido del scrim, puntuación 0
        if (!esDentroRango(usuario, scrim)) {
            return 0.0;
        }

        // Si está fuera de la diferencia máxima de MMR, puntuación 0
        if (!estaDentroRangoMMR(usuario, mmrPromedio)) {
            return 0.0;
        }

        // Calcular puntuación basada en proximidad al MMR promedio
        // Puntuación máxima (100) cuando la diferencia es 0
        // Puntuación mínima (0) cuando la diferencia es igual a diferenciaMaxima
        int diferencia = Math.abs(mmrUsuario - mmrPromedio);

        if (diferenciaMaxima == 0) {
            return diferencia == 0 ? 100.0 : 0.0;
        }

        double puntuacion = 100.0 * (1.0 - ((double) diferencia / diferenciaMaxima));
        return Math.max(0.0, Math.min(100.0, puntuacion));
    }

    /**
     * Verifica si el usuario cumple con los requisitos de rango del scrim.
     */
    private boolean esDentroRango(User usuario, Scrim scrim) {
        if (usuario.getPerfil() == null) {
            return false;
        }

        int mmrUsuario = usuario.getPerfil().getPuntaje();
        return scrim.cumpleRequisitosRango(mmrUsuario);
    }

    /**
     * Verifica si el usuario está dentro de la diferencia máxima de MMR permitida.
     */
    public boolean estaDentroRangoMMR(User usuario, int mmrPromedio) {
        if (usuario.getPerfil() == null) {
            return false;
        }

        int mmrUsuario = usuario.getPerfil().getPuntaje();
        int diferencia = Math.abs(mmrUsuario - mmrPromedio);

        return diferencia <= diferenciaMaxima;
    }

    /**
     * Calcula el MMR promedio esperado del scrim basado en sus restricciones.
     */
    private int calcularMMRPromedio(Scrim scrim) {
        Integer rangoMin = scrim.getRangoMin();
        Integer rangoMax = scrim.getRangoMax();

        // Si hay restricciones de rango, usar el promedio
        if (rangoMin != null && rangoMax != null) {
            return (rangoMin + rangoMax) / 2;
        }

        // Si solo hay mínimo, usar ese valor
        if (rangoMin != null) {
            return rangoMin;
        }

        // Si solo hay máximo, usar ese valor
        if (rangoMax != null) {
            return rangoMax;
        }

        // Sin restricciones, usar 0 como promedio
        return 0;
    }

    // Getters y Setters
    public int getDiferenciaMaxima() {
        return diferenciaMaxima;
    }

    public void setDiferenciaMaxima(int diferenciaMaxima) {
        if (diferenciaMaxima < 0) {
            throw new IllegalArgumentException("La diferencia máxima no puede ser negativa");
        }
        this.diferenciaMaxima = diferenciaMaxima;
    }
}