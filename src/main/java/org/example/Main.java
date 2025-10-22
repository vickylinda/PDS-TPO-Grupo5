package org.example;

import org.example.service.AuthService;
import org.example.store.JsonStore;

import java.io.Console;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        JsonStore store = new JsonStore();
        AuthService auth = new AuthService(store);

        Scanner sc = new Scanner(System.in);
        Console console = System.console();
        String token = null;

        System.out.println("Archivo de datos: " + JsonStore.dataFilePath());

        while (true) {
            System.out.println("""
                ====== MENU ======
                1) Registrar usuario
                2) Login
                3) WhoAmI (requiere token)
                4) Logout
                5) Login con Google
                6) Listar usuarios (MOD/ADMIN)
                7) Promover a MOD/ADMIN (solo ADMIN)
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
                        System.out.println("Login OK. Token:\n" + token);
                    }
                    case "3" -> {
                        ensureToken(token);
                        String email = auth.whoAmI(token);
                        var u = store.findUserByEmail(email).orElseThrow();
                        System.out.println("Usuario: " + u.getEmail());
                        System.out.println("Rol: " + u.getRoleName());
                        System.out.println("Google: " + (u.isInicioSesionConGoogle() ? "Sí" : "No"));
                    }
                    case "4" -> { token = null; System.out.println("Logout OK."); }
                    case "5" -> {
                        String clientId = System.getenv("GOOGLE_OAUTH_CLIENT_ID");
                        String clientSecret = System.getenv("GOOGLE_OAUTH_CLIENT_SECRET");
                        if (clientId == null || clientId.isBlank())
                            throw new IllegalStateException("Configura GOOGLE_OAUTH_CLIENT_ID en variables de entorno.");
                        token = auth.loginWithGoogle(clientId, clientSecret);
                        System.out.println("Login Google OK. Token local:\n" + token);
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


                    case "0" -> { System.out.println("Chau!"); return; }
                    default -> System.out.println("Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("❌ " + e.getMessage());
            }
        }
    }

    private static char[] readPassword(Console console, Scanner sc, String prompt) {
        if (console != null) return console.readPassword(prompt);
        System.out.print(prompt);
        return sc.nextLine().toCharArray();
    }

    private static void ensureToken(String token) {
        if (token == null) throw new IllegalStateException("Primero hacé login para obtener un token.");
    }
}
