package org.example.model;

import java.util.UUID;

public class RangoHierro implements IRangoState {
    private String id;
    private String nombre;
    private int puntajeMin;
    private int puntajeMax;
    private int valorNivel;
    private Perfil perfil; // Referencia al perfil para poder cambiar su estado

    public RangoHierro(Perfil perfil) {
        this.id = UUID.randomUUID().toString();
        this.nombre = "Hierro";
        this.puntajeMin = 0;
        this.puntajeMax = 999;
        this.valorNivel = 1;
        this.perfil = perfil;
    }

    @Override
    public void upgrade() {
        // Cambiar al siguiente rango (Bronce, por ejemplo)
        // perfil.setRango(new Bronce(perfil));
        System.out.println("¡Felicidades! Has subido a Bronce");
    }

    @Override
    public void downgrade() {
        // Hierro es el rango más bajo, no se puede bajar más
        System.out.println("Ya estás en el rango más bajo");
    }

    @Override
    public String getNombre() {
        return nombre;
    }

    @Override
    public int getPuntajeMin() {
        return puntajeMin;
    }

    @Override
    public int getPuntajeMax() {
        return puntajeMax;
    }

    @Override
    public int getValorNivel() {
        return valorNivel;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Hierro{" +
                "nombre='" + nombre + '\'' +
                ", valorNivel=" + valorNivel +
                ", puntaje=" + puntajeMin + "-" + puntajeMax +
                '}';
    }
}