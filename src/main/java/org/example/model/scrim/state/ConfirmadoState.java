package org.example.model.scrim.state;


import org.example.model.scrim.Scrim;
import org.example.model.user.User;

import java.time.LocalDateTime;

public class ConfirmadoState extends ScrimStateBase {

    public ConfirmadoState(Scrim scrim) {
        super(scrim);
    }

    @Override
    public void agregarJugador(User usuario) {
        System.out.println("El scrim ya está confirmado. No se pueden agregar jugadores.");
    }

    @Override
    public void confirmar(User usuario) {
        System.out.println("Todos los jugadores ya confirmaron.");
    }

    @Override
    public void iniciar() {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime fechaHoraInicio = scrim.getFechaHora();

        // Verificar si es la hora de inicio (con margen de 5 minutos antes)
        if (fechaHoraInicio != null && ahora.isBefore(fechaHoraInicio.minusMinutes(5))) {
            System.out.println("Aún no es la hora de inicio. Inicio programado: " + fechaHoraInicio);
            return;
        }

        scrim.setFechaInicio(LocalDateTime.now());
        System.out.println("¡Iniciando partida!");
        scrim.cambiarEstado(new EnJuegoState(scrim));
    }

    @Override
    public String getNombreEstado() {
        return "Confirmado";
    }
}
