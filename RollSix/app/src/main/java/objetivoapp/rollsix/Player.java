package objetivoapp.rollsix;

public class Player {

    private String id;
    private String email;
    private String password;
    private int saldo; // Saldo es un entero, ajusta el tipo de dato según corresponda

    // Constructor
    public Player(String id, String email, String password, int saldo) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.saldo = saldo;
    }

    // Getters y setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

}
