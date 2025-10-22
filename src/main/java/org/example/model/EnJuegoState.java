package org.example.model;

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
        System.out.println("No se puede cancelar un scrim que ya está en juego. Debe finalizarse.");
    }

    @Override
    public String getNombreEstado() {
        return "En Juego";
    }
}