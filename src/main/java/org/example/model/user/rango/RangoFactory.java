package org.example.model.user.rango;

import org.example.model.user.Perfil;

public class RangoFactory {

    public static IRangoState fromPuntaje(Perfil p) {
        int pts = p.getPuntaje();

        if (pts < 1000)  return new RangoHierro(p);
        if (pts < 2000)  return new RangoBronce(p);
        if (pts < 3000)  return new RangoPlata(p);
        if (pts < 4000)  return new RangoOro(p);
        if (pts < 5000)  return new RangoPlatino(p);
        if (pts < 6000)  return new RangoDiamante(p);
        if (pts < 7000)  return new RangoAscendente(p);
        if (pts < 8000)  return new RangoInmortal(p);
        return new RangoRadiante(p);
    }
}

