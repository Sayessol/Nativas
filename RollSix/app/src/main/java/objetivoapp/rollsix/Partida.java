package objetivoapp.rollsix;

public class Partida {

    private String partidaId;
    private String jugadorId;
    private String ganancias;

    // Constructor
    public Partida(String partidaId, String jugadorId, String ganancias) {
        this.partidaId = partidaId;
        this.jugadorId = jugadorId;
        this.ganancias = ganancias;
    }

    // Getters y setters
    public String getPartidaId() {
        return partidaId;
    }

    public void setPartidaId(String partidaId) {
        this.partidaId = partidaId;
    }

    public String getJugadorId() {
        return jugadorId;
    }

    public void setJugadorId(String jugadorId) {
        this.jugadorId = jugadorId;
    }

    public String getGanancias() {
        return ganancias;
    }

    public void setGanancias(String ganancias) {
        this.ganancias = ganancias;
    }

}
