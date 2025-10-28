package org.example.model.user.rango;

import org.example.model.user.Perfil;

public class RangoHierro implements IRangoState {
    private final Perfil perfil;
    public RangoHierro(Perfil p){ this.perfil = p; }

    @Override public void upgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public void downgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public String getNombre(){ return "Hierro"; }
    @Override public int getPuntajeMin(){ return 0; }
    @Override public int getPuntajeMax(){ return 999; }
    @Override public int getValorNivel(){ return 1; }
}
