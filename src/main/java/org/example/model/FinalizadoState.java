package org.example.model;

import java.util.Map;

public class FinalizadoState extends ScrimStateBase {

    public FinalizadoState(Scrim scrim) {
        super(scrim);
    }

    @Override
    public void agregarJugador(User usuario) {
        System.out.println("El scrim ya ha finalizado.");
    }

    @Override
    public void confirmar(User usuario) {
        System.out.println("El scrim ya ha finalizado.");
    }

    @Override
    public void iniciar() {
        System.out.println("El scrim ya ha finalizado.");
    }

    @Override
    public void finalizar() {
        System.out.println("El scrim ya está finalizado.");
    }

    @Override
    public void cancelar() {
        System.out.println("No se puede cancelar un scrim finalizado.");
    }

    @Override
    public void cargarResultados(Resultados resultados) {
        scrim.setResultados(resultados);

        // Actualizar estadísticas de cada usuario
        for (Map.Entry<User, Estadisticas> entry : resultados.getEstadisticas().entrySet()) {
            User usuario = entry.getKey();
            Estadisticas statsPartida = entry.getValue();
            usuario.actualizarEstadisticas(statsPartida);
        }

        System.out.println("Resultados cargados. Ganador: " + resultados.getGanadorEquipo());
    }

    @Override
    public String getNombreEstado() {
        return "Finalizado";
    }
}
