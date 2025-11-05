package pruebasUnitarias;

import org.example.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RangoFactoryTest {

    @Test
    public void testRetornaRangoHierroCuandoPuntajeMenorA1000() {
        Perfil perfil = new Perfil();
        perfil.setPuntaje(500);
        IRangoState rango = RangoFactory.fromPuntaje(perfil);
        assertTrue(rango instanceof RangoHierro);
    }

    @Test
    public void testRetornaRangoBronceCuandoPuntajeEntre1000y1999() {
        Perfil perfil = new Perfil();
        perfil.setPuntaje(1500);
        IRangoState rango = RangoFactory.fromPuntaje(perfil);
        assertTrue(rango instanceof RangoBronce);
    }

    @Test
    public void testRetornaRangoPlataCuandoPuntajeEntre2000y2999() {
        Perfil perfil = new Perfil();
        perfil.setPuntaje(2500);
        IRangoState rango = RangoFactory.fromPuntaje(perfil);
        assertTrue(rango instanceof RangoPlata);
    }

    @Test
    public void testRetornaRangoOroCuandoPuntajeEntre3000y3999() {
        Perfil perfil = new Perfil();
        perfil.setPuntaje(3500);
        IRangoState rango = RangoFactory.fromPuntaje(perfil);
        assertTrue(rango instanceof RangoOro);
    }

    @Test
    public void testRetornaRangoPlatinoCuandoPuntajeEntre4000y4999() {
        Perfil perfil = new Perfil();
        perfil.setPuntaje(4500);
        IRangoState rango = RangoFactory.fromPuntaje(perfil);
        assertTrue(rango instanceof RangoPlatino);
    }

    @Test
    public void testRetornaRangoDiamanteCuandoPuntajeEntre5000y5999() {
        Perfil perfil = new Perfil();
        perfil.setPuntaje(5500);
        IRangoState rango = RangoFactory.fromPuntaje(perfil);
        assertTrue(rango instanceof RangoDiamante);
    }

    @Test
    public void testRetornaRangoAscendenteCuandoPuntajeEntre6000y6999() {
        Perfil perfil = new Perfil();
        perfil.setPuntaje(6500);
        IRangoState rango = RangoFactory.fromPuntaje(perfil);
        assertTrue(rango instanceof RangoAscendente);
    }

    @Test
    public void testRetornaRangoInmortalCuandoPuntajeEntre7000y7999() {
        Perfil perfil = new Perfil();
        perfil.setPuntaje(7500);
        IRangoState rango = RangoFactory.fromPuntaje(perfil);
        assertTrue(rango instanceof RangoInmortal);
    }

    @Test
    public void testRetornaRangoRadianteCuandoPuntajeMayorOIgualA8000() {
        Perfil perfil = new Perfil();
        perfil.setPuntaje(9000);
        IRangoState rango = RangoFactory.fromPuntaje(perfil);
        assertTrue(rango instanceof RangoRadiante);
    }
}