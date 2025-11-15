package org.example;

import org.example.model.Rango;
import org.example.model.Scrim;
import org.example.service.ScrimService;
import org.example.store.ScrimStore;

import java.time.LocalDateTime;

public class MainScrimDemo {
    public static void main(String[] args) {
        System.out.println("=== DEMO: CREACIÓN DE SCRIMS ===");

        ScrimStore store = new ScrimStore();
        ScrimService scrimService = new ScrimService(store);

        Rango rangoMin = new Rango("Hierro", 1);
        Rango rangoMax = new Rango("Oro", 3);

        Scrim scrim = scrimService.crearScrim(
                "League of Legends",
                "5v5",
                rangoMin,
                rangoMax,
                "LATAM SUR",
                80,
                LocalDateTime.now().plusDays(1),
                90,
                "Ranked-like",
                10
        );

        System.out.println("Scrim creada");
        System.out.println("Juego: " + scrim.getJuego());
        System.out.println("Formato: " + scrim.getFormato());
        System.out.println("Región: " + scrim.getRegion());
        System.out.println("Estado: " + scrim.getEstadoActual());
    }
}
