package objetivoapp.rollsix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import objetivoapp.rollsix.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Database database = new Database(this);
        database.cargarDatos();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);

        Player jugador = database.obtenerJugadorPorId("1");

        // Hacer lo que necesites con los datos del jugador...
        if (jugador != null) {
            String emailJugador = jugador.getEmail();
            int saldoJugador = jugador.getSaldo();
            String saldoString = String.valueOf(saldoJugador);
        intent.putExtra("EMAIL_USUARIO", jugador.getEmail()); // Reemplaza emailDelUsuario con el email obtenido
        intent.putExtra("SALDO", saldoString); // Reemplaza saldoDelUsuario con el saldo obtenido
        startActivity(intent);
    }
        setTheme(R.style.Theme_RollSix);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}