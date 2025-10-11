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
                        var userOpt = new org.example.store.JsonStore().findUserByEmail(email);
                        boolean conGoogle = userOpt.isPresent() && userOpt.get().isInicioSesionConGoogle();
                        System.out.println("Usuario: " + email);
                        System.out.println("Inicio de sesión con Google: " + (conGoogle ? "Sí" : "No"));
                    }
                    case "4" -> { token = null; System.out.println("Logout OK."); }
                    case "5" -> {
                        String clientId = System.getenv("GOOGLE_OAUTH_CLIENT_ID");
                        String clientSecret = System.getenv("GOOGLE_OAUTH_CLIENT_SECRET"); // puede ser null
                        if (clientId == null || clientId.isBlank())
                            throw new IllegalStateException("Configura GOOGLE_OAUTH_CLIENT_ID en variables de entorno.");
                        token = auth.loginWithGoogle(clientId, clientSecret);
                        System.out.println("Login Google OK. Token local:\n" + token);
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
