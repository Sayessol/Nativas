package objetivoapp.rollsix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import android.widget.Toast;

import java.util.List;

public class Ranking extends Activity {

    private Database database;
    private ListView listView;
    private ArrayAdapter<Integer> adapter;
    private BackendManager backendManager;
    private List<Integer> topTenList;

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

            // Inicialización del ListView y Adapter
            listView = findViewById(R.id.rankingListView);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            listView.setAdapter(adapter);

            backendManager = new BackendManager();

            // Obtener y mostrar el top ten al inicio
            actualizarTopTen();
            manejarNuevaGanancia(gananciaMasAlta);






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
                // Método para actualizar el top ten y el ListView

                // Método para manejar la nueva ganancia más alta

            });
        }

        }

    private void actualizarTopTen() {
        // Obtener el top ten del backend
        topTenList = backendManager.obtenerTopTen();

        // Limpiar y actualizar el ListView
        adapter.clear();
        adapter.addAll(topTenList);
        adapter.notifyDataSetChanged();
    }

    private void manejarNuevaGanancia(int nuevaGanancia) {
        // Comprobar si la nueva ganancia entra en el top ten
        if (nuevaGanancia > topTenList.get(topTenList.size() - 1)) {
            // Enviar la nueva ganancia al backend
            backendManager.enviarGananciaMasAlta(nuevaGanancia);

            // Actualizar el top ten y el ListView
            actualizarTopTen();
        }
    }
}
