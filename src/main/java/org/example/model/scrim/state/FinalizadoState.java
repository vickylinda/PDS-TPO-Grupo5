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
        }

        System.out.println("Resultados cargados. Ganador: " + resultados.getGanadorEquipo());
    }

    @Override
    public String getNombreEstado() {
        return "Finalizado";
    }
}
