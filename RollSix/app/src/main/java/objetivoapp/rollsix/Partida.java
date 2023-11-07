package objetivoapp.rollsix;

public class Partida {

    private int partidaId;
    private int jugadorId;
    private int ganancias;

    // Constructor
    public Partida(int partidaId, int jugadorId, int ganancias) {
        this.partidaId = partidaId;
        this.jugadorId = jugadorId;
        this.ganancias = ganancias;
    }

    // Getters y setters
    public int getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(int partidaId) {
        this.partidaId = partidaId;
    }

    public int getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(int jugadorId) {
        this.jugadorId = jugadorId;
    }

    public int getGanancias() {
        return ganancias;
    }

    public void setGanancias(int ganancias) {
        this.ganancias = ganancias;
    }

}
