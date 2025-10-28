package org.example.model.user.rango;

import org.example.model.user.Perfil;

public class RangoOro implements IRangoState {
    private final Perfil perfil;
    public RangoOro(Perfil p){ this.perfil = p; }

    @Override public void upgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public void downgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public String getNombre(){ return "Oro"; }
    @Override public int getPuntajeMin(){ return 3000; }
    @Override public int getPuntajeMax(){ return 3999; }
    @Override public int getValorNivel(){ return 4; }
}
