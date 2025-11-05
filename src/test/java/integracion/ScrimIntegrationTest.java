package integracion;

import org.example.model.*;
import org.example.notifications.bus.DomainEventBus;
import org.example.notifications.events.*;
import org.example.notifications.repo.InMemoryNotificationRepository;
import org.example.notifications.subscribers.NotificationSubscriber;
import org.example.notifications.NotificationService;
import org.example.notifications.repo.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;

public class ScrimIntegrationTest {
    private Juego juegoDemo;
    private List<User> usuarios;
    private NotificationRepository notifRepo;
    private NotificationService notifService;

    @BeforeEach
    void setup() {
        juegoDemo = new Juego(1, "Valorant", "FPS táctico 5v5");

        notifRepo = new InMemoryNotificationRepository();
        notifService = new NotificationService(
                notifRepo,
                "fake-api-key",
                "test@escrims.com",
                "eScrims Test"
        );

        DomainEventBus.getInstance().subscribe(new NotificationSubscriber(notifService));

        // Crear 10 usuarios con región válida
        usuarios = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            RegularUser user = new RegularUser();
            user.setId("user-" + i);
            user.setEmail("jugador" + i + "@test.com");
            user.setRegion("AMERICA"); // región igual a la del Scrim

            Perfil perfil = new Perfil(i, juegoDemo, "Noches");
            perfil.actualizarPuntaje(500 + (i * 50));
            user.setPerfil(perfil);

            usuarios.add(user);
        }
    }

    @Test
    void testFlujoCompletoScrim() {
        // === 1️⃣ Crear Scrim ===
        Scrim scrim = ScrimBuilder.nuevo()
                .juego(juegoDemo)
                .formato(5)
                .region("AMERICA")
                .fechaHora(LocalDateTime.now().plusHours(1))
                .rango(1, 10)
                .build();

        scrim.setCreadorId(usuarios.get(0).getId());
        assertEquals("Buscando Jugadores", scrim.getNombreEstadoActual());

        // === 2️⃣ Agregar jugadores ===
        for (User u : usuarios) {
            scrim.agregarJugador(u);
        }

        // Validar que el scrim esté completo y haya cambiado de estado
        assertEquals("Lobby Armado", scrim.getNombreEstadoActual());

        // === 3️⃣ Confirmar jugadores ===
        for (User u : usuarios) {
            scrim.confirmarJugador(u);
        }
        assertEquals("Confirmado", scrim.getNombreEstadoActual());

        // === 4️⃣ Simular inicio de partida ===
        scrim.setFechaHora(LocalDateTime.now().minusMinutes(5)); // Simula que ya es hora
        scrim.iniciarPartida();
        assertEquals("En Juego", scrim.getNombreEstadoActual());

        // === 5️⃣ Finalizar partida ===
        scrim.finalizarPartida();
        assertEquals("Finalizado", scrim.getNombreEstadoActual());

        // === 6️⃣ Cargar resultados ===
        Resultados resultados = new Resultados();
        resultados.registrarGanador("Equipo A");

        for (int i = 0; i < 5; i++) {
            Estadisticas stats = new Estadisticas();
            stats.setKills(10 + i);
            stats.setDeaths(5);
            stats.setAssists(8 + i);
            stats.setPuntaje(1000 + i * 100);
            resultados.agregarEstadistica(usuarios.get(i), stats);
        }

        scrim.cargarResultados(resultados);

        assertEquals("Finalizado", scrim.getNombreEstadoActual());
        assertEquals("Equipo A", resultados.getGanadorEquipo());

        // === 7️⃣ Verificar que haya notificaciones ===
        assertFalse(notifRepo.findAll().isEmpty(),
                "Debería haberse registrado al menos una notificación al publicarse eventos");
    }
}