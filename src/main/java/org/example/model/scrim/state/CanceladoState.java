package org.example.model.scrim.state;

import org.example.model.scrim.Scrim;
import org.example.model.user.User;

public class CanceladoState extends ScrimStateBase {

    public CanceladoState(Scrim scrim) {
        super(scrim);
        scrim.setFechaFinalizacion(java.time.LocalDateTime.now());
    }

    @Override
    public void agregarJugador(User usuario) {
        System.out.println("El scrim ha sido cancelado.");
    }

    @Override
    public void confirmar(User usuario) {
        System.out.println("El scrim ha sido cancelado.");
    }

    @Override
    public void iniciar() {
        System.out.println("El scrim ha sido cancelado.");
    }

    @Override
    public void finalizar() {
        System.out.println("El scrim ya fue cancelado.");
    }

    @Override
    public void cancelar() {
        System.out.println("El scrim ya está cancelado.");
    }

    @Override
    public String getNombreEstado() {
        return "Cancelado";
    }
}