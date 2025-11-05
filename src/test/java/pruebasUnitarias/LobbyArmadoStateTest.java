package pruebasUnitarias;

import org.example.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LobbyArmadoStateTest {

    @Test
    public void testTodosLosJugadoresConfirmanYCambiaAEstadoConfirmado() {
        // Arrange
        Scrim scrim = new Scrim();
        scrim.setJugadoresPorLado(1);
        scrim.setCantidadTotalJugadores(2);

        User j1 = new RegularUser("1", "a@mail.com", "hashA", "saltA");
        User j2 = new RegularUser("2", "b@mail.com", "hashB", "saltB");

        Perfil p1 = new Perfil(); RangoBronce r1 = new RangoBronce(p1); p1.setRango(r1);
        Perfil p2 = new Perfil(); RangoBronce r2 = new RangoBronce(p2); p2.setRango(r2);
        j1.setPerfil(p1); j2.setPerfil(p2);

        scrim.getEquipoA().add(j1);
        scrim.getEquipoB().add(j2);
        scrim.setEstado(new LobbyArmadoState(scrim));

        // Act
        scrim.getEstado().confirmar(j1);
        scrim.getEstado().confirmar(j2);

        // Assert
        assertTrue(scrim.getEstado() instanceof ConfirmadoState,
                "Cuando todos confirman, debe cambiar a ConfirmadoState");
    }

    @Test
    public void testJugadorQueNoPerteneceLanzaExcepcion() {
        // Arrange
        Scrim scrim = new Scrim();
        scrim.setJugadoresPorLado(1);
        scrim.setCantidadTotalJugadores(2);

        User jugadorValido = new RegularUser("1", "a@mail.com", "hashA", "saltA");
        User jugadorInvalido = new RegularUser("3", "x@mail.com", "hashX", "saltX");

        Perfil perfil = new Perfil(); RangoBronce rango = new RangoBronce(perfil); perfil.setRango(rango);
        jugadorValido.setPerfil(perfil);
        jugadorInvalido.setPerfil(perfil);

        scrim.getEquipoA().add(jugadorValido);
        scrim.setEstado(new LobbyArmadoState(scrim));

        // Act + Assert
        assertThrows(IllegalArgumentException.class,
                () -> scrim.getEstado().confirmar(jugadorInvalido),
                "Debe lanzar excepción si el jugador no pertenece al scrim");
    }
}
