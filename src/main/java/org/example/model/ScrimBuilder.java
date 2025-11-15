package org.example.model;

import java.time.LocalDateTime;

public class ScrimBuilder {
    private Scrim scrim;

    public ScrimBuilder() {
        scrim = new Scrim();
    }

    public ScrimBuilder juego(String juego) {
        scrim.setJuego(juego);
        return this;
    }

    public ScrimBuilder formato(String formato) {
        scrim.setFormato(formato);
        return this;
    }

    public ScrimBuilder rango(Rango min, Rango max) {
        scrim.setRangoMin(min);
        scrim.setRangoMax(max);
        return this;
    }

    public ScrimBuilder region(String region) {
        scrim.setRegion(region);
        return this;
    }

    public ScrimBuilder latenciaMax(int latenciaMax) {
        scrim.setLatenciaMax(latenciaMax);
        return this;
    }

    public ScrimBuilder fechaHora(LocalDateTime fechaHora) {
        scrim.setFechaHora(fechaHora);
        return this;
    }

    public ScrimBuilder duracion(int duracion) {
        scrim.setDuracion(duracion);
        return this;
    }

    public ScrimBuilder modalidad(String modalidad) {
        scrim.setModalidad(modalidad);
        return this;
    }

    public ScrimBuilder cupos(int total) {
        scrim.setCuposTotales(total);
        return this;
    }

    public Scrim build() {
        scrim.setEstadoActual(new EstadoBuscandoJugadores());
        return scrim;
    }
}
