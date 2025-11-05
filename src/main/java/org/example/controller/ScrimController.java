package org.example.controller;

import org.example.model.scrim.*;
import org.example.model.user.Perfil;
import org.example.model.user.RegularUser;
import org.example.model.user.User;
import org.example.notifications.NotificationService;
import org.example.notifications.bus.DomainEventBus;
import org.example.notifications.events.ScrimCancelado;
import org.example.notifications.events.ScrimEnJuego;
import org.example.notifications.events.ScrimFinalizado;
import org.example.store.JsonStore;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ScrimController {

    // -------------------- Singleton --------------------
    private static volatile ScrimController INSTANCE;

    public static ScrimController getInstance(JsonStore store,
                                              NotificationService notifService,
                                              Session session) {
        if (INSTANCE == null) {
            synchronized (ScrimController.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ScrimController(store, notifService, session);
                }
            }
        }
        return INSTANCE;
    }

    /** Acceso sin parámetros, después de haber llamado al getInstance(...) con deps. */
    public static ScrimController getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("ScrimController no fue inicializado. Llamá getInstance(store, notifService, session) primero.");
        }
        return INSTANCE;
    }

    /** Útil para tests/manual: borra la instancia singleton. */
    public static void resetForTests() { INSTANCE = null; }
    // ---------------------------------------------------

    public static final Juego[] JUEGOS_DISPONIBLES = {
            new Juego(1,"League of Legends","MOBA 5v5 competitivo"),
            new Juego(2,"Valorant","FPS táctico 5v5"),
            new Juego(3,"Dota 2","MOBA estratégico"),
            new Juego(4,"Counter-Strike 2","FPS competitivo"),
            new Juego(5,"Overwatch 2","Hero shooter por equipos"),
            new Juego(6,"Rocket League","Fútbol con autos"),
            new Juego(7,"Fortnite","Battle Royale"),
            new Juego(8,"Apex Legends","Battle Royale con héroes")
    };

    private final JsonStore store;
    private final NotificationService notifService;
    private final Session session;

    private final List<Scrim> scrims = new ArrayList<>();

    // Constructor privado (propio del Singleton)
    private ScrimController(JsonStore store,
                            NotificationService notifService,
                            Session session) {
        this.store = store;
        this.notifService = notifService;
        this.session = session;
    }

    private void ensureLoggedIn() {
        if (session.getCurrentUser() == null) throw new IllegalStateException("Debes iniciar sesión primero.");
    }
    private void ensurePerfil() {
        if (session.getCurrentUser().getPerfil() == null)
            throw new IllegalStateException("Debes crear un perfil primero (opción 8).");
    }

    // ===== SCRIMS (mismo cuerpo que en Main) =====
    public void crearScrim(Scanner sc) {
        ensureLoggedIn(); ensurePerfil();

        System.out.println("\n" + "=".repeat(60));
        System.out.println("CREAR NUEVO SCRIM");
        System.out.println("=".repeat(60));

        System.out.println("\nJuegos disponibles:");
        for (int i = 0; i < JUEGOS_DISPONIBLES.length; i++) {
            System.out.println((i + 1) + ". " + JUEGOS_DISPONIBLES[i].getNombre());
        }
        System.out.print("Selecciona el juego (1-" + JUEGOS_DISPONIBLES.length + "): ");
        int juegoIdx = Integer.parseInt(sc.nextLine().trim()) - 1;
        Juego juego = JUEGOS_DISPONIBLES[juegoIdx];

        System.out.print("Jugadores por lado (ej: 5 para 5v5): ");
        int jugadoresPorLado = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Región (ej: LAN, NA, EUW): ");
        String region = sc.nextLine().trim().toUpperCase();

        System.out.println("Fecha y hora de inicio. Formato: yyyy-MM-dd HH:mm (ej: 2025-10-20 19:00):");
        String fechaStr = sc.nextLine().trim();
        LocalDateTime fechaHora;
        if (fechaStr.isEmpty()) fechaHora = LocalDateTime.now().plusMinutes(5);
        else fechaHora = LocalDateTime.parse(fechaStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        System.out.print("¿Deseas establecer requisitos de rango? (s/n): ");
        String establecerRango = sc.nextLine().trim().toLowerCase();

        ScrimBuilder builder = ScrimBuilder.nuevo()
                .juego(juego).formato(jugadoresPorLado).region(region).fechaHora(fechaHora);

        if (establecerRango.equals("s")) {
            System.out.print("Puntaje de Rango mínimo (1-8000): ");
            int rangoMin = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Puntaje de Rango máximo (1-8000): ");
            int rangoMax = Integer.parseInt(sc.nextLine().trim());
            builder.rango(rangoMin, rangoMax);
        }

        Scrim scrim = builder.build();
        scrim.setCreadorId(session.getCurrentUser().getId());
        scrims.add(scrim);

        scrim.notificarScrimCreado(session.getCurrentUser().getEmail());

        System.out.println("\n✅ Scrim creado exitosamente!");
        System.out.println("ID: " + scrim.getId());
        System.out.println("Estado: " + scrim.getNombreEstadoActual());
        System.out.println("Formato: " + scrim.getFormato());
        System.out.println("Región: " + scrim.getRegion().toUpperCase());
        System.out.println("Fecha: " + scrim.getFechaHora());
        System.out.println("Jugadores: " + scrim.getJugadoresActuales() + "/" + scrim.getCantidadTotalJugadores());
    }

    public void verScrimsDisponibles() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SCRIMS DISPONIBLES");
        System.out.println("=".repeat(80));

        if (scrims.isEmpty()) { System.out.println("No hay scrims disponibles."); return; }

        for (int i = 0; i < scrims.size(); i++) {
            Scrim s = scrims.get(i);
            System.out.println("\n[" + (i + 1) + "] ID: " + s.getId().substring(0, 8) + "...");
            System.out.println("    Estado: " + s.getNombreEstadoActual());
            System.out.println("    Juego: " + s.getJuego().getNombre());
            System.out.println("    Formato: " + s.getFormato());
            System.out.println("    Región: " + s.getRegion().toUpperCase());
            System.out.println("    Jugadores: " + s.getJugadoresActuales() + "/" + s.getCantidadTotalJugadores());
            System.out.println("    Fecha: " + s.getFechaHora());
        }
        System.out.println("=".repeat(80) + "\n");
    }

    public void unirseAScrim(Scanner sc) {
        ensureLoggedIn(); ensurePerfil();
        if (scrims.isEmpty()) { System.out.println("No hay scrims disponibles."); return; }

        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= scrims.size()) throw new IllegalArgumentException("Scrim inválido.");

        Scrim scrim = scrims.get(idx);
        try {
            scrim.agregarJugador(session.getCurrentUser());
            if (scrim.getJugadoresActuales() == scrim.getCantidadTotalJugadores()) {
                var p = new java.util.HashMap<String,String>();
                p.put("scrimId", scrim.getId());
                p.put("fechaHora", scrim.getFechaHora() != null ? scrim.getFechaHora().toString() : "próximamente");
                DomainEventBus.getInstance().publish(new org.example.notifications.events.LobbyCompleto(scrim.getId(), p));
            }
            System.out.println("✅ Te has unido al scrim exitosamente!");
            scrim.mostrarInfo();
        } catch (Exception e) {
            System.out.println("❌ No se pudo unir al scrim: " + e.getMessage());
        }
    }

    public void confirmarParticipacion(Scanner sc) {
        if (scrims.isEmpty()) { System.out.println("No hay scrims disponibles."); return; }
        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= scrims.size()) throw new IllegalArgumentException("Scrim inválido.");

        Scrim scrim = scrims.get(idx);
        try {
            scrim.confirmarJugador(session.getCurrentUser());
            var p = new java.util.HashMap<String,String>();
            p.put("scrimId", scrim.getId());
            p.put("juego", scrim.getJuego().getNombre());
            p.put("region", scrim.getRegion());
            p.put("fechaHora", scrim.getFechaHora().toString());
            p.put("to", session.getCurrentUser().getEmail());
            DomainEventBus.getInstance().publish(new org.example.notifications.events.ScrimConfirmado(scrim.getId(), p));
            System.out.println("✅ Participación confirmada!");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void iniciarPartida(Scanner sc) {
        if (scrims.isEmpty()) { System.out.println("No hay scrims disponibles."); return; }
        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= scrims.size()) throw new IllegalArgumentException("Scrim inválido.");

        Scrim scrim = scrims.get(idx);
        scrim.iniciarPartida();
        var payload = new java.util.HashMap<String,String>();
        payload.put("scrimId", scrim.getId());
        payload.put("juego", scrim.getJuego().getNombre());
        payload.put("region", scrim.getRegion());
        payload.put("fechaHora", scrim.getFechaHora() != null ? scrim.getFechaHora().toString() : "próximamente");
        DomainEventBus.getInstance().publish(new ScrimEnJuego(scrim.getId(), payload));
        System.out.println("📩 Notificación enviada: Scrim en juego");
    }

    public void finalizarPartida(Scanner sc) {
        if (scrims.isEmpty()) { System.out.println("No hay scrims disponibles."); return; }
        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= scrims.size()) throw new IllegalArgumentException("Scrim inválido.");

        Scrim scrim = scrims.get(idx);
        scrim.finalizarPartida();
        var payload = new java.util.HashMap<String,String>();
        payload.put("scrimId", scrim.getId());
        payload.put("juego", scrim.getJuego().getNombre());
        payload.put("region", scrim.getRegion());
        payload.put("fechaHora", scrim.getFechaHora() != null ? scrim.getFechaHora().toString() : "próximamente");
        DomainEventBus.getInstance().publish(new ScrimFinalizado(scrim.getId(), payload));
        System.out.println("📩 Notificación enviada: Scrim finalizado");
    }

    public void cancelarScrim(Scanner sc) {
        if (scrims.isEmpty()) { System.out.println("No hay scrims disponibles."); return; }
        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= scrims.size()) throw new IllegalArgumentException("Scrim inválido.");

        Scrim scrim = scrims.get(idx);
        scrim.cancelar();
        var payload = new java.util.HashMap<String,String>();
        payload.put("scrimId", scrim.getId());
        payload.put("juego", scrim.getJuego().getNombre());
        payload.put("region", scrim.getRegion());
        payload.put("fechaHora", scrim.getFechaHora() != null ? scrim.getFechaHora().toString() : "próximamente");
        DomainEventBus.getInstance().publish(new ScrimCancelado(scrim.getId(), payload));
        System.out.println("📩 Notificación enviada: Scrim cancelado");
        System.out.println("✅ Scrim cancelado.");
    }

    public void cargarResultados(Scanner sc) {
        if (scrims.isEmpty()) { System.out.println("No hay scrims disponibles."); return; }
        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= scrims.size()) throw new IllegalArgumentException("Scrim inválido.");

        Scrim scrim = scrims.get(idx);

        System.out.println("\n¿Cómo deseas cargar los resultados?");
        System.out.println("1. Aleatorio");
        System.out.println("2. Aleatorio realista");
        System.out.println("3. Especificar ganador");
        System.out.print("Opción: ");
        String opcion = sc.nextLine().trim();

        Resultados resultados = switch (opcion) {
            case "1" -> ResultadosGenerator.generarResultadosAleatorios(scrim);
            case "2" -> ResultadosGenerator.generarResultadosRealistas(scrim);
            case "3" -> {
                System.out.print("Equipo ganador (A/B): ");
                String ganador = sc.nextLine().trim().toUpperCase();
                yield ResultadosGenerator.generarConGanador(scrim, ganador);
            }
            default -> {
                System.out.println("⚠ Opción inválida, usando aleatorio.");
                yield ResultadosGenerator.generarResultadosAleatorios(scrim);
            }
        };

        try {
            scrim.cargarResultados(resultados);
            System.out.println("✅ Resultados cargados exitosamente!");
            ResultadosGenerator.mostrarResumen(resultados);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    public void demoCompletoScrim() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO COMPLETO: CICLO DE VIDA DE UN SCRIM");
        System.out.println("=".repeat(80));

        // Crear usuarios de prueba
        List<User> usuarios = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            RegularUser user = new RegularUser();
            user.setId("user-" + i);
            user.setEmail("jugador" + i + "@example.com");
            user.setRegion("LAN");

            Perfil perfil = new Perfil(i, JUEGOS_DISPONIBLES[0], "Todo el día");
            perfil.actualizarPuntaje(500 + (i * 100));
            user.setPerfil(perfil);

            usuarios.add(user);
        }

        // 1. Crear scrim
        System.out.println("\n PASO 1: Creando scrim 2v2...");
        Scrim scrim = ScrimBuilder.nuevo()
                .juego(JUEGOS_DISPONIBLES[0])
                .formato(2)
                .region("LAN")
                .fechaHora(LocalDateTime.now().plusSeconds(1))
                .rango(100, 10000)
                .build();

        System.out.println("   Estado: " + scrim.getNombreEstadoActual());

        // 2. Agregar jugadores
        System.out.println("\nPASO 2: Agregando jugadores...");
        for (int i = 0; i < 10; i++) {
            scrim.agregarJugador(usuarios.get(i));
            System.out.println("   Jugador " + (i + 1) + " agregado - " +
                    scrim.getJugadoresActuales() + "/" + scrim.getCantidadTotalJugadores());
        }
        System.out.println("   Estado: " + scrim.getNombreEstadoActual());

        // 3. Confirmar jugadores
        System.out.println("\nPASO 3: Confirmando jugadores...");
        for (int i = 0; i < 10; i++) {
            scrim.confirmarJugador(usuarios.get(i));
        }
        System.out.println("   Estado: " + scrim.getNombreEstadoActual());

        // 4. Iniciar partida
        System.out.println("\n PASO 4: Iniciando partida...");
        scrim.iniciarPartida();
        System.out.println("   Estado: " + scrim.getNombreEstadoActual());

        // 5. Finalizar partida
        System.out.println("\n PASO 5: Finalizando partida...");
        scrim.finalizarPartida();
        System.out.println("   Estado: " + scrim.getNombreEstadoActual());

        // 6. Cargar resultados
        System.out.println("\n PASO 6: Cargando resultados...");
        Resultados resultados = ResultadosGenerator.generarResultadosRealistas(scrim);
        scrim.cargarResultados(resultados);

        // Mostrar resumen de resultados
        ResultadosGenerator.mostrarResumen(resultados);

        // Mostrar información final
        System.out.println("\n" + "=".repeat(80));
        System.out.println(" INFORMACIÓN FINAL DEL SCRIM");
        System.out.println("=".repeat(80));
        scrim.mostrarInfo();
        System.out.println("Estado final: " + scrim.getNombreEstadoActual());
        System.out.println("Ganador: " + resultados.getGanadorEquipo());
        System.out.println("Fecha de finalización: " + scrim.getFechaFinalizacion());
        System.out.println("=".repeat(80) + "\n");

        // Demo de crear y cancelar
        System.out.println("\n DEMO EXTRA: Creando y cancelando un scrim...");
        Scrim scrimCancelado = ScrimBuilder.nuevo()
                .juego(JUEGOS_DISPONIBLES[1])
                .formato(3)
                .region("NA")
                .fechaHora(LocalDateTime.now().plusHours(1))
                .build();

        System.out.println("Estado antes de cancelar: " + scrimCancelado.getNombreEstadoActual());
        scrimCancelado.cancelar();
        System.out.println("Estado después de cancelar: " + scrimCancelado.getNombreEstadoActual());

        System.out.println("\n✅ Demo completo finalizado!");

        // Demo de cancelación
        System.out.println("\n DEMO EXTRA: Intentando cancelar un scrim finalizado...");
        scrim.cancelar();
    }
    public List<Scrim> getScrims() {return scrims;}
}