package org.example;

import org.example.model.*;
import org.example.service.AuthService;
import org.example.service.MatchmakingService;
import org.example.store.JsonStore;

import java.io.Console;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    // Lista de juegos disponibles
    private static final Juego[] JUEGOS_DISPONIBLES = {
            new Juego(1, "League of Legends", "MOBA 5v5 competitivo"),
            new Juego(2, "Valorant", "FPS táctico 5v5"),
            new Juego(3, "Dota 2", "MOBA estratégico"),
            new Juego(4, "Counter-Strike 2", "FPS competitivo"),
            new Juego(5, "Overwatch 2", "Hero shooter por equipos"),
            new Juego(6, "Rocket League", "Fútbol con autos"),
            new Juego(7, "Fortnite", "Battle Royale"),
            new Juego(8, "Apex Legends", "Battle Royale con héroes")
    };
    // Lista para almacenar scrims creados
    private static final List<Scrim> scrims = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        JsonStore store = new JsonStore();
        AuthService auth = new AuthService(store);

        Scanner sc = new Scanner(System.in);
        Console console = System.console();
        String token = null;
        User currentUser = null;

        System.out.println("Archivo de datos: " + JsonStore.dataFilePath());

        while (true) {
            System.out.println("""
                    ====== MENU ======
                    === AUTENTICACIÓN ===
                    1) Registrar usuario
                    2) Login
                    3) Login con Google
                    4) WhoAmI (requiere token)
                    5) Listar usuarios (MOD/ADMIN)
                    6) Promover a MOD/ADMIN (solo ADMIN)
                    7) Logout
                    
                    === PERFIL Y RANGOS ===
                    8) Crear/Actualizar Perfil
                    9) Agregar Rol Preferido
                    10) Ver Mi Perfil
                    11) Actualizar Puntaje (simular progresión)
                    12) Ver Información de Rango
                    13) Probar Sistema de Rangos (demo completo)
                    
                    === GESTIÓN DE SCRIMS ===
                    14) Crear Scrim
                    15) Ver Scrims Disponibles
                    16) Unirse a un Scrim
                    17) Confirmar Participación
                    18) Iniciar Partida
                    19) Finalizar Partida
                    20) Cancelar Scrim
                    21) Cargar Resultados
                    22) Demo Completo de Scrim
                   
                    0) Salir
                    """);
            System.out.print("Opción: ");
            String op = sc.nextLine().trim();

            try {
                switch (op) {
                    case "1" -> {
                        System.out.print("Email: ");
                        String email = sc.nextLine().trim();
                        char[] pass1 = readPassword(console, sc, "Password: ");
                        char[] pass2 = readPassword(console, sc, "Confirmar password: ");
                        if (!java.util.Arrays.equals(pass1, pass2))
                            throw new IllegalArgumentException("Las contraseñas no coinciden.");
                        auth.register(email, pass1);
                        java.util.Arrays.fill(pass1, '\0');
                        java.util.Arrays.fill(pass2, '\0');
                        System.out.println("✅ Usuario registrado.");
                    }
                    case "2" -> {
                        System.out.print("Email: ");
                        String email = sc.nextLine().trim();
                        char[] pass = readPassword(console, sc, "Password: ");
                        token = auth.login(email, pass);
                        var userOpt = store.findUserByEmail(email);
                        if (userOpt.isPresent()) currentUser = userOpt.get();
                        System.out.println("✅ Login OK. Token:\n" + token);
                    }
                    case "3" -> {
                        String clientId = System.getenv("GOOGLE_OAUTH_CLIENT_ID");
                        String clientSecret = System.getenv("GOOGLE_OAUTH_CLIENT_SECRET");
                        if (clientId == null || clientId.isBlank())
                            throw new IllegalStateException("Configura GOOGLE_OAUTH_CLIENT_ID en variables de entorno.");
                        token = auth.loginWithGoogle(clientId, clientSecret);
                        // ⚠️ Cargar currentUser desde el JSON usando el email del token
                        String email = auth.whoAmI(token);
                        currentUser = store.findUserByEmail(email).orElseThrow();
                        System.out.println("✅ Login Google OK. Token local:\n" + token);
                    }
                    case "4" -> {
                        ensureToken(token);
                        String email = auth.whoAmI(token);
                        var u = store.findUserByEmail(email).orElseThrow();
                        System.out.println("Usuario: " + u.getEmail());
                        System.out.println("Rol: " + u.getRoleName());
                        System.out.println("Google: " + (u.isInicioSesionConGoogle() ? "Sí" : "No"));
                    }
                    case "5" -> {
                        ensureToken(token);
                        var list = auth.listUsers(token);
                        System.out.println("=== Usuarios ===");
                        for (var u : list) {
                            System.out.printf("- %-30s  [%s]  google:%s%n",
                                    u.getEmail(), u.getRoleName(), u.isInicioSesionConGoogle() ? "sí" : "no");
                        }
                    }
                    case "6" -> {
                        ensureToken(token);
                        System.out.print("Email destino: ");
                        String emailDst = sc.nextLine().trim();
                        System.out.print("Nuevo rol (MOD/ADMIN): ");
                        String rol = sc.nextLine().trim().toUpperCase();
                        if ("MOD".equals(rol)) {
                            auth.promoteToModerator(token, emailDst);
                        } else if ("ADMIN".equals(rol)) {
                            auth.promoteToAdmin(token, emailDst);
                        } else {
                            throw new IllegalArgumentException("Rol inválido.");
                        }
                        System.out.println("✔ Rol actualizado.");
                    }
                    case "7" -> {
                        token = null;
                        currentUser = null;
                        System.out.println("✅ Logout OK.");
                    }
                    case "8" -> {
                        ensureLoggedIn(currentUser);
                        // Mostrar juegos disponibles
                        System.out.println("\nJuegos disponibles:");
                        for (int i = 0; i < JUEGOS_DISPONIBLES.length; i++) {
                            System.out.println((i + 1) + ". " + JUEGOS_DISPONIBLES[i].getNombre() +
                                    " - " + JUEGOS_DISPONIBLES[i].getDescripcion());
                        }
                        System.out.print("Selecciona tu juego principal (1-" + JUEGOS_DISPONIBLES.length + "): ");
                        int juegoIdx = Integer.parseInt(sc.nextLine().trim()) - 1;
                        if (juegoIdx < 0 || juegoIdx >= JUEGOS_DISPONIBLES.length)
                            throw new IllegalArgumentException("Opción de juego inválida.");
                        Juego juegoSeleccionado = JUEGOS_DISPONIBLES[juegoIdx];

                        System.out.print("Región (ej: LAN, NA, EUW): ");
                        String regionElegida = sc.nextLine().trim().toUpperCase();

                        System.out.print("Disponibilidad horaria (ej: Noches 8pm-12am): ");
                        String disponibilidad = sc.nextLine().trim();

                        if (currentUser.getPerfil() == null) {
                            Perfil perfil = new Perfil(
                                    Integer.parseInt(currentUser.getId().substring(0, 8), 16),
                                    juegoSeleccionado,
                                    disponibilidad
                            );
                            currentUser.setPerfil(perfil);
                            System.out.println("✅ Perfil creado exitosamente.");
                        } else {
                            currentUser.getPerfil().setJuegoPrincipal(juegoSeleccionado);
                            currentUser.getPerfil().setDisponibilidadHoraria(disponibilidad);
                            System.out.println("✅ Perfil actualizado.");
                        }
                        currentUser.setRegion(regionElegida);
                        store.updateUser(currentUser);
                        mostrarPerfil(currentUser);
                    }
                    case "9" -> {
                        ensureLoggedIn(currentUser);
                        ensurePerfil(currentUser);

                        System.out.println("\nRoles disponibles:");
                        System.out.println("1. Support - Rol de apoyo al equipo");
                        System.out.println("2. ADC - Tirador/Carry");
                        System.out.println("3. Mid - Línea central");
                        System.out.println("4. Top - Línea superior");
                        System.out.println("5. Jungle - Jungla");
                        System.out.print("Selecciona un rol (1-5): ");
                        String rolOp = sc.nextLine().trim();

                        Rol rol = switch (rolOp) {
                            case "1" -> new Rol(1, "Support", "Rol de apoyo al equipo");
                            case "2" -> new Rol(2, "ADC", "Tirador/Carry");
                            case "3" -> new Rol(3, "Mid", "Línea central");
                            case "4" -> new Rol(4, "Top", "Línea superior");
                            case "5" -> new Rol(5, "Jungle", "Jungla");
                            default -> throw new IllegalArgumentException("Opción inválida");
                        };

                        currentUser.getPerfil().agregarRolPreferido(rol);
                        store.updateUser(currentUser);

                        System.out.println("✅ Rol agregado: " + rol.getNombre());
                        mostrarRolesPreferidos(currentUser);
                    }
                    case "10" -> {
                        ensureLoggedIn(currentUser);
                        ensurePerfil(currentUser);
                        mostrarPerfilCompleto(currentUser);
                    }
                    case "11" -> {
                        ensureLoggedIn(currentUser);
                        ensurePerfil(currentUser);

                        System.out.print("Ingresa el nuevo puntaje: ");
                        int delta = Integer.parseInt(sc.nextLine().trim());

                        String rangoAnterior = currentUser.getPerfil().getRango().getNombre();
                        currentUser.getPerfil().actualizarPuntaje(delta);
                        String rangoActual = currentUser.getPerfil().getRango().getNombre();

                        store.updateUser(currentUser);

                        System.out.println("\nActualización de puntaje:");
                        System.out.println("  Puntaje total: " + currentUser.getPerfil().getPuntaje());
                        System.out.println("  Rango anterior: " + rangoAnterior);
                        System.out.println("  Rango actual:   " + rangoActual);
                        if (!rangoAnterior.equals(rangoActual)) {
                            System.out.println("¡Has cambiado de rango!");
                        }
                    }
                    case "12" -> {
                        ensureLoggedIn(currentUser);
                        ensurePerfil(currentUser);
                        mostrarInfoRango(currentUser);
                    }
                    case "13" -> {
                        demoSistemaRangos();
                    }

                    // ===== SCRIMS (solo memoria) =====
                    case "14" -> {
                        ensureLoggedIn(currentUser);
                        ensurePerfil(currentUser);
                        crearScrim(sc, currentUser);
                    }
                    case "15" -> verScrimsDisponibles();
                    case "16" -> {
                        ensureLoggedIn(currentUser);
                        ensurePerfil(currentUser);
                        unirseAScrim(sc, currentUser);
                    }
                    case "17" -> {
                        ensureLoggedIn(currentUser);
                        confirmarParticipacion(sc, currentUser);
                    }
                    case "18" -> iniciarPartida(sc);
                    case "19" -> finalizarPartida(sc);
                    case "20" -> cancelarScrim(sc);
                    case "21" -> cargarResultados(sc);
                    case "22" -> demoCompletoScrim();

                    case "0" -> {
                        System.out.println("¡Hasta luego! :) ");
                        return;
                    }
                    default -> System.out.println("❌ Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    // ===== MÉTODOS DE SCRIM =====

    private static void crearScrim(Scanner sc, User currentUser) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("CREAR NUEVO SCRIM");
        System.out.println("=".repeat(60));

        // Seleccionar juego
        System.out.println("\nJuegos disponibles:");
        for (int i = 0; i < JUEGOS_DISPONIBLES.length; i++) {
            System.out.println((i + 1) + ". " + JUEGOS_DISPONIBLES[i].getNombre());
        }
        System.out.print("Selecciona el juego (1-" + JUEGOS_DISPONIBLES.length + "): ");
        int juegoIdx = Integer.parseInt(sc.nextLine().trim()) - 1;
        Juego juego = JUEGOS_DISPONIBLES[juegoIdx];

        // Configurar formato
        System.out.print("Jugadores por lado (ej: 5 para 5v5): ");
        int jugadoresPorLado = Integer.parseInt(sc.nextLine().trim());

        // Región
        System.out.print("Región (ej: LAN, NA, EUW): ");
        String region = sc.nextLine().trim().toUpperCase();

        // Fecha y hora
        System.out.println("Fecha y hora de inicio. Formato: yyyy-MM-dd HH:mm (ej: 2025-10-20 19:00):");
        String fechaStr = sc.nextLine().trim();
        LocalDateTime fechaHora;
        if (fechaStr.isEmpty()) {
            fechaHora = LocalDateTime.now().plusMinutes(5);
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            fechaHora = LocalDateTime.parse(fechaStr, formatter);
        }

        // Requisitos de rango (opcional)
        System.out.print("¿Deseas establecer requisitos de rango? (s/n): ");
        String establecerRango = sc.nextLine().trim().toLowerCase();

        ScrimBuilder builder = ScrimBuilder.nuevo()
                .juego(juego)
                .formato(jugadoresPorLado)
                .region(region)
                .fechaHora(fechaHora);

        if (establecerRango.equals("s")) {
            System.out.print("Rango mínimo (1-10): ");
            int rangoMin = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Rango máximo (1-10): ");
            int rangoMax = Integer.parseInt(sc.nextLine().trim());
            builder.rango(rangoMin, rangoMax);
        }

        Scrim scrim = builder.build();
        scrim.setCreadorId(currentUser.getId());
        scrims.add(scrim);

        System.out.println("\n✅ Scrim creado exitosamente!");
        System.out.println("ID: " + scrim.getId());
        System.out.println("Estado: " + scrim.getNombreEstadoActual());
        System.out.println("Formato: " + scrim.getFormato());
        System.out.println("Región: " + scrim.getRegion().toUpperCase());
        System.out.println("Fecha: " + scrim.getFechaHora());
        System.out.println("Jugadores: " + scrim.getJugadoresActuales() + "/" + scrim.getCantidadTotalJugadores());
    }

    private static void verScrimsDisponibles() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SCRIMS DISPONIBLES");
        System.out.println("=".repeat(80));

        if (scrims.isEmpty()) {
            System.out.println("No hay scrims disponibles.");
            return;
        }

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

    private static void unirseAScrim(Scanner sc, User currentUser) {
        if (scrims.isEmpty()) {
            System.out.println("No hay scrims disponibles.");
            return;
        }

        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;

        if (idx < 0 || idx >= scrims.size()) {
            throw new IllegalArgumentException("Scrim inválido.");
        }

        Scrim scrim = scrims.get(idx);

        try {
            scrim.agregarJugador(currentUser);
            System.out.println("✅ Te has unido al scrim exitosamente!");
            scrim.mostrarInfo();
        } catch (Exception e) {
            System.out.println("❌ No se pudo unir al scrim: " + e.getMessage());
        }
    }

    private static void confirmarParticipacion(Scanner sc, User currentUser) {
        if (scrims.isEmpty()) {
            System.out.println("No hay scrims disponibles.");
            return;
        }

        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;

        if (idx < 0 || idx >= scrims.size()) {
            throw new IllegalArgumentException("Scrim inválido.");
        }

        Scrim scrim = scrims.get(idx);

        try {
            scrim.confirmarJugador(currentUser);
            System.out.println("✅ Participación confirmada!");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void iniciarPartida(Scanner sc) {
        if (scrims.isEmpty()) {
            System.out.println("No hay scrims disponibles.");
            return;
        }

        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;

        if (idx < 0 || idx >= scrims.size()) {
            throw new IllegalArgumentException("Scrim inválido.");
        }

        Scrim scrim = scrims.get(idx);
        scrim.iniciarPartida();
    }

    private static void finalizarPartida(Scanner sc) {
        if (scrims.isEmpty()) {
            System.out.println("No hay scrims disponibles.");
            return;
        }

        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;

        if (idx < 0 || idx >= scrims.size()) {
            throw new IllegalArgumentException("Scrim inválido.");
        }

        Scrim scrim = scrims.get(idx);
        scrim.finalizarPartida();
    }

    private static void cancelarScrim(Scanner sc) {
        if (scrims.isEmpty()) {
            System.out.println("No hay scrims disponibles.");
            return;
        }

        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;

        if (idx < 0 || idx >= scrims.size()) {
            throw new IllegalArgumentException("Scrim inválido.");
        }

        Scrim scrim = scrims.get(idx);
        scrim.cancelar();
        System.out.println("✅ Scrim cancelado.");
    }

    private static void cargarResultados(Scanner sc) {
        if (scrims.isEmpty()) {
            System.out.println("No hay scrims disponibles.");
            return;
        }

        verScrimsDisponibles();
        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;

        if (idx < 0 || idx >= scrims.size()) {
            throw new IllegalArgumentException("Scrim inválido.");
        }

        Scrim scrim = scrims.get(idx);

        System.out.print("Equipo ganador (A/B): ");
        String ganador = sc.nextLine().trim().toUpperCase();

        Resultados resultados = new Resultados();
        resultados.registrarGanador("Equipo " + ganador);

        try {
            scrim.cargarResultados(resultados);
            System.out.println("✅ Resultados cargados exitosamente!");
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private static void demoCompletoScrim() {
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
        System.out.println("\n PASO 1: Creando scrim 5v5...");
        Scrim scrim = ScrimBuilder.nuevo()
                .juego(JUEGOS_DISPONIBLES[0])
                .formato(5)
                .region("LAN")
                .fechaHora(LocalDateTime.now().plusMinutes(10))
                .rango(1, 10)
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
        // 3.5. Emparejamiento automático con estrategias
System.out.println("\nPASO 3.5: Emparejamiento automático (Matchmaking)\n");

MatchmakingService matchmaking = new MatchmakingService();

// Estrategia 1: Por MMR
System.out.println("→ Estrategia: ByMMRStrategy (rango ±300)");
matchmaking.setStrategy(new ByMMRStrategy(300));
List<User> seleccionMMR = matchmaking.emparejarJugadores(scrim, usuarios);
System.out.println("Jugadores seleccionados por MMR: " + seleccionMMR.size());
for (User u : seleccionMMR) {
    System.out.println("   • " + u.getEmail() + " | Puntaje: " + u.getPerfil().getPuntaje());
}

// Estrategia 2: Por Latencia
System.out.println("\n→ Estrategia: ByLatencyStrategy (umbral 100ms)");
matchmaking.setStrategy(new ByLatencyStrategy(100));
List<User> seleccionLatencia = matchmaking.emparejarJugadores(scrim, usuarios);
System.out.println("Jugadores seleccionados por Latencia: " + seleccionLatencia.size());

// Estrategia 3: Híbrida (FairPlay y Abandonos)
System.out.println("\n→ Estrategia: HybridStrategy (pesoAbandonos=0.4, pesoFairPlay=1.0)");
matchmaking.setStrategy(new HybridStrategy(0.4, 1.0));
List<User> seleccionHybrid = matchmaking.emparejarJugadores(scrim, usuarios);
System.out.println("Jugadores seleccionados por FairPlay/Reputación: " + seleccionHybrid.size());

        // Armar equipos finales con los jugadores seleccionados por MMR
        System.out.println("\n→ Armando equipos equilibrados con los jugadores seleccionados...");
        var equipos = matchmaking.armarEquipos(seleccionMMR, scrim);
        for (Equipo e : equipos) {
            System.out.println("Equipo: " + e.getNombre());
            for (User jugador : e.getJugadores()) {
                System.out.println("   - " + jugador.getEmail());
            }
        }
        System.out.println("\n✅ Emparejamiento automático finalizado.\n");


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
        Resultados resultados = new Resultados();
        resultados.registrarGanador("Equipo A");

        for (int i = 0; i < 5; i++) {
            Estadisticas stats = new Estadisticas();
            stats.setKills(10 + i);
            stats.setDeaths(5);
            stats.setAssists(8 + i);
            stats.setPuntaje(1000 + (i * 100));
            resultados.agregarEstadistica(usuarios.get(i), stats);
        }

        scrim.cargarResultados(resultados);

        // Mostrar información final
        System.out.println("\n" + "=".repeat(80));
        System.out.println(" INFORMACIÓN FINAL DEL SCRIM");
        System.out.println("=".repeat(80));
        scrim.mostrarInfo();
        System.out.println("Estado final: " + scrim.getNombreEstadoActual());
        System.out.println("Ganador: " + resultados.getGanadorEquipo());
        System.out.println("Fecha de finalización: " + scrim.getFechaFinalizacion());
        System.out.println("=".repeat(80) + "\n");

        // Demo de cancelación
        System.out.println("\n DEMO EXTRA: Intentando cancelar un scrim finalizado...");
        scrim.cancelar();

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
    }

    private static char[] readPassword(Console console, Scanner sc, String prompt) {
        if (console != null) return console.readPassword(prompt);
        System.out.print(prompt);
        return sc.nextLine().toCharArray();
    }

    private static void ensureToken(String token) {
        if (token == null)
            throw new IllegalStateException("Primero hacé login para obtener un token.");
    }

    private static void ensureLoggedIn(User user) {
        if (user == null)
            throw new IllegalStateException("Debes iniciar sesión primero.");
    }

    private static void ensurePerfil(User user) {
        if (user.getPerfil() == null)
            throw new IllegalStateException("Debes crear un perfil primero (opción 8).");
    }

    private static void mostrarPerfil(User user) {
        Perfil p = user.getPerfil();
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" PERFIL DE " + user.getEmail());
        System.out.println("=".repeat(50));
        System.out.println("Juego principal: " + p.getJuegoPrincipal().getNombre());
        System.out.println("Región: " + user.getRegion().toUpperCase());
        System.out.println("Disponibilidad: " + p.getDisponibilidadHoraria());
        System.out.println("Puntaje: " + p.getPuntaje());
        System.out.println("Rango: " + p.getRango().getNombre());
        System.out.println("=".repeat(50) + "\n");
    }

    private static void mostrarRolesPreferidos(User user) {
        Perfil p = user.getPerfil();
        System.out.println("\n Roles preferidos:");
        if (p.getRolesPreferidos().isEmpty()) {
            System.out.println("   (ninguno)");
        } else {
            for (Rol rol : p.getRolesPreferidos()) {
                System.out.println("   • " + rol.getNombre() + " - " + rol.getDescripcion());
            }
        }
        System.out.println();
    }

    private static void mostrarPerfilCompleto(User user) {
        Perfil p = user.getPerfil();
        System.out.println("\n" + "=".repeat(60));
        System.out.println(" PERFIL COMPLETO DE " + user.getEmail());
        System.out.println("=".repeat(60));
        System.out.println("ID: " + user.getId());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Activo: " + (user.isActive() ? "Sí" : "No"));
        System.out.println("Login con Google: " + (user.isInicioSesionConGoogle() ? "Sí" : "No"));
        System.out.println();
        System.out.println("--- Información del Perfil ---");
        System.out.println("Juego principal: " + p.getJuegoPrincipal().getNombre());
        System.out.println("Descripción: " + p.getJuegoPrincipal().getDescripcion());
        System.out.println("Región: " + user.getRegion().toUpperCase());
        System.out.println("Disponibilidad: " + p.getDisponibilidadHoraria());
        System.out.println("Puntaje: " + p.getPuntaje());
        System.out.println();
        System.out.println("--- Rango Actual ---");
        System.out.println("Nombre: " + p.getRango().getNombre());
        System.out.println("Nivel: " + p.getRango().getValorNivel());
        System.out.println("Rango de puntaje: " + p.getRango().getPuntajeMin() + " - " + p.getRango().getPuntajeMax());
        System.out.println();
        System.out.println("--- Roles Preferidos ---");
        if (p.getRolesPreferidos().isEmpty()) {
            System.out.println("(ninguno)");
        } else {
            for (Rol rol : p.getRolesPreferidos()) {
                System.out.println("• " + rol.getNombre() + " - " + rol.getDescripcion());
            }
        }
        System.out.println("=".repeat(60) + "\n");
    }

    private static void mostrarInfoRango(User user) {
        Perfil p = user.getPerfil();
        var rango = p.getRango();

        System.out.println("\n" + "=".repeat(50));
        System.out.println(" INFORMACIÓN DE RANGO");
        System.out.println("=".repeat(50));
        System.out.println("Rango actual: " + rango.getNombre());
        System.out.println("Nivel: " + rango.getValorNivel());
        System.out.println("Puntaje actual: " + p.getPuntaje());
        System.out.println("Rango de puntaje: " + rango.getPuntajeMin() + " - " + rango.getPuntajeMax());

        int puntosParaSiguiente = rango.getPuntajeMax() + 1 - p.getPuntaje();
        if (puntosParaSiguiente > 0) {
            System.out.println("Puntos para siguiente rango: " + puntosParaSiguiente);
        } else {
            System.out.println("¡Has alcanzado el máximo de este rango!");
        }
        System.out.println("=".repeat(50) + "\n");
    }

    private static void demoSistemaRangos() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(" DEMO: SISTEMA DE RANGOS");
        System.out.println("=".repeat(60));

        // Crear un perfil de prueba con un juego
        Juego juegoDemo = new Juego(1, "League of Legends", "MOBA 5v5 competitivo");
        Perfil perfil = new Perfil(999, juegoDemo, "Todo el día");

        // Agregar algunos roles
        perfil.agregarRolPreferido(new Rol(1, "Support", "Rol de apoyo"));
        perfil.agregarRolPreferido(new Rol(3, "Mid", "Línea central"));

        System.out.println("Perfil de demostración creado:");
        System.out.println("  • Juego: " + perfil.getJuegoPrincipal().getNombre());
        System.out.println("  • Roles: " + perfil.getRolesPreferidos().size() + " roles preferidos");
        System.out.println("\n Simulando progresión de rangos...\n");

        // Simular progresión
        int[] puntajes = {0, 500, 1200, 2100, 2800, 3500, 2500, 900};

        for (int puntaje : puntajes) {
            String rangoAntes = perfil.getRango().getNombre();
            perfil.actualizarPuntaje(puntaje);
            String rangoDespues = perfil.getRango().getNombre();

            System.out.println("Puntaje: " + String.format("%4d", puntaje) +
                    " | Rango: " + String.format("%-10s", rangoDespues) +
                    " | Nivel: " + perfil.getRango().getValorNivel());

            if (!rangoAntes.equals(rangoDespues)) {
                if (perfil.getRango().getValorNivel() > getNivelPorNombre(rangoAntes)) {
                    System.out.println("           ⬆️  ¡SUBISTE DE RANGO!");
                } else {
                    System.out.println("           ⬇️  Bajaste de rango");
                }
            }
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("Resumen final:");
        System.out.println("  • Juego: " + perfil.getJuegoPrincipal().getNombre());
        System.out.println("  • Rango: " + perfil.getRango().getNombre());
        System.out.println("  • Nivel: " + perfil.getRango().getValorNivel());
        System.out.println("  • Puntaje: " + perfil.getPuntaje());
        System.out.println("  • Roles preferidos: " + perfil.getRolesPreferidos().size());
        System.out.println("=".repeat(60) + "\n");
    }

    private static int getNivelPorNombre(String nombre) {
        return switch (nombre) {
            case "Hierro" -> 1;
            case "Bronce" -> 2;
            case "Plata" -> 3;
            case "Oro" -> 4;
            case "Platino" -> 5;
            case "Diamante" -> 6;
            case "Ascendente" -> 7;
            case "Inmortal" -> 8;
            case "Radiante" -> 9;
            default -> 0;
        };
    }
}