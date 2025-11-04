package org.example.model.scrim;

import org.example.model.user.Estadisticas;
import org.example.model.user.User;

import java.util.List;
import java.util.Random;

/**
 * Generador de resultados mockeados para MVP
 * Permite crear resultados aleatorios o manuales para testing
 */

public class ResultadosGenerator {

    private static final Random random = new Random();
    /**
     * Genera resultados aleatorios completos para un scrim finalizado
     * Incluye equipo ganador y estadísticas para todos los jugadores
     *
     * @param scrim El scrim para el cual generar resultados
     * @return Objeto Resultados con datos mockeados
     */
    public static Resultados generarResultadosAleatorios(Scrim scrim) {
        Resultados resultados = new Resultados();

        // Determinar equipo ganador aleatoriamente
        String equipoGanador = random.nextBoolean() ? "Equipo A" : "Equipo B";
        resultados.registrarGanador(equipoGanador);

        // Generar estadísticas para Equipo A
        for (User usuario : scrim.getEquipoA()) {
            Estadisticas stats = generarEstadisticasAleatorias();
            resultados.agregarEstadistica(usuario, stats);
        }

        // Generar estadísticas para Equipo B
        for (User usuario : scrim.getEquipoB()) {
            Estadisticas stats = generarEstadisticasAleatorias();
            resultados.agregarEstadistica(usuario, stats);
        }

        return resultados;
    }

    /**
     * Genera estadísticas realistas aleatorias
     */
    private static Estadisticas generarEstadisticasAleatorias() {
        Estadisticas stats = new Estadisticas();
        stats.setKills(random.nextInt(25));      // 0-24 kills
        stats.setDeaths(random.nextInt(15));     // 0-14 deaths
        stats.setAssists(random.nextInt(20));    // 0-19 assists
        return stats;
    }

    /**
     * Genera estadísticas realistas con tendencia al ganador
     * El equipo ganador tendrá mejores estadísticas en promedio
     *
     * @param scrim El scrim finalizado
     * @return Resultados con estadísticas sesgadas hacia el ganador
     */
    public static Resultados generarResultadosRealistas(Scrim scrim) {
        Resultados resultados = new Resultados();

        // Determinar equipo ganador aleatoriamente
        boolean ganaEquipoA = random.nextBoolean();
        String equipoGanador = ganaEquipoA ? "Equipo A" : "Equipo B";
        resultados.registrarGanador(equipoGanador);

        // Generar estadísticas con sesgo para el ganador
        for (User usuario : scrim.getEquipoA()) {
            Estadisticas stats = generarEstadisticasSesgadas(ganaEquipoA);
            resultados.agregarEstadistica(usuario, stats);
        }

        for (User usuario : scrim.getEquipoB()) {
            Estadisticas stats = generarEstadisticasSesgadas(!ganaEquipoA);
            resultados.agregarEstadistica(usuario, stats);
        }

        return resultados;
    }

    /**
     * Genera estadísticas con sesgo según si el equipo ganó o perdió
     */
    private static Estadisticas generarEstadisticasSesgadas(boolean esGanador) {
        Estadisticas stats = new Estadisticas();

        if (esGanador) {
            // Equipo ganador: más kills, menos deaths
            stats.setKills(10 + random.nextInt(15));  // 10-24
            stats.setDeaths(random.nextInt(8));       // 0-7
            stats.setAssists(8 + random.nextInt(12)); // 8-19
        } else {
            // Equipo perdedor: menos kills, más deaths
            stats.setKills(random.nextInt(12));       // 0-11
            stats.setDeaths(5 + random.nextInt(10));  // 5-14
            stats.setAssists(random.nextInt(12));     // 0-11
        }

        return stats;
    }

    /**
     * Genera resultados con un equipo específico como ganador
     *
     * @param scrim El scrim finalizado
     * @param equipoGanador "A" o "B"
     * @return Resultados con el equipo especificado como ganador
     */
    public static Resultados generarConGanador(Scrim scrim, String equipoGanador) {
        Resultados resultados = new Resultados();

        boolean ganaA = "A".equalsIgnoreCase(equipoGanador);
        resultados.registrarGanador("Equipo " + equipoGanador.toUpperCase());

        // Generar estadísticas sesgadas según el ganador
        for (User usuario : scrim.getEquipoA()) {
            Estadisticas stats = generarEstadisticasSesgadas(ganaA);
            resultados.agregarEstadistica(usuario, stats);
        }

        for (User usuario : scrim.getEquipoB()) {
            Estadisticas stats = generarEstadisticasSesgadas(!ganaA);
            resultados.agregarEstadistica(usuario, stats);
        }

        return resultados;
    }

    /**
     * Crea resultados manualmente (para cuando tengas UI o quieras datos específicos)
     *
     * @param equipoGanador "Equipo A" o "Equipo B"
     * @param todosLosJugadores Lista ordenada de jugadores (primero A, luego B)
     * @param kills Array de kills para cada jugador
     * @param deaths Array de deaths para cada jugador
     * @param assists Array de assists para cada jugador
     */
    public static Resultados crearResultadosManual(
            String equipoGanador,
            List<User> todosLosJugadores,
            int[] kills,
            int[] deaths,
            int[] assists
    ) {
        if (todosLosJugadores.size() != kills.length ||
                kills.length != deaths.length ||
                deaths.length != assists.length) {
            throw new IllegalArgumentException("Los arrays deben tener el mismo tamaño que la lista de jugadores");
        }

        Resultados resultados = new Resultados();
        resultados.registrarGanador(equipoGanador);

        for (int i = 0; i < todosLosJugadores.size(); i++) {
            Estadisticas stats = new Estadisticas();
            stats.setKills(kills[i]);
            stats.setDeaths(deaths[i]);
            stats.setAssists(assists[i]);
            resultados.agregarEstadistica(todosLosJugadores.get(i), stats);
        }

        return resultados;
    }

    /**
     * Muestra un resumen de los resultados generados
     */
    public static void mostrarResumen(Resultados resultados) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("RESUMEN DE RESULTADOS");
        System.out.println("=".repeat(60));
        System.out.println("🏆 Ganador: " + resultados.getGanadorEquipo());
        System.out.println("\nEstadísticas individuales:");
        System.out.println(String.format("%-30s | %6s | %6s | %6s | KDA",
                "Jugador", "Kills", "Deaths", "Assists"));
        System.out.println("-".repeat(60));

        resultados.getEstadisticas().forEach((usuario, stats) -> {
            double kda = stats.getDeaths() == 0 ?
                    (stats.getKills() + stats.getAssists()) :
                    (stats.getKills() + stats.getAssists()) / (double) stats.getDeaths();

            System.out.println(String.format("%-30s | %6d | %6d | %6d | %.2f",
                    usuario.getEmail(),
                    stats.getKills(),
                    stats.getDeaths(),
                    stats.getAssists(),
                    kda
            ));
        });
        System.out.println("=".repeat(60) + "\n");
    }
}