package org.example.model.scrim.state;

import org.example.model.scrim.Resultados;
import org.example.model.scrim.Scrim;
import org.example.model.user.User;
import org.example.model.user.Estadisticas;

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
        throw new IllegalStateException("No se puede cancelar un scrim finalizado.");
    }

    @Override
    public void cargarResultados(Resultados resultados) {
        scrim.setResultados(resultados);

        // Actualizar estadísticas de cada usuario
        for (Map.Entry<User, Estadisticas> entry : resultados.getEstadisticas().entrySet()) {
            User usuario = entry.getKey();
            Estadisticas statsPartida = entry.getValue();
            usuario.actualizarEstadisticas(statsPartida);

            // Verificar si el usuario está en el equipo ganador
            boolean esGanador = false;

            if ("Equipo A".equalsIgnoreCase(resultados.getGanadorEquipo()) && scrim.getEquipoA().contains(usuario)) {
                esGanador = true;
            } else if ("Equipo B".equalsIgnoreCase(resultados.getGanadorEquipo()) && scrim.getEquipoB().contains(usuario)) {
                esGanador = true;
            }

            // Si el usuario pertenece al equipo ganador, sumar 100 puntos
            if (esGanador && usuario.getPerfil() != null) {
                usuario.getPerfil().actualizarPuntaje(100);
            }


        }

        System.out.println("Resultados cargados. Ganador: " + resultados.getGanadorEquipo());
    }

    @Override
    public String getNombreEstado() {
        return "Finalizado";
    }
}
