package org.example.model;

public class RangoRadiante implements IRangoState {
    private final Perfil perfil;
    public RangoRadiante(Perfil p){ this.perfil = p; }

    @Override public void upgrade(){ /* ya estás en el tope */ }
    @Override public void downgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public String getNombre(){ return "Radiante"; }
    @Override public int getPuntajeMin(){ return 8000; }
    @Override public int getPuntajeMax(){ return Integer.MAX_VALUE; }
    @Override public int getValorNivel(){ return 9; }
}
