package org.example.model.scrim.state;

import org.example.model.scrim.Scrim;
import org.example.model.user.User;

import java.util.HashSet;
import java.util.Set;

public class LobbyArmadoState extends ScrimStateBase {
    private Set<String> jugadoresConfirmados;

    public LobbyArmadoState(Scrim scrim) {
        super(scrim);
        this.jugadoresConfirmados = new HashSet<>();
    }

    @Override
    public void agregarJugador(User usuario) {
        System.out.println("El cupo ya está completo. No se pueden agregar más jugadores.");
    }

    @Override
    public void confirmar(User usuario) {
        if (!scrim.getEquipoA().contains(usuario) && !scrim.getEquipoB().contains(usuario)) {
            throw new IllegalArgumentException("El usuario no está en este scrim");
        }

        if (jugadoresConfirmados.contains(usuario.getId())) {
            System.out.println("El jugador ya había confirmado: " + usuario.getEmail());
            return;
        }

        jugadoresConfirmados.add(usuario.getId());
        System.out.println("Jugador confirmado: " + usuario.getEmail() +
                " (" + jugadoresConfirmados.size() + "/" + scrim.getCantidadTotalJugadores() + ")");

        // Verificar si todos confirmaron
        if (jugadoresConfirmados.size() == scrim.getCantidadTotalJugadores()) {
            System.out.println("¡Todos los jugadores han confirmado! Pasando a Confirmado");
            scrim.cambiarEstado(new ConfirmadoState(scrim));
        }
    }

    @Override
    public String getNombreEstado() {
        return "Lobby Armado";
    }

    public int getJugadoresConfirmados() {
        return jugadoresConfirmados.size();
    }
}

