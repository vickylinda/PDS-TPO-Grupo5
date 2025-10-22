package org.example.model;

public class RangoAscendente implements IRangoState {
    private final Perfil perfil;
    public RangoAscendente(Perfil p){ this.perfil = p; }

    @Override public void upgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public void downgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public String getNombre(){ return "Ascendente"; }
    @Override public int getPuntajeMin(){ return 6000; }
    @Override public int getPuntajeMax(){ return 6999; }
    @Override public int getValorNivel(){ return 7; }
}

