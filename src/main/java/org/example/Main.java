package org.example;

import org.example.model.*;
import org.example.service.AuthService;
import org.example.store.JsonStore;

import java.io.Console;
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
                3) WhoAmI (requiere token)
                4) Logout
                5) Login con Google
                6) Listar usuarios (MOD/ADMIN)
                7) Promover a MOD/ADMIN (solo ADMIN)
                
                === PERFIL Y RANGOS ===
                6) Crear/Actualizar Perfil
                7) Agregar Rol Preferido
                8) Ver Mi Perfil
                9) Actualizar Puntaje (simular progresión)
                10) Ver Información de Rango
                11) Probar Sistema de Rangos (demo completo)
                
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
                        // Cargar usuario actual
                        var userOpt = store.findUserByEmail(email);
                        if (userOpt.isPresent()) {
                            currentUser = userOpt.get();
                        }
                        System.out.println("✅ Login OK. Token:\n" + token);
                    }
                    case "3" -> {
                        ensureToken(token);
                        String email = auth.whoAmI(token);
                        var u = store.findUserByEmail(email).orElseThrow();
                        System.out.println("Usuario: " + u.getEmail());
                        System.out.println("Rol: " + u.getRoleName());
                        System.out.println("Google: " + (u.isInicioSesionConGoogle() ? "Sí" : "No"));
                    }
                    case "4" -> {
                        token = null;
                        currentUser = null;
                        System.out.println("✅ Logout OK.");
                    }
                    case "5" -> {
                        String clientId = System.getenv("GOOGLE_OAUTH_CLIENT_ID");
                        String clientSecret = System.getenv("GOOGLE_OAUTH_CLIENT_SECRET");
                        if (clientId == null || clientId.isBlank())
                            throw new IllegalStateException("Configura GOOGLE_OAUTH_CLIENT_ID en variables de entorno.");
                        token = auth.loginWithGoogle(clientId, clientSecret);
                        System.out.println("✅ Login Google OK. Token local:\n" + token);
                    }

                    case "6" -> {
                        ensureLoggedIn(currentUser);

                        // Mostrar juegos disponibles
                        System.out.println("\n🎮 Juegos disponibles:");
                        for (int i = 0; i < JUEGOS_DISPONIBLES.length; i++) {
                            System.out.println((i + 1) + ". " + JUEGOS_DISPONIBLES[i].getNombre() +
                                    " - " + JUEGOS_DISPONIBLES[i].getDescripcion());
                        }
                        System.out.print("Selecciona tu juego principal (1-" + JUEGOS_DISPONIBLES.length + "): ");
                        int juegoIdx = Integer.parseInt(sc.nextLine().trim()) - 1;

                        if (juegoIdx < 0 || juegoIdx >= JUEGOS_DISPONIBLES.length) {
                            throw new IllegalArgumentException("Opción de juego inválida.");
                        }

                        Juego juegoSeleccionado = JUEGOS_DISPONIBLES[juegoIdx];

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
                        mostrarPerfil(currentUser);
                    }

                    case "7" -> {
                        ensureLoggedIn(currentUser);
                        ensurePerfil(currentUser);

                        System.out.println("\n🎮 Roles disponibles:");
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
                        System.out.println("✅ Rol agregado: " + rol.getNombre());
                        mostrarRolesPreferidos(currentUser);
                    }

                    case "8" -> {
                        ensureLoggedIn(currentUser);
                        ensurePerfil(currentUser);
                        mostrarPerfilCompleto(currentUser);
                    }
                    case "6" -> {
                        ensureToken(token);
                        var list = auth.listUsers(token);
                        System.out.println("=== Usuarios ===");
                        for (var u : list) {
                            System.out.printf("- %-30s  [%s]  google:%s%n",
                                    u.getEmail(), u.getRoleName(), u.isInicioSesionConGoogle() ? "sí" : "no");
                        }
                    }

                    case "7" -> {
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


                    case "9" -> {
                        ensureLoggedIn(currentUser);
                        ensurePerfil(currentUser);

                        System.out.print("Ingresa el nuevo puntaje: ");
                        int nuevoPuntaje = Integer.parseInt(sc.nextLine().trim());

                        String rangoAnterior = currentUser.getPerfil().getRango().getNombre();
                        currentUser.getPerfil().actualizarPuntaje(nuevoPuntaje);
                        String rangoActual = currentUser.getPerfil().getRango().getNombre();

                        System.out.println("\n📊 Actualización de puntaje:");
                        System.out.println("   Puntaje: " + nuevoPuntaje);
                        System.out.println("   Rango anterior: " + rangoAnterior);
                        System.out.println("   Rango actual: " + rangoActual);

                        if (!rangoAnterior.equals(rangoActual)) {
                            System.out.println("   🎉 ¡Has cambiado de rango!");
                        }
                    }

                    case "10" -> {
                        ensureLoggedIn(currentUser);
                        ensurePerfil(currentUser);
                        mostrarInfoRango(currentUser);
                    }

                    case "11" -> {
                        demoSistemaRangos();
                    }

                    case "0" -> {
                        System.out.println("👋 ¡Hasta luego!");
                        return;
                    }
                    default -> System.out.println("❌ Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
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
            throw new IllegalStateException("Debes crear un perfil primero (opción 6).");
    }

    private static void mostrarPerfil(User user) {
        Perfil p = user.getPerfil();
        System.out.println("\n" + "=".repeat(50));
        System.out.println("📋 PERFIL DE " + user.getEmail());
        System.out.println("=".repeat(50));
        System.out.println("Juego principal: " + p.getJuegoPrincipal().getNombre());
        System.out.println("Disponibilidad: " + p.getDisponibilidadHoraria());
        System.out.println("Puntaje: " + p.getPuntaje());
        System.out.println("Rango: " + p.getRango().getNombre());
        System.out.println("=".repeat(50) + "\n");
    }

    private static void mostrarRolesPreferidos(User user) {
        Perfil p = user.getPerfil();
        System.out.println("\n🎮 Roles preferidos:");
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
        System.out.println("📋 PERFIL COMPLETO DE " + user.getEmail());
        System.out.println("=".repeat(60));
        System.out.println("ID: " + user.getId());
        System.out.println("Email: " + user.getEmail());
        System.out.println("Activo: " + (user.isActive() ? "Sí" : "No"));
        System.out.println("Login con Google: " + (user.isInicioSesionConGoogle() ? "Sí" : "No"));
        System.out.println();
        System.out.println("--- Información del Perfil ---");
        System.out.println("Juego principal: " + p.getJuegoPrincipal().getNombre());
        System.out.println("Descripción: " + p.getJuegoPrincipal().getDescripcion());
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
        System.out.println("🏆 INFORMACIÓN DE RANGO");
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
        System.out.println("🎮 DEMO: SISTEMA DE RANGOS");
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
        System.out.println("\n📊 Simulando progresión de rangos...\n");

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
            default -> 0;
        };
    }
}