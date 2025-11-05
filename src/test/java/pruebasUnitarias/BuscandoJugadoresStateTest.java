package pruebasUnitarias;

import org.example.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BuscandoJugadoresStateTest {

    @Test
    public void testAgregarJugadorAgregaCorrectamenteYNoCambiaEstadoSiNoCompleta() {
        // Arrange

        Scrim scrim = new Scrim();
        scrim.setJugadoresPorLado(1); // 2 por lado = 4 total
        scrim.setEstado(new BuscandoJugadoresState(scrim));

        // Crear Perfil y Rango correctamente (RangoBronce necesita un Perfil en el constructor)
        Perfil perfil = new Perfil();
        RangoBronce rangoBronce = new RangoBronce(perfil);
        perfil.setRango(rangoBronce);

        // Constructor completo: id, email, passwordHash, saltBase64
        User jugador = new RegularUser("1", "test@mail.com", "hash123", "salt123");
        jugador.setPerfil(perfil);
        //jugador.setRegion(null);


        // Act
        scrim.getEstado().agregarJugador(jugador);

        // Assert
        assertEquals(1, scrim.getEquipoA().size());
        assertTrue(scrim.getEstado() instanceof BuscandoJugadoresState);

    }

    @Test
    public void testAgregarJugadorCompletaCupoYCambiaAEstadoLobby() {
        // Arrange
        Scrim scrim = new Scrim();

        scrim.setJugadoresPorLado(1); // 1 por lado = total 2
        scrim.setEstado(new BuscandoJugadoresState(scrim));

        // Perfil + rango para ambos jugadores
        Perfil perfil1 = new Perfil();
        RangoBronce rango1 = new RangoBronce(perfil1);
        perfil1.setRango(rango1);

        Perfil perfil2 = new Perfil();
        RangoBronce rango2 = new RangoBronce(perfil2);
        perfil2.setRango(rango2);

        User j1 = new RegularUser("1", "a@mail.com", "hashA", "saltA");
        User j2 = new RegularUser("2", "b@mail.com", "hashB", "saltB");

        j1.setPerfil(perfil1);
        j2.setPerfil(perfil2);

        // Act
        scrim.getEstado().agregarJugador(j1); // equipo A
        scrim.getEstado().agregarJugador(j2); // equipo B → completa cupo

        // Assert
        assertTrue(scrim.getEstado() instanceof LobbyArmadoState,
                "Al completar el cupo debe cambiar a LobbyArmadoState");

    }
}
