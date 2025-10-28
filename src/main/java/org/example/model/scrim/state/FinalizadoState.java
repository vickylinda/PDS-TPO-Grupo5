package org.example.model.scrim.state;

import org.example.model.Resultados;
import org.example.model.scrim.Scrim;
import org.example.model.user.User;

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
        System.out.println("Resultados cargados exitosamente");
    }

    @Override
    public String getNombreEstado() {
        return "Finalizado";
    }
}
