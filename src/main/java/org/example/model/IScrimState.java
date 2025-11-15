package org.example.model;

public interface IScrimState {
    String getNombreEstado();
}

class EstadoBuscandoJugadores implements IScrimState {
    @Override
    public String getNombreEstado() {
        return "Buscando jugadores";
    }
}
