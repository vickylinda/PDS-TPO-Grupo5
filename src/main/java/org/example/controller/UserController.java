package org.example.controller;

import org.example.model.scrim.Juego;
import org.example.model.user.Perfil;
import org.example.model.user.Rol;
import org.example.model.user.User;
import org.example.notifications.NotificationService;
import org.example.service.AuthService;
import org.example.store.JsonStore;

import java.io.Console;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public final class UserController {

    // ===== Singleton =====
    private static volatile UserController INSTANCE;

    public static UserController getInstance(JsonStore store,
                                             AuthService auth,
                                             NotificationService notifService,
                                             Session session) {
        UserController result = INSTANCE;
        if (result == null) {
            synchronized (UserController.class) {
                result = INSTANCE;
                if (result == null) {
                    INSTANCE = result = new UserController(store, auth, notifService, session);
                }
            }
        }
        return result;
    }


    // ===== Estado =====
    private final JsonStore store;
    private final AuthService auth;
    private final NotificationService notifService;
    private final Session session;

    // ctor privado (Singleton)
    private UserController(JsonStore store, AuthService auth, NotificationService notifService, Session session) {
        this.store = store;
        this.auth = auth;
        this.notifService = notifService;
        this.session = session;
    }

    // ===== Helpers de validación =====
    private void ensureToken() {
        if (session.getToken() == null) throw new IllegalStateException("Primero hacé login para obtener un token.");
    }
    private void ensureLoggedIn() {
        if (session.getCurrentUser() == null) throw new IllegalStateException("Debes iniciar sesión primero.");
    }
    private void ensurePerfil() {
        if (session.getCurrentUser().getPerfil() == null)
            throw new IllegalStateException("Debes crear un perfil primero (opción 8).");
    }
    private static char[] readPassword(Console console, Scanner sc, String prompt) {
        if (console != null) return console.readPassword(prompt);
        System.out.print(prompt);
        return sc.nextLine().toCharArray();
    }

    // ===== Autenticación =====
    public void registrar(Scanner sc) throws Exception {
        Console console = System.console();
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

    public void login(Scanner sc) throws Exception {
        Console console = System.console();
        System.out.print("Email: ");
        String email = sc.nextLine().trim();
        char[] pass = readPassword(console, sc, "Password: ");
        String token = auth.login(email, pass);
        session.setToken(token);
        store.findUserByEmail(email).ifPresent(session::setCurrentUser);
        System.out.println("✅ Login OK. Token:\n" + token);
    }

    public void loginConGoogle() throws Exception {
        String clientId = System.getenv("GOOGLE_OAUTH_CLIENT_ID");
        String clientSecret = System.getenv("GOOGLE_OAUTH_CLIENT_SECRET");
        if (clientId == null || clientId.isBlank())
            throw new IllegalStateException("Configura GOOGLE_OAUTH_CLIENT_ID en variables de entorno.");
        String token = auth.loginWithGoogle(clientId, clientSecret);
        session.setToken(token);
        String email = auth.whoAmI(token);
        session.setCurrentUser(store.findUserByEmail(email).orElseThrow());
        System.out.println("✅ Login Google OK. Token local:\n" + token);
    }

    public void whoAmI() {
        ensureToken();
        String email = auth.whoAmI(session.getToken());
        var u = store.findUserByEmail(email).orElseThrow();
        System.out.println("Usuario: " + u.getEmail());
        System.out.println("Rol: " + u.getRoleName());
        System.out.println("Google: " + (u.isInicioSesionConGoogle() ? "Sí" : "No"));
    }

    public void listarUsuarios() {
        ensureToken();
        List<User> list = auth.listUsers(session.getToken());
        System.out.println("=== Usuarios ===");
        for (var u : list) {
            System.out.printf("- %-30s  [%s]  google:%s%n",
                    u.getEmail(), u.getRoleName(), u.isInicioSesionConGoogle() ? "sí" : "no");
        }
    }

    public void promoverRol(Scanner sc) throws IOException {
        ensureToken();
        System.out.print("Email destino: ");
        String emailDst = sc.nextLine().trim();
        System.out.print("Nuevo rol (MOD/ADMIN): ");
        String rol = sc.nextLine().trim().toUpperCase();
        if ("MOD".equals(rol)) {
            auth.promoteToModerator(session.getToken(), emailDst);
        } else if ("ADMIN".equals(rol)) {
            auth.promoteToAdmin(session.getToken(), emailDst);
        } else {
            throw new IllegalArgumentException("Rol inválido.");
        }
        System.out.println("✔ Rol actualizado.");
    }

    public void logout() {
        session.clear();
        System.out.println("✅ Logout OK.");
    }

    // ===== Perfil y rangos =====
    public void crearActualizarPerfil(Scanner sc) throws Exception {
        ensureLoggedIn();

        var juegos = ScrimController.JUEGOS_DISPONIBLES;
        System.out.println("\nJuegos disponibles:");
        for (int i = 0; i < juegos.length; i++) {
            System.out.println((i + 1) + ". " + juegos[i].getNombre() + " - " + juegos[i].getDescripcion());
        }
        System.out.print("Selecciona tu juego principal (1-" + juegos.length + "): ");
        int juegoIdx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (juegoIdx < 0 || juegoIdx >= juegos.length)
            throw new IllegalArgumentException("Opción de juego inválida.");
        Juego juegoSeleccionado = juegos[juegoIdx];

        System.out.print("Disponibilidad horaria (ej: Noches 8pm-12am): ");
        String disponibilidad = sc.nextLine().trim();

        User current = session.getCurrentUser();
        Perfil perfil;
        if (current.getPerfil() == null) {
            perfil = new Perfil(Math.abs(current.getId().hashCode()), juegoSeleccionado, disponibilidad);
            current.setPerfil(perfil);
            System.out.println("✅ Perfil creado exitosamente.");
        } else {
            perfil = current.getPerfil();
            perfil.setJuegoPrincipal(juegoSeleccionado);
            perfil.setDisponibilidadHoraria(disponibilidad);
            System.out.println("✅ Perfil actualizado.");
        }

        System.out.println("¿Agregar un juego de interés? (s/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("s")) {
            for (int i = 0; i < juegos.length; i++) System.out.println((i + 1) + ". " + juegos[i].getNombre());
            System.out.print("Selecciona uno: ");
            int idx = Integer.parseInt(sc.nextLine()) - 1;
            perfil.getJuegosInteresados().add(juegos[idx]);
        }

        System.out.print("Región (ej: LAN, NA, EUW): ");
        String regionElegida = sc.nextLine().trim().toUpperCase();
        current.setRegion(regionElegida);

        System.out.println("¿Agregar una región preferida? (s/n): ");
        if (sc.nextLine().trim().equalsIgnoreCase("s")) {
            System.out.print("Región: ");
            perfil.getRegionesPreferidas().add(sc.nextLine().trim().toUpperCase());
        }

        store.updateUser(current);
        mostrarPerfil(current);
    }

    public void agregarRolPreferido(Scanner sc) throws Exception {
        ensureLoggedIn(); ensurePerfil();

        System.out.println("\nRoles disponibles:");
        System.out.println("1. Support - Rol de apoyo al equipo");
        System.out.println("2. ADC - Tirador/Carry");
        System.out.println("3. Mid - Línea central");
        System.out.println("4. Top - Línea superior");
        System.out.println("5. Jungle - Jungla");
        System.out.print("Selecciona un rol (1-5): ");
        String rolOp = sc.nextLine().trim();

        Rol rol = switch (rolOp) {
            case "1" -> new Rol(1,"Support","Rol de apoyo al equipo");
            case "2" -> new Rol(2,"ADC","Tirador/Carry");
            case "3" -> new Rol(3,"Mid","Línea central");
            case "4" -> new Rol(4,"Top","Línea superior");
            case "5" -> new Rol(5,"Jungle","Jungla");
            default -> throw new IllegalArgumentException("Opción inválida");
        };

        session.getCurrentUser().getPerfil().agregarRolPreferido(rol);
        store.updateUser(session.getCurrentUser());

        System.out.println("✅ Rol agregado: " + rol.getNombre());
        mostrarRolesPreferidos(session.getCurrentUser());
    }

    public void verMiPerfil() {
        ensureLoggedIn(); ensurePerfil();
        mostrarPerfilCompleto(session.getCurrentUser());
    }

    public void actualizarPuntaje(Scanner sc) throws Exception {
        ensureLoggedIn(); ensurePerfil();

        System.out.print("Ingresa el nuevo puntaje: ");
        int delta = Integer.parseInt(sc.nextLine().trim());

        var u = session.getCurrentUser();
        String rangoAnterior = u.getPerfil().getRango().getNombre();
        u.getPerfil().actualizarPuntaje(delta);
        String rangoActual = u.getPerfil().getRango().getNombre();

        store.updateUser(u);

        System.out.println("\nActualización de puntaje:");
        System.out.println("  Puntaje total: " + u.getPerfil().getPuntaje());
        System.out.println("  Rango anterior: " + rangoAnterior);
        System.out.println("  Rango actual:   " + rangoActual);
        if (!rangoAnterior.equals(rangoActual)) System.out.println("¡Has cambiado de rango!");
    }

    public void verInfoRango() {
        ensureLoggedIn(); ensurePerfil();
        mostrarInfoRango(session.getCurrentUser());
    }

    public void demoSistemaRangos() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println(" DEMO: SISTEMA DE RANGOS");
        System.out.println("=".repeat(60));

        var juegoDemo = new Juego(1,"League of Legends","MOBA 5v5 competitivo");
        var perfil = new Perfil(999, juegoDemo, "Todo el día");

        perfil.agregarRolPreferido(new Rol(1,"Support","Rol de apoyo"));
        perfil.agregarRolPreferido(new Rol(3,"Mid","Línea central"));

        System.out.println("Perfil de demostración creado:");
        System.out.println("  • Juego: " + perfil.getJuegoPrincipal().getNombre());
        System.out.println("  • Roles: " + perfil.getRolesPreferidos().size() + " roles preferidos");
        System.out.println("\n Simulando progresión de rangos...\n");

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
                    System.out.println("           ⬆  ¡SUBISTE DE RANGO!");
                } else {
                    System.out.println("           ⬇  Bajaste de rango");
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

    // ===== impresiones =====
    private static int getNivelPorNombre(String nombre) {
        return switch (nombre) {
            case "Hierro" -> 1; case "Bronce" -> 2; case "Plata" -> 3; case "Oro" -> 4;
            case "Platino" -> 5; case "Diamante" -> 6; case "Ascendente" -> 7;
            case "Inmortal" -> 8; case "Radiante" -> 9; default -> 0;
        };
    }
    private static void mostrarPerfil(User user) {
        var p = user.getPerfil();
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
        var p = user.getPerfil();
        System.out.println("\n Roles preferidos:");
        if (p.getRolesPreferidos().isEmpty()) System.out.println("   (ninguno)");
        else for (Rol rol : p.getRolesPreferidos())
            System.out.println("   • " + rol.getNombre() + " - " + rol.getDescripcion());
        System.out.println();
    }
    private static void mostrarPerfilCompleto(User user) {
        var p = user.getPerfil();
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
        if (p.getRolesPreferidos().isEmpty()) System.out.println("(ninguno)");
        else for (Rol rol : p.getRolesPreferidos()) System.out.println("• " + rol.getNombre() + " - " + rol.getDescripcion());
        System.out.println("=".repeat(60) + "\n");
    }
    private static void mostrarInfoRango(User user) {
        var p = user.getPerfil();
        var rango = p.getRango();
        System.out.println("\n" + "=".repeat(50));
        System.out.println(" INFORMACIÓN DE RANGO");
        System.out.println("=".repeat(50));
        System.out.println("Rango actual: " + rango.getNombre());
        System.out.println("Nivel: " + rango.getValorNivel());
        System.out.println("Puntaje actual: " + p.getPuntaje());
        System.out.println("Rango de puntaje: " + rango.getPuntajeMin() + " - " + rango.getPuntajeMax());
        int puntosParaSiguiente = rango.getPuntajeMax() + 1 - p.getPuntaje();
        if (puntosParaSiguiente > 0) System.out.println("Puntos para siguiente rango: " + puntosParaSiguiente);
        else System.out.println("¡Has alcanzado el máximo de este rango!");
        System.out.println("=".repeat(50) + "\n");
    }
}