package org.example.model.scrim;

import org.example.model.scrim.state.BuscandoJugadoresState;
import org.example.model.scrim.state.IScrimState;
import org.example.model.Juego;
import org.example.model.Resultados;
import org.example.model.user.User;

import java.time.LocalDateTime;
import java.util.*;

public class Scrim {

    // ===== IDENTIFICACIÓN =====
    private String id;
    private Juego juego;

    // ===== FORMATO Y CONFIGURACIÓN =====
    private int jugadoresPorLado;
    private String formato; // Ej: "5v5", "3v3"
    private Integer cantidadTotalJugadores;


    // ===== REGIÓN Y REQUISITOS =====
    private String region;

    private Integer rangoMin;
    private Integer rangoMax;
    private Integer latenciaMaxima;

    // ===== TEMPORALIDAD =====
    private LocalDateTime fechaHora;
    private Integer duracionEstimada; // minutos
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFinalizacion;

    // ===== MODALIDAD Y ESTADO =====

    private IScrimState estado;

    // ===== PARTICIPANTES =====
    private String creadorId;
    private List<User> equipoA;
    private List<User> equipoB;
    private List<String> jugadoresInscritos;
    private int jugadoresActuales;

    // ===== RESULTADOS Y METADATA =====
    private String ganadorId;
    private Resultados resultados;
    private String observaciones;
    private boolean esPrivado;
    private String codigoAcceso;

    // ===== CONSTRUCTOR =====
    public Scrim() {
        this.id = UUID.randomUUID().toString();
        this.jugadoresInscritos = new ArrayList<>();
        //this.resultados = new HashMap<>();
        this.jugadoresActuales = 0;
        this.esPrivado = false;
        this.fechaCreacion = LocalDateTime.now();
        this.equipoA = new ArrayList<>();
        this.equipoB = new ArrayList<>();
        this.estado = new BuscandoJugadoresState(this);
    }

    // ===== MÉTODOS DE NEGOCIO =====

    public void agregarJugador(User usuario) {
        estado.agregarJugador(usuario);
    }

    public void confirmarJugador(User usuario) {
        estado.confirmar(usuario);
    }

    public void iniciarPartida() {
        estado.iniciar();
    }

    public void finalizarPartida() {
        estado.finalizar();
    }

    public void cancelar() {
        estado.cancelar();
    }

    public void cargarResultados(Resultados resultados) {
        estado.cargarResultados(resultados);
    }

    public void cambiarEstado(IScrimState nuevoEstado) {
        this.estado = nuevoEstado;
        System.out.println("Estado cambiado a: " + nuevoEstado.getNombreEstado());
    }

    public boolean estaCompleto() {
        return equipoA.size() == jugadoresPorLado &&
                equipoB.size() == jugadoresPorLado;
    }


    /**
     * Verifica si un jugador cumple con los requisitos de rango
     */
    public boolean cumpleRequisitosRango(int rangoJugador) {
        if (rangoMin == null && rangoMax == null) {
            return true; // Sin restricciones de rango
        }

        if (rangoMin != null && rangoJugador < rangoMin) {
            return false;
        }

        if (rangoMax != null && rangoJugador > rangoMax) {
            return false;
        }

        return true;
    }




    /**
     * Obtiene jugadores faltantes
     */
    public int getJugadoresFaltantes() {
        return Math.max(0, cantidadTotalJugadores - jugadoresActuales);
    }

    /**
     * Genera código de acceso para scrim privado
     */
    public String generarCodigoAcceso() {
        this.esPrivado = true;
        this.codigoAcceso = generarCodigoAleatorio(6);
        return this.codigoAcceso;
    }

    private String generarCodigoAleatorio(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < longitud; i++) {
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }

        return codigo.toString();
    }

    /**
     * Verifica si el código de acceso es válido
     */
    public boolean verificarCodigoAcceso(String codigo) {
        if (!esPrivado) {
            return true; // No requiere código
        }
        return this.codigoAcceso != null && this.codigoAcceso.equals(codigo);
    }

    // ===== GETTERS Y SETTERS =====

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Juego getJuego() {
        return juego;
    }

    public void setJuego(Juego juego) {
        this.juego = juego;
    }

    public int getJugadoresPorLado() {
        return jugadoresPorLado;
    }

    public void setJugadoresPorLado(int jugadoresPorLado) {
        this.jugadoresPorLado = jugadoresPorLado;
        this.formato = jugadoresPorLado + "v" + jugadoresPorLado;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public Integer getCantidadTotalJugadores() {
        return cantidadTotalJugadores;
    }

    public void setCantidadTotalJugadores(Integer cantidadTotalJugadores) {
        this.cantidadTotalJugadores = cantidadTotalJugadores;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }


    public Integer getRangoMin() {
        return rangoMin;
    }

    public void setRangoMin(Integer rangoMin) {
        this.rangoMin = rangoMin;
    }

    public Integer getRangoMax() {
        return rangoMax;
    }

    public void setRangoMax(Integer rangoMax) {
        this.rangoMax = rangoMax;
    }

    public Integer getLatenciaMaxima() {
        return latenciaMaxima;
    }

    public void setLatenciaMaxima(Integer latenciaMaxima) {
        this.latenciaMaxima = latenciaMaxima;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Integer getDuracionEstimada() {
        return duracionEstimada;
    }

    public void setDuracionEstimada(Integer duracionEstimada) {
        this.duracionEstimada = duracionEstimada;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFinalizacion() {
        return fechaFinalizacion;
    }

    public void setFechaFinalizacion(LocalDateTime fechaFinalizacion) {
        this.fechaFinalizacion = fechaFinalizacion;
    }



    public IScrimState getEstado() {
        return estado;
    }

    public void setEstado(IScrimState estado) {
        this.estado = estado;
    }

    public String getCreadorId() {
        return creadorId;
    }

    public void setCreadorId(String creadorId) {
        this.creadorId = creadorId;
    }


    public List<String> getJugadoresInscritos() {
        return Collections.unmodifiableList(jugadoresInscritos);
    }

    public int getJugadoresActuales() {
        return jugadoresActuales;
    }

    public void setJugadoresActuales(int jugadoresActuales) {
        this.jugadoresActuales = jugadoresActuales;
    }

    public String getGanadorId() {
        return ganadorId;
    }

    public void setGanadorId(String ganadorId) {
        this.ganadorId = ganadorId;
    }

    public String getNombreEstadoActual() {
        return estado.getNombreEstado();
    }

    public Resultados getResultados() {
        return resultados;
    }

    public void setResultados(Resultados resultados) {
        this.resultados = resultados;
    }

    public void mostrarInfo() {
        System.out.println("\n=== INFORMACIÓN DEL SCRIM ===");
        System.out.println("ID: " + id);
        System.out.println("Estado: " + estado.getNombreEstado());
        System.out.println("Juego: " + juego.getNombre());
        System.out.println("Modalidad: " + jugadoresPorLado + " vs " + jugadoresPorLado);
        System.out.println("Región: " + region);
        // la fecha programada
        System.out.println("Fecha programada: " + (fechaHora != null ? fechaHora : "-"));
        //  tendrán valor luego de iniciar/finalizar
        System.out.println("Fecha/Hora inicio: " + (fechaInicio != null ? fechaInicio : "-"));
        System.out.println("Fecha/Hora fin: " + (fechaFinalizacion != null ? fechaFinalizacion : "-"));
        System.out.println("\nEquipo A (" + equipoA.size() + "/" + jugadoresPorLado + "):");
        equipoA.forEach(u -> System.out.println("  - " + u.getEmail()));
        System.out.println("\nEquipo B (" + equipoB.size() + "/" + jugadoresPorLado + "):");
        equipoB.forEach(u -> System.out.println("  - " + u.getEmail()));
        System.out.println("============================\n");
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isEsPrivado() {
        return esPrivado;
    }

    public void setEsPrivado(boolean esPrivado) {
        this.esPrivado = esPrivado;
    }

    public String getCodigoAcceso() {
        return codigoAcceso;
    }

    public void setCodigoAcceso(String codigoAcceso) {
        this.codigoAcceso = codigoAcceso;
    }
    public List<User> getEquipoA() {
        return equipoA;
    }

    public List<User> getEquipoB() {
        return equipoB;
    }



    // ===== EQUALS, HASHCODE, TOSTRING =====

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scrim scrim = (Scrim) o;
        return Objects.equals(id, scrim.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Scrim{" +
                "id='" + id + '\'' +
                ", juego='" + juego.getNombre() + '\'' +
                ", formato='" + formato + '\'' +
                ", region='" + region + '\'' +
                ", estado=" + estado +
                ", jugadores=" + jugadoresActuales + "/" + cantidadTotalJugadores +
                ", fechaHora=" + fechaHora +
                '}';
    }
}