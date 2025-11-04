package org.example.model;

public class RangoPlata implements IRangoState {
    private final Perfil perfil;
    public RangoPlata(Perfil p){ this.perfil = p; }

    @Override public void upgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public void downgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public String getNombre(){ return "Plata"; }
    @Override public int getPuntajeMin(){ return 2000; }
    @Override public int getPuntajeMax(){ return 2999; }
    @Override public int getValorNivel(){ return 3; }
}
