package org.example.model;


public class BuscandoJugadoresState extends ScrimStateBase {

    public BuscandoJugadoresState(Scrim scrim) {
        super(scrim);
    }

    @Override
    public void agregarJugador(User usuario) {
        // Validar requisitos de rango si existen
        if (scrim.getRangoMin() != null || scrim.getRangoMax() != null) {
            if (!scrim.cumpleRequisitosRango(usuario.getPerfil().getRango().getValorNivel())) {
                throw new IllegalArgumentException("El jugador no cumple con los requisitos de rango");
            }
        }

        // Validar región
        if (scrim.getRegion() != null && !scrim.getRegion().equals(usuario.getRegion())) {
            throw new IllegalArgumentException("El jugador no pertenece a la región correcta");
        }

        // Agregar jugador a un equipo (lógica de balanceo simple)
        if (scrim.getEquipoA().size() < scrim.getJugadoresPorLado()) {
            scrim.getEquipoA().add(usuario);
        } else if (scrim.getEquipoB().size() < scrim.getJugadoresPorLado()) {
            scrim.getEquipoB().add(usuario);
        } else {
            throw new IllegalStateException("Los equipos ya están completos");
        }

        scrim.setJugadoresActuales(scrim.getJugadoresActuales() + 1);
        System.out.println("Jugador agregado: " + usuario.getEmail() +
                " (" + scrim.getJugadoresActuales() + "/" + scrim.getCantidadTotalJugadores() + ")");

        // Verificar si se completó el cupo
        if (scrim.estaCompleto()) {
            System.out.println("¡Cupo completo! Pasando a Lobby Armado");
            scrim.cambiarEstado(new LobbyArmadoState(scrim));
        }
    }

    @Override
    public String getNombreEstado() {
        return "Buscando Jugadores";
    }
}

