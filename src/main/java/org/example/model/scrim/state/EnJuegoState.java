package org.example.model.scrim.state;

import org.example.model.scrim.Scrim;
import org.example.model.user.User;

import java.time.LocalDateTime;

public class EnJuegoState extends ScrimStateBase {

    public EnJuegoState(Scrim scrim) {
        super(scrim);
    }

    @Override
    public void agregarJugador(User usuario) {
        System.out.println("El juego ya está en curso. No se pueden agregar jugadores.");
    }

    @Override
    public void confirmar(User usuario) {
        System.out.println("El juego ya está en curso.");
    }

    @Override
    public void iniciar() {
        System.out.println("El juego ya ha iniciado.");
    }

    @Override
    public void finalizar() {
        scrim.setFechaFinalizacion(LocalDateTime.now());
        System.out.println("Finalizando partida...");
        scrim.cambiarEstado(new FinalizadoState(scrim));
    }

    @Override
    public void cancelar() {
        throw new IllegalStateException("No se puede cancelar un scrim en juego.");
    }

    @Override
    public String getNombreEstado() {
        return "En Juego";
    }
}