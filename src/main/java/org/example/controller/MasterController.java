package org.example.controller;
import org.example.notifications.NotificationService;
import org.example.notifications.bus.DomainEventBus;
import org.example.notifications.repo.InMemoryNotificationRepository;
import org.example.notifications.subscribers.NotificationSubscriber;
import org.example.service.AuthService;
import org.example.store.JsonStore;
import java.util.Scanner;

public class MasterController {

    // singleton
    private static volatile MasterController INSTANCE;

    public static MasterController getInstance() throws Exception {
        if (INSTANCE == null) {
            synchronized (MasterController.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MasterController();
                }
            }
        }
        return INSTANCE;
    }

    private final JsonStore store;
    private final AuthService auth;
    private final NotificationService notifService;
    private final Session session;

    private final UserController userController;
    private final ScrimController scrimController;
    private final MatchmakingController matchmakingController;

    private MasterController() throws Exception {
        this.store = new JsonStore();
        this.auth  = new AuthService(store);

        String sgKey = System.getenv("SENDGRID_API_KEY");
        if (sgKey == null || sgKey.isBlank())
            throw new IllegalStateException("Configura SENDGRID_API_KEY en variables de entorno.");

        String fromEmail = System.getenv().getOrDefault("NOTIF_FROM_EMAIL","vicky9abril@gmail.com");
        String fromName  = System.getenv().getOrDefault("NOTIF_FROM_NAME", "eScrims Platform");

        this.notifService =
                new NotificationService(new InMemoryNotificationRepository(), sgKey, fromEmail, fromName);

        DomainEventBus.getInstance().subscribe(new NotificationSubscriber(notifService));

        this.session = new Session();

        this.userController         = UserController.getInstance(store, auth, notifService, session);
        this.scrimController        = ScrimController.getInstance(store, notifService, session);
        MatchmakingController.init(session);
        this.matchmakingController  = MatchmakingController.getInstance();
    }

    public void run() {
        System.out.println("Archivo de datos: " + JsonStore.dataFilePath());
        Scanner sc = new Scanner(System.in);

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

                    === MATCHMAKING ===
                    23) Demo Matchmaking por MMR
                    24) Demo Matchmaking por Latencia
                    25) Buscar Jugadores para Scrim (interactivo)

                    0) Salir
                    """);
            System.out.print("Opción: ");
            String op = sc.nextLine().trim();

            try {
                switch (op) {
                    case "1" -> userController.registrar(sc);
                    case "2" -> userController.login(sc);
                    case "3" -> userController.loginConGoogle();
                    case "4" -> userController.whoAmI();
                    case "5" -> userController.listarUsuarios();
                    case "6" -> userController.promoverRol(sc);
                    case "7" -> userController.logout();

                    case "8"  -> userController.crearActualizarPerfil(sc);
                    case "9"  -> userController.agregarRolPreferido(sc);
                    case "10" -> userController.verMiPerfil();
                    case "11" -> userController.actualizarPuntaje(sc);
                    case "12" -> userController.verInfoRango();
                    case "13" -> userController.demoSistemaRangos();

                    case "14" -> scrimController.crearScrim(sc);
                    case "15" -> scrimController.verScrimsDisponibles();
                    case "16" -> scrimController.unirseAScrim(sc);
                    case "17" -> scrimController.confirmarParticipacion(sc);
                    case "18" -> scrimController.iniciarPartida(sc);
                    case "19" -> scrimController.finalizarPartida(sc);
                    case "20" -> scrimController.cancelarScrim(sc);
                    case "21" -> scrimController.cargarResultados(sc);
                    case "22" -> scrimController.demoCompletoScrim();

                    case "23" -> matchmakingController.demoMatchmakingPorMMR();
                    case "24" -> matchmakingController.demoMatchmakingPorLatencia();
                    case "25" -> matchmakingController.buscarJugadoresParaScrim(sc);

                    case "0" -> { System.out.println("¡Hasta luego! :)"); return; }

                    default -> System.out.println("❌ Opción inválida.");
                }
            } catch (Exception e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }
}