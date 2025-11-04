package org.example.service;

import org.example.model.Scrim;
import org.example.model.scrim.matchmaking.ByMMRStrategy;
import org.example.model.scrim.matchmaking.MatchmakingStrategy;
import org.example.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de emparejamiento que utiliza el patrón Strategy para
 * seleccionar jugadores para scrims según diferentes criterios.
 */
public class MatchmakingService {

    private MatchmakingStrategy strategy;

    /**
     * Constructor por defecto que inicializa con estrategia por MMR.
     */
    public MatchmakingService() {
        this.strategy = new ByMMRStrategy(500); // Estrategia por defecto
    }

    /**
     * Constructor que permite especificar la estrategia a usar.
     *
     * @param strategy Estrategia de emparejamiento
     */
    public MatchmakingService(MatchmakingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("La estrategia no puede ser null");
        }
        this.strategy = strategy;
    }

    /**
     * Cambia la estrategia de emparejamiento en tiempo de ejecución.
     *
     * @param strategy Nueva estrategia a utilizar
     */
    public void setStrategy(MatchmakingStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("La estrategia no puede ser null");
        }
        this.strategy = strategy;
        System.out.println("Estrategia cambiada a: " + strategy.getClass().getSimpleName());
    }

    /**
     * Obtiene la estrategia actual.
     *
     * @return Estrategia de emparejamiento actual
     */
    public MatchmakingStrategy getStrategy() {
        return strategy;
    }

    /**
     * Empareja jugadores para un scrim utilizando la estrategia actual.
     *
     * @param scrim Scrim para el cual buscar jugadores
     * @param candidatos Lista de usuarios candidatos disponibles
     * @return Lista de usuarios seleccionados y ordenados por compatibilidad
     */
    public List<User> emparejarJugadores(Scrim scrim, List<User> candidatos) {
        if (scrim == null) {
            throw new IllegalArgumentException("El scrim no puede ser null");
        }

        if (candidatos == null || candidatos.isEmpty()) {
            System.out.println("No hay candidatos disponibles para emparejar");
            return new ArrayList<>();
        }

        System.out.println("\n=== INICIANDO EMPAREJAMIENTO ===");
        System.out.println("Estrategia: " + strategy.getClass().getSimpleName());
        System.out.println("Scrim: " + scrim.getId() + " - " + scrim.getFormato());
        System.out.println("Candidatos disponibles: " + candidatos.size());

        // Usar la estrategia para seleccionar y ordenar candidatos
        List<User> jugadoresSeleccionados = strategy.seleccionar(candidatos, scrim);

        System.out.println("Jugadores seleccionados: " + jugadoresSeleccionados.size());
        System.out.println("================================\n");

        return jugadoresSeleccionados;
    }

    /**
     * Forma equipos balanceados a partir de los jugadores emparejados.
     * Distribuye los jugadores entre Equipo A y Equipo B de manera equilibrada.
     *
     * @param jugadores Lista de jugadores seleccionados
     * @param scrim Scrim para el cual formar equipos
     * @return Resultado con ambos equipos formados
     */
    public ResultadoFormacionEquipos armarEquipos(List<User> jugadores, Scrim scrim) {
        if (jugadores == null || jugadores.isEmpty()) {
            return new ResultadoFormacionEquipos(new ArrayList<>(), new ArrayList<>());
        }

        int jugadoresPorLado = scrim.getJugadoresPorLado();
        int jugadoresNecesarios = jugadoresPorLado * 2;

        if (jugadores.size() < jugadoresNecesarios) {
            System.out.println("⚠ Advertencia: No hay suficientes jugadores. " +
                    "Necesarios: " + jugadoresNecesarios + ", Disponibles: " + jugadores.size());
        }

        List<User> equipoA = new ArrayList<>();
        List<User> equipoB = new ArrayList<>();

        // Distribuir jugadores alternadamente para balance
        // Los mejores matches se distribuyen equitativamente
        for (int i = 0; i < Math.min(jugadores.size(), jugadoresNecesarios); i++) {
            if (i % 2 == 0 && equipoA.size() < jugadoresPorLado) {
                equipoA.add(jugadores.get(i));
            } else if (equipoB.size() < jugadoresPorLado) {
                equipoB.add(jugadores.get(i));
            } else if (equipoA.size() < jugadoresPorLado) {
                equipoA.add(jugadores.get(i));
            }
        }

        System.out.println("\n=== EQUIPOS FORMADOS ===");
        System.out.println("Equipo A: " + equipoA.size() + "/" + jugadoresPorLado);
        equipoA.forEach(u -> System.out.println("  - " + u.getEmail()));
        System.out.println("Equipo B: " + equipoB.size() + "/" + jugadoresPorLado);
        equipoB.forEach(u -> System.out.println("  - " + u.getEmail()));
        System.out.println("========================\n");

        return new ResultadoFormacionEquipos(equipoA, equipoB);
    }

    /**
     * Busca y agrega jugadores automáticamente a un scrim.
     *
     * @param scrim Scrim al cual agregar jugadores
     * @param candidatos Lista de candidatos disponibles
     * @return Número de jugadores agregados exitosamente
     */
    public int buscarYAgregarJugadores(Scrim scrim, List<User> candidatos) {
        if (scrim == null || scrim.estaCompleto()) {
            return 0;
        }

        // Emparejar jugadores
        List<User> jugadoresSeleccionados = emparejarJugadores(scrim, candidatos);

        // Calcular cuántos jugadores faltan
        int jugadoresFaltantes = scrim.getJugadoresFaltantes();

        // Agregar jugadores hasta completar el scrim
        int jugadoresAgregados = 0;
        for (User jugador : jugadoresSeleccionados) {
            if (jugadoresAgregados >= jugadoresFaltantes) {
                break;
            }

            try {
                scrim.agregarJugador(jugador);
                jugadoresAgregados++;
            } catch (IllegalStateException e) {
                System.out.println("No se pudo agregar jugador: " + e.getMessage());
            }
        }

        return jugadoresAgregados;
    }

    /**
     * Evalúa qué tan compatible es un usuario con un scrim.
     *
     * @param usuario Usuario a evaluar
     * @param scrim Scrim de referencia
     * @return Puntuación de compatibilidad (0-100)
     */
    public Double evaluarCompatibilidad(User usuario, Scrim scrim) {
        return strategy.calcularPuntuacion(usuario, scrim);
    }

    /**
     * Filtra candidatos que sean compatibles con el scrim.
     *
     * @param candidatos Lista de candidatos
     * @param scrim Scrim de referencia
     * @param puntuacionMinima Puntuación mínima requerida
     * @return Lista de candidatos compatibles
     */
    public List<User> filtrarCandidatosCompatibles(List<User> candidatos, Scrim scrim, double puntuacionMinima) {
        if (candidatos == null || candidatos.isEmpty()) {
            return new ArrayList<>();
        }

        return candidatos.stream()
                .filter(usuario -> evaluarCompatibilidad(usuario, scrim) >= puntuacionMinima)
                .collect(Collectors.toList());
    }

    /**
     * Clase interna para encapsular el resultado de la formación de equipos.
     */
    public static class ResultadoFormacionEquipos {
        private final List<User> equipoA;
        private final List<User> equipoB;

        public ResultadoFormacionEquipos(List<User> equipoA, List<User> equipoB) {
            this.equipoA = equipoA;
            this.equipoB = equipoB;
        }

        public List<User> getEquipoA() {
            return equipoA;
        }

        public List<User> getEquipoB() {
            return equipoB;
        }

        public boolean estanCompletos(int jugadoresPorLado) {
            return equipoA.size() == jugadoresPorLado &&
                    equipoB.size() == jugadoresPorLado;
        }

        public int totalJugadores() {
            return equipoA.size() + equipoB.size();
        }
    }
}