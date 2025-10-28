package org.example.model.user.rango;

import org.example.model.user.Perfil;

public class RangoDiamante implements IRangoState {
    private final Perfil perfil;
    public RangoDiamante(Perfil p){ this.perfil = p; }

    @Override public void upgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public void downgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public String getNombre(){ return "Diamante"; }
    @Override public int getPuntajeMin(){ return 5000; }
    @Override public int getPuntajeMax(){ return 5999; }
    @Override public int getValorNivel(){ return 6; }
}
