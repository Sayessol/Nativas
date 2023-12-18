package objetivoapp.rollsix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Historial extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial); // Así asumimos que el XML se llama "layout_principal"

        // Recibir los datos del jugador pasados desde la actividad anterior
        Intent intent = getIntent();
        String emailUsuario = intent.getStringExtra("EMAIL_USUARIO");
        String saldo = intent.getStringExtra("SALDO");



        // Mostrar los datos en los TextView correspondientes en la actividad Historial
        TextView emailTextView = findViewById(R.id.emailTextView);
        emailTextView.setText(emailUsuario);

        TextView saldoTextView = findViewById(R.id.saldoTextView);
        saldoTextView.setText(saldo);

        // Aquí se hace la referencia a tu botón y se agrega el Listener para cerrar la aplicación
        Button botonCerrar = findViewById(R.id.botonCerrar);
        botonCerrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Historial.this, Calendario.class);
                intent.putExtra("EMAIL_USUARIO", emailUsuario);
                intent.putExtra("SALDO", saldo);
                startActivity(intent);
            }
        });

        // Referencia al ListView
        ListView historialListView = findViewById(R.id.historialListView);

        Database database = new Database(this);

        // Obtener el jugador por su email
        Player jugador = database.obtenerJugadorPorEmail(emailUsuario);

        if (jugador != null) {
            // Obtener el id del jugador
            String idJugador = jugador.getId();
            // En tu método dentro del Activity
            // Obtener las partidas del jugador utilizando la función sin RxJava
            ArrayList<String> partidasJugador = database.obtenerPartidasPorIdJugador(idJugador);

// Crear un ArrayAdapter con los datos obtenidos
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, partidasJugador);

// Mostrar cada elemento del adaptador en un Toast y configurar el adaptador en el ListView
           Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> {
                for (int i = 0; i < adapter.getCount(); i++) {
                    final int finalI = i;
                    handler.postDelayed(() -> {
                        String item = adapter.getItem(finalI);
                        if (item != null) {
                            //Toast.makeText(getApplicationContext(), item, Toast.LENGTH_SHORT).show();
                        }
                    }, 1000 * i); // Muestra cada elemento del adaptador cada segundo (1000 ms)
                }
                historialListView.setAdapter(adapter);
            });

            // Referencia al botón "Enviar datos al multijugador"
            Button botonEnviarDatos = findViewById(R.id.botonEnviarDatosMultijugador);
            botonEnviarDatos.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Obtener las ganancias más altas del historial
                    int gananciasAltas = database.obtenerGananciaMasAltaPorIdJugador(idJugador);
                    Toast.makeText(Historial.this, String.valueOf(gananciasAltas), Toast.LENGTH_SHORT).show();

                    // Enviar los datos del jugador y las ganancias altas a la actividad "Ranking"
                    int gananciaDelJuego = getIntent().getIntExtra("GANANCIA_JUEGO", 0);
                    Intent intent = new Intent(Historial.this, Ranking.class);
                    intent.putExtra("EMAIL_USUARIO", emailUsuario);
                    intent.putExtra("GANANCIAS_ALTAS", gananciasAltas);
                    intent.putExtra("GANANCIA_DEL_JUEGO", gananciaDelJuego);
                    startActivity(intent);
                }
            });

/*
            // Resto del código...
            // Obtener la lista de partidas asociadas al jugador por su id
            ArrayList<String> partidasJugador = database.obtenerPartidasPorIdJugador(idJugador);

            // Llenar la ListView con las partidas obtenidas
            historialListView = findViewById(R.id.historialListView);

            // Crear un ArrayAdapter para mostrar los datos en el ListView
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, partidasJugador);

            // Establecer el adaptador para el ListView
            historialListView.setAdapter(adapter);*/
        }
    }
}
