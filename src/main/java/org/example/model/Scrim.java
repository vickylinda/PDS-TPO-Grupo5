package org.example.model;

import java.time.LocalDateTime;

public class Scrim {
    private static int nextId = 1;

    private int id;
    private String juego;
    private String formato; // "5v5", "3v3", "1v1"
    private Rango rangoMin;
    private Rango rangoMax;
    private String region;
    private int latenciaMax;
    private LocalDateTime fechaHora;
    private int duracion; // en minutos
    private String modalidad; // "ranked-like", "casual", etc.
    private int cuposTotales;
    private int cuposOcupados;
    private IScrimState estadoActual;

    public Scrim() {
        this.id = nextId++;
    }

    // Getters y setters
    public int getId() { return id; }
    public String getJuego() { return juego; }
    public void setJuego(String juego) { this.juego = juego; }
    public String getFormato() { return formato; }
    public void setFormato(String formato) { this.formato = formato; }
    public Rango getRangoMin() { return rangoMin; }
    public void setRangoMin(Rango rangoMin) { this.rangoMin = rangoMin; }
    public Rango getRangoMax() { return rangoMax; }
    public void setRangoMax(Rango rangoMax) { this.rangoMax = rangoMax; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public int getLatenciaMax() { return latenciaMax; }
    public void setLatenciaMax(int latenciaMax) { this.latenciaMax = latenciaMax; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }
    public String getModalidad() { return modalidad; }
    public void setModalidad(String modalidad) { this.modalidad = modalidad; }
    public int getCuposTotales() { return cuposTotales; }
    public void setCuposTotales(int cuposTotales) { this.cuposTotales = cuposTotales; }
    public int getCuposOcupados() { return cuposOcupados; }
    public void setCuposOcupados(int cuposOcupados) { this.cuposOcupados = cuposOcupados; }
    public IScrimState getEstadoActual() { return estadoActual; }
    public void setEstadoActual(IScrimState estadoActual) { this.estadoActual = estadoActual; }
}
