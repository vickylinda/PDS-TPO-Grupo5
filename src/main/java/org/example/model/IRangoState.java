package org.example.model;

public interface IRangoState {
    void upgrade();
    void downgrade();
    String getNombre();
    int getPuntajeMin();
    int getPuntajeMax();
    int getValorNivel();
}