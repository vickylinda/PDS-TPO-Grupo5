package pruebasUnitarias;

import org.example.model.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class ScrimBuilderTest {

    @Test
    public void testCreaScrimValidoConParametrosMinimos() {
        // Arrange
        Juego juego = new Juego(1,"Valorant","Shoter Tactico");

        // Act
        Scrim scrim = ScrimBuilder.nuevo()
                .juego(juego)
                .formato(5)
                .region("LATAM")
                .fechaHora(LocalDateTime.now().plusDays(1))
                .build();

        // Assert
        assertEquals("Valorant", scrim.getJuego().getNombre());
        assertEquals(5, scrim.getJugadoresPorLado());
        assertEquals("LATAM", scrim.getRegion());
        assertNotNull(scrim.getEstado());
        assertTrue(scrim.getEstado() instanceof BuscandoJugadoresState);
    }

    @Test
    public void testLanzaExcepcionSiFaltaCampoObligatorio() {
        // Arrange
        Juego juego = new Juego(1,"Valorant","Shoter Tactico");

        // Act + Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            ScrimBuilder.nuevo()
                    .juego(juego)
                    .formato(5)
                    .build();
        });

        assertTrue(ex.getMessage().contains("región"));
    }

    @Test
    public void testLanzaExcepcionSiRangoInvalido() {
        Juego juego = new Juego(1,"Valorant","Shoter Tactico");

        // Act + Assert
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ScrimBuilder.nuevo()
                    .juego(juego)
                    .formato(5)
                    .region("LATAM")
                    .fechaHora(LocalDateTime.now().plusDays(1))
                    .rango(2000, 1000) // rango máximo menor al mínimo
                    .build();
        });

        assertTrue(ex.getMessage().contains("rango máximo"));
    }
}
