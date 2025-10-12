package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class Perfil {
    private int id;
    private String juegoPrincipal;
    private List<Rol> rolesPreferidos;
    private String disponibilidadHoraria;
    private IRangoState rango;
    private int puntaje; // Para gestionar el puntaje actual del jugador

    public Perfil() {
        this.rolesPreferidos = new ArrayList<>();
        this.rango = new RangoHierro(this); // Por defecto empieza en Hierro
        this.puntaje = 0;
    }

    public Perfil(int id, String juegoPrincipal, String disponibilidadHoraria) {
        this.id = id;
        this.juegoPrincipal = juegoPrincipal;
        this.disponibilidadHoraria = disponibilidadHoraria;
        this.rolesPreferidos = new ArrayList<>();
        this.rango = new RangoHierro(this);
        this.puntaje = 0;
    }

    // Método para actualizar el puntaje y cambiar de rango si es necesario
    public void actualizarPuntaje(int nuevoPuntaje) {
        this.puntaje = nuevoPuntaje;

        // Verificar si debe subir de rango
        if (nuevoPuntaje > rango.getPuntajeMax()) {
            rango.upgrade();
        }
        // Verificar si debe bajar de rango
        else if (nuevoPuntaje < rango.getPuntajeMin() && rango.getValorNivel() > 1) {
            rango.downgrade();
        }
    }

    public void agregarRolPreferido(Rol rol) {
        if (!rolesPreferidos.contains(rol)) {
            rolesPreferidos.add(rol);
        }
    }

    public void eliminarRolPreferido(Rol rol) {
        rolesPreferidos.remove(rol);
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJuegoPrincipal() {
        return juegoPrincipal;
    }

    public void setJuegoPrincipal(String juegoPrincipal) {
        this.juegoPrincipal = juegoPrincipal;
    }

    public List<Rol> getRolesPreferidos() {
        return rolesPreferidos;
    }

    public void setRolesPreferidos(List<Rol> rolesPreferidos) {
        this.rolesPreferidos = rolesPreferidos;
    }

    public String getDisponibilidadHoraria() {
        return disponibilidadHoraria;
    }

    public void setDisponibilidadHoraria(String disponibilidadHoraria) {
        this.disponibilidadHoraria = disponibilidadHoraria;
    }

    public IRangoState getRango() {
        return rango;
    }

    public void setRango(IRangoState rango) {
        this.rango = rango;
    }

    public int getPuntaje() {
        return puntaje;
    }

    public void setPuntaje(int puntaje) {
        this.puntaje = puntaje;
    }

    @Override
    public String toString() {
        return "Perfil{" +
                "id=" + id +
                ", juegoPrincipal='" + juegoPrincipal + '\'' +
                ", rolesPreferidos=" + rolesPreferidos.size() +
                ", disponibilidadHoraria='" + disponibilidadHoraria + '\'' +
                ", rango=" + rango.getNombre() +
                ", puntaje=" + puntaje +
                '}';
    }
}