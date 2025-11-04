package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({"rango"}) // por si aparece en un JSON viejo, ignorar la propiedad
public class Perfil {
    private int id;
    private Juego juegoPrincipal;
    private List<Rol> rolesPreferidos;
    private List<String> regionesPreferidas;
    private List<Juego> juegosInteresados;
    private String disponibilidadHoraria;

    @JsonIgnore
    private IRangoState rango;

    private int puntaje;

    public Perfil() {
        this.rolesPreferidos = new ArrayList<>();
        this.regionesPreferidas = new ArrayList<>();
        this.juegosInteresados = new ArrayList<>();
        this.puntaje = 0;
        this.rango = RangoFactory.fromPuntaje(this); // valor inicial coherente
    }

    public Perfil(int id, Juego juegoPrincipal, String disponibilidadHoraria) {
        this();
        this.id = id;
        this.juegoPrincipal = juegoPrincipal;
        this.disponibilidadHoraria = disponibilidadHoraria;
    }

    /** Suma al puntaje actual (delta puede ser +/-) y ajusta rango. */
    public void actualizarPuntaje(int delta) {
        this.puntaje += delta;
        if (this.puntaje < 0) this.puntaje = 0;
        recomputarRango();
    }

    /** Reasigna el estado de rango según el puntaje actual. */
    private void recomputarRango() {
        this.rango = RangoFactory.fromPuntaje(this);
    }

    // --- getters/setters básicos ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Juego getJuegoPrincipal() { return juegoPrincipal; }
    public void setJuegoPrincipal(Juego juegoPrincipal) { this.juegoPrincipal = juegoPrincipal; }

    public List<Rol> getRolesPreferidos() { return rolesPreferidos; }
    public void setRolesPreferidos(List<Rol> rolesPreferidos) { this.rolesPreferidos = rolesPreferidos; }

    public List<String> getRegionesPreferidas() {
        return regionesPreferidas;
    }

    public void setRegionesPreferidas(List<String> regionesPreferidas) {
        this.regionesPreferidas = regionesPreferidas;
    }

    public List<Juego> getJuegosInteresados() {
        return juegosInteresados;
    }

    public void setJuegosInteresados(List<Juego> juegosInteresados) {
        this.juegosInteresados = juegosInteresados;
    }

    public String getDisponibilidadHoraria() { return disponibilidadHoraria; }
    public void setDisponibilidadHoraria(String disponibilidadHoraria) { this.disponibilidadHoraria = disponibilidadHoraria; }

    /** Getter “lazy”: si rango es null (p. ej. recién cargado del JSON), se reconstruye. */
    public IRangoState getRango() {
        if (rango == null) {
            rango = RangoFactory.fromPuntaje(this);
        }
        return rango;
    }

    @JsonIgnore // evitá que Jackson intente setear la interfaz
    public void setRango(IRangoState rango) { this.rango = rango; }

    public int getPuntaje() { return puntaje; }

    /** Si Jackson setea el puntaje desde el JSON, recalculá el rango. */
    public void setPuntaje(int puntaje) {
        this.puntaje = Math.max(0, puntaje);
        recomputarRango();
    }

    public void agregarRolPreferido(Rol rol) {
        if (!rolesPreferidos.contains(rol)) rolesPreferidos.add(rol);
    }
    public void eliminarRolPreferido(Rol rol) { rolesPreferidos.remove(rol); }

    public void agregarRegionPreferido(String region) {
        if (!regionesPreferidas.contains(region)) regionesPreferidas.add(region);
    }
    public void eliminarRegionPreferido(String region) { regionesPreferidas.remove(region); }

    public void agregarJuegoPreferido(Juego juego) {
        if (!juegosInteresados.contains(juego)) juegosInteresados.add(juego);
    }
    public void eliminarJuegoPreferido(Juego juego) { juegosInteresados.remove(juego); }

    @Override public String toString() {
        return "Perfil{" +
                "id=" + id +
                ", juegoPrincipal=" + (juegoPrincipal != null ? juegoPrincipal.getNombre() : "-") +
                ", rolesPreferidos=" + rolesPreferidos.size() +
                ", disponibilidadHoraria='" + disponibilidadHoraria + '\'' +
                ", rango=" + getRango().getNombre() +
                ", puntaje=" + puntaje +
                '}';
    }
}
