package org.example.model.user.rango;

import org.example.model.user.Perfil;

public class RangoPlatino implements IRangoState {
    private final Perfil perfil;
    public RangoPlatino(Perfil p){ this.perfil = p; }

    @Override public void upgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public void downgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public String getNombre(){ return "Platino"; }
    @Override public int getPuntajeMin(){ return 4000; }
    @Override public int getPuntajeMax(){ return 4999; }
    @Override public int getValorNivel(){ return 5; }
}

