package org.example.model.scrim.state;

import org.example.model.Resultados;
import org.example.model.scrim.Scrim;
import org.example.model.scrim.state.CanceladoState;
import org.example.model.scrim.state.IScrimState;
import org.example.model.user.User;

public abstract class ScrimStateBase implements IScrimState {
    protected Scrim scrim;

    public ScrimStateBase(Scrim scrim) {
        this.scrim = scrim;
    }

    @Override
    public void agregarJugador(User usuario) {
        throw new IllegalStateException("No se pueden agregar jugadores en el estado: " + getNombreEstado());
    }

    @Override
    public void confirmar(User usuario) {
        throw new IllegalStateException("No se puede confirmar en el estado: " + getNombreEstado());
    }

    @Override
    public void iniciar() {
        throw new IllegalStateException("No se puede iniciar en el estado: " + getNombreEstado());
    }

    @Override
    public void finalizar() {
        throw new IllegalStateException("No se puede finalizar en el estado: " + getNombreEstado());
    }

    @Override
    public void cancelar() {
        scrim.cambiarEstado(new CanceladoState(scrim));
    }

    @Override
    public void cargarResultados(Resultados resultados) {
        throw new IllegalStateException("No se pueden cargar resultados en el estado: " + getNombreEstado());
    }
}
