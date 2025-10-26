package org.example.store;

import org.example.model.Scrim;
import java.util.*;
import java.util.stream.Collectors;

public class ScrimStore {
    private final List<Scrim> scrims = new ArrayList<>();

    public void save(Scrim scrim) {
        scrims.add(scrim);
    }

    public List<Scrim> findAll() {
        return new ArrayList<>(scrims);
    }

    public List<Scrim> search(String juego, String formato, String region,
                              Integer latenciaMax, Date fecha, Integer rangoMin, Integer rangoMax) {

        return scrims.stream()
            .filter(s -> juego == null || s.getJuego().equalsIgnoreCase(juego))
            .filter(s -> formato == null || s.getFormato().equalsIgnoreCase(formato))
            .filter(s -> region == null || s.getRegion().equalsIgnoreCase(region))
            .filter(s -> latenciaMax == null || s.getLatenciaMax() <= latenciaMax)
            .filter(s -> fecha == null || s.getFechaHora().toLocalDate().equals(fecha.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()))
            .filter(s -> rangoMin == null || s.getRangoMin().getNivel() >= rangoMin)
            .filter(s -> rangoMax == null || s.getRangoMax().getNivel() <= rangoMax)
            .collect(Collectors.toList());
    }
}
