package objetivoapp.rollsix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Ranking extends Activity {

    private Database database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        database = new Database(this);

        // Recibir los datos del jugador y la ganancia más alta pasados desde la actividad anterior
        Intent intent = getIntent();
        String emailUsuario = intent.getStringExtra("EMAIL_USUARIO");
        int gananciaMasAlta = intent.getIntExtra("GANANCIA_MAS_ALTA", 0); // 0 es el valor predeterminado si no se encuentra

        // Obtener datos del jugador por email
        Player jugador = database.obtenerJugadorPorEmail(emailUsuario);

        if (jugador != null) {
            // Mostrar datos del jugador en los TextView correspondientes
            TextView emailTextView = findViewById(R.id.emailTextView);
            emailTextView.setText(jugador.getEmail());

            TextView saldoTextView = findViewById(R.id.saldoTextView);
            saldoTextView.setText(String.valueOf(jugador.getSaldo()));

            // Manejo del botón "Volver" para regresar a la pantalla de instrucciones
            Button botonVolver = findViewById(R.id.botonVolver);
            botonVolver.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Ranking.this, LogicaJuego.class);
                    Player p = database.obtenerJugadorPorEmail(emailUsuario);
                    intent.putExtra("ID_USUARIO", p.getId());
                    startActivity(intent);
                }
            });
        }
    }
}
