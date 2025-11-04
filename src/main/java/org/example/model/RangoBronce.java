package org.example.model;

public class RangoBronce implements IRangoState {
    private final Perfil perfil;
    public RangoBronce(Perfil p){ this.perfil = p; }

    @Override public void upgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public void downgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public String getNombre(){ return "Bronce"; }
    @Override public int getPuntajeMin(){ return 1000; }
    @Override public int getPuntajeMax(){ return 1999; }
    @Override public int getValorNivel(){ return 2; }
}

