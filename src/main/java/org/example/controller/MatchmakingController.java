package org.example.controller;

import org.example.model.scrim.Juego;
import org.example.model.scrim.Scrim;
import org.example.model.scrim.ScrimBuilder;
import org.example.model.user.Perfil;
import org.example.model.user.RegularUser;
import org.example.model.user.User;
import org.example.model.scrim.matchmaking.ByLatencyStrategy;
import org.example.model.scrim.matchmaking.ByMMRStrategy;
import org.example.model.scrim.matchmaking.MatchmakingStrategy;
import org.example.service.MatchmakingService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class MatchmakingController {

    // singleton
    private static volatile MatchmakingController INSTANCE;

    public static MatchmakingController init(Session session) {
        if (INSTANCE == null) {
            synchronized (MatchmakingController.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MatchmakingController(session, ScrimController.getInstance());
                }
            }
        }
        return INSTANCE;
    }

    public static MatchmakingController getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("MatchmakingController no inicializado. Llamá a MatchmakingController.init(...) primero.");
        }
        return INSTANCE;
    }

    private final Session session;
    private final ScrimController scrimController;

    private MatchmakingController(Session session, ScrimController scrimController) { this.session = session; this.scrimController = scrimController; }

    public void demoMatchmakingPorMMR() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO: MATCHMAKING POR MMR/RANGO");
        System.out.println("=".repeat(80));

        List<User> candidatos = crearUsuariosPruebaMMR();

        Scrim scrim = ScrimBuilder.nuevo()
                .juego(new Juego(1,"League of Legends","MOBA 5v5 competitivo"))
                .formato(5).region("LAN").rango(1000,2000)
                .fechaHora(LocalDateTime.now().plusHours(2)).build();

        System.out.println("   Juego: " + scrim.getJuego().getNombre());
        System.out.println("   Formato: " + scrim.getFormato());
        System.out.println("   Puntaaje de Rango requerido: " + scrim.getRangoMin() + " - " + scrim.getRangoMax());
        System.out.println("   Jugadores necesarios: " + scrim.getCantidadTotalJugadores());

        for (int i = 0; i < candidatos.size(); i++) {
            User u = candidatos.get(i);
            System.out.printf("   %2d. %-25s | MMR: %4d | Región: %s%n",
                    i+1, u.getEmail(), u.getPerfil().getPuntaje(), u.getRegion());
        }

        MatchmakingStrategy estrategia = new ByMMRStrategy(500);
        MatchmakingService matchmaking = new MatchmakingService(estrategia);

        List<User> seleccionados = matchmaking.emparejarJugadores(scrim, candidatos);
        System.out.println("\n✅ Jugadores seleccionados (ordenados por compatibilidad):");
        if (seleccionados.isEmpty()) System.out.println("   ❌ No se encontraron jugadores compatibles");
        else {
            for (int i = 0; i < seleccionados.size(); i++) {
                User u = seleccionados.get(i);
                double score = matchmaking.evaluarCompatibilidad(u, scrim);
                System.out.printf("   %2d. %-25s | MMR: %4d | Compatibilidad: %.1f/100%n",
                        i+1, u.getEmail(), u.getPerfil().getPuntaje(), score);
            }
            var resultado = matchmaking.armarEquipos(seleccionados, scrim);
            System.out.println("\n🔵 EQUIPO A:");
            for (User u : resultado.getEquipoA())
                System.out.printf("   • %-25s (MMR: %d)%n", u.getEmail(), u.getPerfil().getPuntaje());
            System.out.println("\n🔴 EQUIPO B:");
            for (User u : resultado.getEquipoB())
                System.out.printf("   • %-25s (MMR: %d)%n", u.getEmail(), u.getPerfil().getPuntaje());
        }
        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    public void demoMatchmakingPorLatencia() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DEMO: MATCHMAKING POR LATENCIA/PING");
        System.out.println("=".repeat(80));

        List<User> candidatos = crearUsuariosPruebaLatencia();

        Scrim scrim = ScrimBuilder.nuevo()
                .juego(new Juego(2,"Valorant","FPS táctico 5v5"))
                .formato(5).region("NA")
                .fechaHora(LocalDateTime.now().plusHours(2)).build();

        System.out.println("   Juego: " + scrim.getJuego().getNombre());
        System.out.println("   Formato: " + scrim.getFormato());
        System.out.println("   Región del servidor: " + scrim.getRegion());
        System.out.println("   Jugadores necesarios: " + scrim.getCantidadTotalJugadores());

        ByLatencyStrategy estrategiaLatencia = new ByLatencyStrategy(100);
        for (int i = 0; i < candidatos.size(); i++) {
            User u = candidatos.get(i);
            int latencia = estrategiaLatencia.obtenerLatencia(u, scrim.getRegion());
            String indicador = latencia <= 50 ? "🟢" : latencia <= 100 ? "🟡" : "🔴";
            System.out.printf("   %2d. %-25s | Región: %-3s | Latencia: %3dms %s%n",
                    i+1, u.getEmail(), u.getRegion(), latencia, indicador);
        }

        MatchmakingService matchmaking = new MatchmakingService(estrategiaLatencia);
        List<User> seleccionados = matchmaking.emparejarJugadores(scrim, candidatos);
        System.out.println("\n✅ Jugadores seleccionados (ordenados por latencia):");
        if (seleccionados.isEmpty()) System.out.println("   ❌ No se encontraron jugadores con latencia aceptable");
        else {
            for (int i = 0; i < seleccionados.size(); i++) {
                User u = seleccionados.get(i);
                int latencia = estrategiaLatencia.obtenerLatencia(u, scrim.getRegion());
                double score = matchmaking.evaluarCompatibilidad(u, scrim);
                String calidad = latencia <= 30 ? "Excelente" : latencia <= 60 ? "Muy buena" : "Buena";
                System.out.printf("   %2d. %-25s | %3dms | %s | Score: %.1f/100%n",
                        i+1, u.getEmail(), latencia, calidad, score);
            }
        }
        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    public void buscarJugadoresParaScrim(Scanner sc) {
        // 1) tomar la lista actual de scrims desde ScrimController
        List<Scrim> scrims = scrimController.getScrims();

        if (scrims.isEmpty()) {
            System.out.println("\n❌ No hay scrims disponibles.");
            System.out.println("Crea un scrim primero (opción 14).\n");
            return;
        }

        System.out.println("\n" + "=".repeat(80));
        System.out.println("BUSCAR JUGADORES PARA SCRIM (MATCHMAKING)");
        System.out.println("=".repeat(80));

        // 2) mostrar con el método del otro controller (re-usa tu impresión actual)
        scrimController.verScrimsDisponibles();

        System.out.print("\nSelecciona el número del scrim: ");
        int idx = Integer.parseInt(sc.nextLine().trim()) - 1;
        if (idx < 0 || idx >= scrims.size()) {
            System.out.println("❌ Scrim inválido.");
            return;
        }

        Scrim scrim = scrims.get(idx);

        // 3) candidatos de prueba
        System.out.println("\n🔄 Generando candidatos de prueba...");
        List<User> candidatos = crearUsuariosMixtos();
        System.out.println("✅ " + candidatos.size() + " candidatos generados");

        // 4) elegir estrategia
        System.out.println("\n⚙  Selecciona estrategia de matchmaking:");
        System.out.println("1. Por MMR/Rango (prioriza habilidad)");
        System.out.println("2. Por Latencia/Ping (prioriza conexión)");
        System.out.print("\nOpción (1-2): ");
        String opcion = sc.nextLine().trim();

        MatchmakingService matchmaking;
        String nombreEstrategia;

        if ("1".equals(opcion)) {
            System.out.print("Diferencia máxima de MMR permitida (100-1000, recomendado 500): ");
            int difMMR = Integer.parseInt(sc.nextLine().trim());
            matchmaking = new MatchmakingService(new ByMMRStrategy(difMMR));
            nombreEstrategia = "MMR (diferencia máx: " + difMMR + ")";
        } else {
            System.out.print("Umbral máximo de latencia en ms (50-200, recomendado 100): ");
            int umbralLatencia = Integer.parseInt(sc.nextLine().trim());
            matchmaking = new MatchmakingService(new ByLatencyStrategy(umbralLatencia));
            nombreEstrategia = "Latencia (máx: " + umbralLatencia + "ms)";
        }

        // 5) emparejar
        System.out.println("\n🔍 Buscando jugadores con estrategia: " + nombreEstrategia);
        List<User> seleccionados = matchmaking.emparejarJugadores(scrim, candidatos);

        if (seleccionados.isEmpty()) {
            System.out.println("\n❌ No se encontraron jugadores compatibles.");
            System.out.println("Intenta con criterios menos restrictivos.");
            return;
        }

        // 6) mostrar top-N
        System.out.println("\n✅ Se encontraron " + seleccionados.size() + " jugadores compatibles:");
        for (int i = 0; i < Math.min(10, seleccionados.size()); i++) {
            User u = seleccionados.get(i);
            double score = matchmaking.evaluarCompatibilidad(u, scrim);
            System.out.printf("   %2d. %-25s | Score: %.1f/100%n", i + 1, u.getEmail(), score);
        }

        // 7) agregar al scrim (ojo con el límite y off-by-one)
        System.out.print("\n¿Deseas agregar los mejores jugadores al scrim? (s/n): ");
        String agregar = sc.nextLine().trim().toLowerCase();

        if ("s".equals(agregar)) {
            int agregados = 0;
            for (User u : seleccionados) {
                if (scrim.getJugadoresFaltantes() <= 0) break; // ya se completó
                try {
                    scrim.agregarJugador(u);
                    agregados++;
                } catch (Exception e) {
                    System.out.println("⚠  No se pudo agregar jugador: " + e.getMessage());
                }
            }

            System.out.println("\n✅ Se agregaron " + agregados + " jugadores al scrim");
            System.out.println("Jugadores actuales: " + scrim.getJugadoresActuales() +
                    "/" + scrim.getCantidadTotalJugadores());

            if (scrim.estaCompleto()) {
                System.out.println("\n🎉 ¡El scrim está completo y listo para comenzar!");
            }
        }

        System.out.println("\n" + "=".repeat(80) + "\n");
    }

    // ==== datos de prueba  ====
    private static List<User> crearUsuariosMixtos() {
        List<User> usuarios = new ArrayList<>();
        Object[][] datos = {
                {"proNA1@game.com", 1800, "NA"},
                {"proNA2@game.com", 1750, "NA"},
                {"casualNA@game.com", 1200, "NA"},
                {"proEU@game.com", 1900, "EU"},
                {"mediumEU@game.com", 1500, "EU"},
                {"latinoPro@game.com", 1850, "SA"},
                {"latinoMid@game.com", 1400, "SA"},
                {"asianPro@game.com", 2000, "AS"},
                {"oceanic@game.com", 1600, "OCE"},
                {"rookieNA@game.com", 900, "NA"},
                {"veteranEU@game.com", 1950, "EU"},
                {"risingStarSA@game.com", 1650, "SA"}
        };
        for (int i = 0; i < datos.length; i++) {
            Object[] d = datos[i];
            RegularUser u = new RegularUser();
            u.setId("test-mix-" + i);
            u.setEmail((String) d[0]);
            u.setRegion((String) d[2]);
            Perfil p = new Perfil(i, new Juego(1,"League of Legends","MOBA 5v5 competitivo"), "Variable");
            p.setPuntaje((Integer) d[1]);
            u.setPerfil(p);
            usuarios.add(u);
        }
        return usuarios;
    }
    private static List<User> crearUsuariosPruebaMMR() {
        List<User> usuarios = new ArrayList<>();
        int[] mmrValues = {800, 950, 1150, 1300, 1450, 1500, 1550, 1700, 1850, 2100, 2300};
        for (int i = 0; i < mmrValues.length; i++) {
            RegularUser u = new RegularUser();
            u.setId("test-mmr-" + i);
            u.setEmail(String.format("jugador%d@test.com", i + 1));
            u.setRegion("LAN");
            Perfil p = new Perfil(i, new Juego(1,"League of Legends","MOBA 5v5 competitivo"), "Disponible siempre");
            p.setPuntaje(mmrValues[i]);
            u.setPerfil(p);
            usuarios.add(u);
        }
        return usuarios;
    }

    private static List<User> crearUsuariosPruebaLatencia() {
        List<User> usuarios = new ArrayList<>();
        String[] regiones = {"NA","NA","NA","SA","SA","EU","EU","AS","OCE","OCE"};
        for (int i = 0; i < regiones.length; i++) {
            RegularUser u = new RegularUser();
            u.setId("test-lat-" + i);
            u.setEmail(String.format("player%d@region.com", i + 1));
            u.setRegion(regiones[i]);
            Perfil p = new Perfil(i, new Juego(2,"Valorant","FPS táctico 5v5"), "Tardes y noches");
            p.setPuntaje(1500);
            u.setPerfil(p);
            usuarios.add(u);
        }
        return usuarios;
    }
}