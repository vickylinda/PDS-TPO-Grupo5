package org.example.model.scrim.matchmaking;


import org.example.model.Scrim;
import org.example.model.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Estrategia de emparejamiento basada en la latencia/ping de los jugadores.
 * Filtra jugadores dentro de un umbral de latencia y prioriza los de menor latencia.
 */
public class ByLatencyStrategy implements MatchmakingStrategy {

    private int umbralLatencia;

    // Tabla de latencias simuladas por región
    // En una implementación real, esto se obtendría de un servicio de red
    private static final Map<String, Map<String, Integer>> LATENCIAS_POR_REGION = new HashMap<>();

    static {
        // Inicializar latencias base entre regiones (ms)
        Map<String, Integer> latenciasNA = new HashMap<>();
        latenciasNA.put("NA", 10);  // Misma región
        latenciasNA.put("EU", 120);
        latenciasNA.put("SA", 80);
        latenciasNA.put("AS", 200);
        latenciasNA.put("OCE", 180);
        LATENCIAS_POR_REGION.put("NA", latenciasNA);

        Map<String, Integer> latenciasEU = new HashMap<>();
        latenciasEU.put("NA", 120);
        latenciasEU.put("EU", 10);
        latenciasEU.put("SA", 150);
        latenciasEU.put("AS", 180);
        latenciasEU.put("OCE", 280);
        LATENCIAS_POR_REGION.put("EU", latenciasEU);

        Map<String, Integer> latenciasSA = new HashMap<>();
        latenciasSA.put("NA", 80);
        latenciasSA.put("EU", 150);
        latenciasSA.put("SA", 10);
        latenciasSA.put("AS", 250);
        latenciasSA.put("OCE", 220);
        LATENCIAS_POR_REGION.put("SA", latenciasSA);

        Map<String, Integer> latenciasAS = new HashMap<>();
        latenciasAS.put("NA", 200);
        latenciasAS.put("EU", 180);
        latenciasAS.put("SA", 250);
        latenciasAS.put("AS", 10);
        latenciasAS.put("OCE", 80);
        LATENCIAS_POR_REGION.put("AS", latenciasAS);

        Map<String, Integer> latenciasOCE = new HashMap<>();
        latenciasOCE.put("NA", 180);
        latenciasOCE.put("EU", 280);
        latenciasOCE.put("SA", 220);
        latenciasOCE.put("AS", 80);
        latenciasOCE.put("OCE", 10);
        LATENCIAS_POR_REGION.put("OCE", latenciasOCE);
    }

    /**
     * Constructor con umbral de latencia configurable.
     *
     * @param umbralLatencia Latencia máxima permitida en milisegundos
     */
    public ByLatencyStrategy(int umbralLatencia) {
        if (umbralLatencia < 0 || umbralLatencia > 500) {
            throw new IllegalArgumentException("El umbral de latencia debe estar entre 0 y 500 ms");
        }
        this.umbralLatencia = umbralLatencia;
    }

    @Override
    public List<User> seleccionar(List<User> candidatos, Scrim scrim) {
        if (candidatos == null || candidatos.isEmpty()) {
            return new ArrayList<>();
        }

        String regionScrim = scrim.getRegion();

        // Filtrar y ordenar candidatos por latencia
        return candidatos.stream()
                .filter(usuario -> {
                    int latencia = obtenerLatencia(usuario, regionScrim);
                    return latencia <= umbralLatencia;
                })
                .sorted(Comparator.comparingDouble(u -> -calcularPuntuacion(u, scrim)))
                .collect(Collectors.toList());
    }

    @Override
    public Double calcularPuntuacion(User usuario, Scrim scrim) {
        if (usuario == null || scrim == null || scrim.getRegion() == null) {
            return 0.0;
        }

        int latencia = obtenerLatencia(usuario, scrim.getRegion());

        // Si está fuera del umbral, puntuación 0
        if (latencia > umbralLatencia) {
            return 0.0;
        }

        // Calcular puntuación basada en latencia
        // Latencia 0-10ms = 100 puntos
        // Latencia = umbralLatencia = 0 puntos
        // Interpolación lineal entre estos valores

        if (umbralLatencia == 0) {
            return latencia == 0 ? 100.0 : 0.0;
        }

        // Considerar 10ms como latencia "perfecta" para dar margen
        int latenciaBase = 10;

        if (latencia <= latenciaBase) {
            return 100.0;
        }

        // Interpolación lineal desde latenciaBase hasta umbralLatencia
        double puntuacion = 100.0 * (1.0 - ((double)(latencia - latenciaBase) / (umbralLatencia - latenciaBase)));
        return Math.max(0.0, Math.min(100.0, puntuacion));
    }

    /**
     * Obtiene la latencia estimada de un usuario hacia una región específica.
     * En una implementación real, esto consultaría un servicio de red o base de datos.
     *
     * @param usuario Usuario del cual obtener la latencia
     * @param regionDestino Región de destino
     * @return Latencia estimada en milisegundos
     */
    public int obtenerLatencia(User usuario, String regionDestino) {
        if (usuario == null || usuario.getRegion() == null || regionDestino == null) {
            return Integer.MAX_VALUE; // Latencia infinita si no hay información
        }

        String regionOrigen = usuario.getRegion();

        // Si el usuario no tiene región asignada, asumir latencia máxima
        if (regionOrigen == null || regionOrigen.trim().isEmpty()) {
            return Integer.MAX_VALUE;
        }

        // Buscar en la tabla de latencias
        Map<String, Integer> latenciasOrigen = LATENCIAS_POR_REGION.get(regionOrigen);
        if (latenciasOrigen == null) {
            // Región no reconocida, asumir latencia alta
            return 200;
        }

        Integer latencia = latenciasOrigen.get(regionDestino);
        if (latencia == null) {
            // Región destino no reconocida, asumir latencia alta
            return 200;
        }

        return latencia;
    }

    // Getters y Setters
    public int getUmbralLatencia() {
        return umbralLatencia;
    }

    public void setUmbralLatencia(int umbralLatencia) {
        if (umbralLatencia < 0 || umbralLatencia > 500) {
            throw new IllegalArgumentException("El umbral de latencia debe estar entre 0 y 500 ms");
        }
        this.umbralLatencia = umbralLatencia;
    }
}