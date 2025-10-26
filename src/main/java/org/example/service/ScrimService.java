package org.example.service;

import org.example.model.*;
import org.example.store.ScrimStore;
import java.time.LocalDateTime;

public class ScrimService {
    private final ScrimStore store;

    public ScrimService(ScrimStore store) {
        this.store = store;
    }

    // --- CREACIÓN DE SCRIM ---
    public Scrim crearScrim(String juego, String formato, Rango rangoMin, Rango rangoMax,
                            String region, int latenciaMax, LocalDateTime fechaHora,
                            int duracion, String modalidad, int cuposTotales) {

        Scrim scrim = new ScrimBuilder()
                .juego(juego)
                .formato(formato)
                .rango(rangoMin, rangoMax)
                .region(region)
                .latenciaMax(latenciaMax)
                .fechaHora(fechaHora)
                .duracion(duracion)
                .modalidad(modalidad)
                .cupos(cuposTotales)
                .build();

        store.save(scrim);
        return scrim;
    }
}
