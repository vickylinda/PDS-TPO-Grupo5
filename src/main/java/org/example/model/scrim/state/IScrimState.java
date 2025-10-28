package org.example.model.scrim.state;

import org.example.model.Resultados;
import org.example.model.user.User;

public interface IScrimState {
    void agregarJugador(User usuario);
    void confirmar(User usuario);
    void iniciar();
    void finalizar();
    void cancelar();
    void cargarResultados(Resultados resultados);
    String getNombreEstado();
}