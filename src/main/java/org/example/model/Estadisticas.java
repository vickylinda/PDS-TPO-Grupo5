package org.example.model;

public class Estadisticas {
    private int kills;
    private int deaths;
    private int assists;
    private int puntaje;

    public Estadisticas() {
        this.kills = 0;
        this.deaths = 0;
        this.assists = 0;
        this.puntaje = 0;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void setAssists(int assists) {
        this.assists = assists;
    }

    public void setPuntaje(int puntaje) {
        this.puntaje = puntaje;
    }

    public int getKills() {
        return kills;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getAssists() {
        return assists;
    }

    public int getPuntaje() {
        return puntaje;
    }
}
