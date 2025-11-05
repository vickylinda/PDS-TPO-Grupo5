package org.example.model.scrim;

import org.example.model.scrim.state.BuscandoJugadoresState;

import java.time.LocalDateTime;
import java.util.*;

public class ScrimBuilder {

    // Campos obligatorios
    private Juego juego;
    private int jugadoresPorLado;
    private String region;
    private LocalDateTime fechaHora;

    // Campos con valores por defecto
    private Integer rangoMin;
    private Integer rangoMax;
    private Integer latenciaMaxima = 100; // ms por defecto
    private Integer duracionEstimada = 60; // minutos por defecto


    // Campos opcionales
    private Integer cantidadTotalJugadores;

    // Constructor privado para forzar uso de método estático
    private ScrimBuilder() {}

    public static ScrimBuilder nuevo() {
        return new ScrimBuilder();
    }

    // ===== MÉTODOS OBLIGATORIOS =====

    public ScrimBuilder juego(Juego juego) {
        if (juego == null || juego.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El juego no puede ser nulo o vacío");
        }
        this.juego = juego;
        return this;
    }

    public ScrimBuilder formato(int jugadoresPorLado) {
        if (jugadoresPorLado < 1 || jugadoresPorLado > 50) {
            throw new IllegalArgumentException("Jugadores por lado debe estar entre 1 y 50");
        }
        this.jugadoresPorLado = jugadoresPorLado;
        return this;
    }

    public ScrimBuilder region(String region) {
        if (region == null || region.trim().isEmpty()) {
            throw new IllegalArgumentException("La región no puede ser nula o vacía");
        }
        this.region = region.trim();
        return this;
    }

    public ScrimBuilder fechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            throw new IllegalArgumentException("La fecha/hora no puede ser nula");
        }
        if (fechaHora.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La fecha/hora no puede estar en el pasado");
        }
        this.fechaHora = fechaHora;
        return this;
    }

    // ===== MÉTODOS CON VALIDACIÓN =====

    public ScrimBuilder rango(int min, int max) {
        if (min < 0) {
            throw new IllegalArgumentException("El rango mínimo no puede ser negativo");
        }
        if (max < min) {
            throw new IllegalArgumentException("El rango máximo debe ser mayor o igual al mínimo");
        }
        this.rangoMin = min;
        this.rangoMax = max;
        return this;
    }

    public ScrimBuilder latenciaMaxima(int latenciaMaxima) {
        if (latenciaMaxima < 0 || latenciaMaxima > 500) {
            throw new IllegalArgumentException("Latencia máxima debe estar entre 0 y 500 ms");
        }
        this.latenciaMaxima = latenciaMaxima;
        return this;
    }

    public ScrimBuilder duracionEstimada(int minutos) {
        if (minutos < 5 || minutos > 480) {
            throw new IllegalArgumentException("Duración estimada debe estar entre 5 y 480 minutos");
        }
        this.duracionEstimada = minutos;
        return this;
    }


    // ===== MÉTODOS OPCIONALES =====

    public ScrimBuilder cantidadTotalJugadores(int cantidad) {
        if (cantidad < 2) {
            throw new IllegalArgumentException("Debe haber al menos 2 jugadores");
        }
        this.cantidadTotalJugadores = cantidad;
        return this;
    }


    // ===== BUILD CON VALIDACIONES =====

    public Scrim build() {
        // Validar campos obligatorios
        validarCamposObligatorios();

        // Validar invariantes de negocio
        validarInvariantes();

        // Crear nueva instancia de Scrim (inmutabilidad del builder)
        Scrim scrim = new Scrim();

        // Establecer campos obligatorios
        scrim.setJuego(this.juego);
        scrim.setJugadoresPorLado(this.jugadoresPorLado);
        scrim.setRegion(this.region);
        scrim.setFechaHora(this.fechaHora);

        // Establecer campos con valores por defecto
        scrim.setRangoMin(this.rangoMin);
        scrim.setRangoMax(this.rangoMax);
        scrim.setLatenciaMaxima(this.latenciaMaxima);
        scrim.setDuracionEstimada(this.duracionEstimada);

        // Establecer campos opcionales
        scrim.setCantidadTotalJugadores(
                this.cantidadTotalJugadores != null
                        ? this.cantidadTotalJugadores
                        : this.jugadoresPorLado * 2
        );


        // Establecer estado inicial
        scrim.setEstado(new BuscandoJugadoresState(scrim));
        scrim.setFechaCreacion(LocalDateTime.now());

        return scrim;
    }

    private void validarCamposObligatorios() {
        List<String> camposFaltantes = new ArrayList<>();

        if (juego == null) camposFaltantes.add("juego");
        if (jugadoresPorLado == 0) camposFaltantes.add("formato/jugadoresPorLado");
        if (region == null) camposFaltantes.add("región");
        if (fechaHora == null) camposFaltantes.add("fechaHora");

        if (!camposFaltantes.isEmpty()) {
            throw new IllegalStateException(
                    "Faltan campos obligatorios: " + String.join(", ", camposFaltantes)
            );
        }
    }

    private void validarInvariantes() {

        // Validar que cantidadTotalJugadores sea coherente
        if (cantidadTotalJugadores != null) {
            if (cantidadTotalJugadores < jugadoresPorLado * 2) {
                throw new IllegalStateException(
                        "Cantidad total de jugadores (" + cantidadTotalJugadores +
                                ") no puede ser menor que jugadores por lado * 2 (" +
                                (jugadoresPorLado * 2) + ")"
                );
            }
        }
    }
}