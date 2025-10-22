package org.example.model;

public class RangoInmortal implements IRangoState {
    private final Perfil perfil;
    public RangoInmortal(Perfil p){ this.perfil = p; }

    @Override public void upgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public void downgrade(){ perfil.setRango(RangoFactory.fromPuntaje(perfil)); }
    @Override public String getNombre(){ return "Inmortal"; }
    @Override public int getPuntajeMin(){ return 7000; }  // 👈 empieza acá
    @Override public int getPuntajeMax(){ return 7999; }
    @Override public int getValorNivel(){ return 8; }
}
