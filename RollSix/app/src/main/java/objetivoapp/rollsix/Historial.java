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
            // Mostrar el valor de idJugador en un Toast
            Toast.makeText(getApplicationContext(), "El ID del jugador es: " + idJugador, Toast.LENGTH_LONG).show();

            // En tu método dentro del Activity
            Disposable disposable = database.obtenerPartidasPorIdJugadorRx(idJugador)
                    .subscribeOn(Schedulers.io())
                    .subscribe(partidasJugador -> {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                    android.R.layout.simple_list_item_1, (List) partidasJugador);

                            historialListView.setAdapter(adapter);
                        });
                    }, throwable -> {
                        // Manejo de errores
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
