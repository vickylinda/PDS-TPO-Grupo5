package org.example.model;

public interface IScrimState {
    void agregarJugador(User usuario);
    void confirmar(User usuario);
    void iniciar();
    void finalizar();
    void cancelar();
    void cargarResultados(Resultados resultados);
    String getNombreEstado();
}