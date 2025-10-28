package org.example.model.user.rango;

public interface IRangoState {
    void upgrade();
    void downgrade();
    String getNombre();
    int getPuntajeMin();
    int getPuntajeMax();
    int getValorNivel();
}