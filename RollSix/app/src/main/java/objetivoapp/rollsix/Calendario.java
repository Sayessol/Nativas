package objetivoapp.rollsix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class Calendario extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);

        Database db = new Database(this);
        // Recibir los datos del jugador pasados desde la actividad anterior
        Intent intent = getIntent();
        String emailUsuario = intent.getStringExtra("EMAIL_USUARIO");
        String saldo = intent.getStringExtra("SALDO");


        // Mostrar los datos en los TextView correspondientes en la actividad Historial
        TextView emailTextView = findViewById(R.id.emailTextView);
        emailTextView.setText(emailUsuario);

        TextView saldoTextView = findViewById(R.id.saldoTextView);
        saldoTextView.setText(saldo);

// Obtener referencias de los TextView
        TextView ubicacionTextView = findViewById(R.id.ubicacionTextView);
        TextView fechaTextView = findViewById(R.id.fechaTextView);

// Cuando tengas la información de ubicación y fecha de victoria
        String ubicacion = "Tu ubicación";
        String fecha = "Tu fecha";

        CalendarView calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String fechaSeleccionada = year + "-" + (month + 1) + "-" + dayOfMonth; // Formato de fecha a consultar

                // Obtener el ID de la partida para buscar la ruta de la imagen asociada
                int idPartida = db.obtenerUltimoIdPartidaconRuta(fechaSeleccionada);


// Verificar si la partida tiene una ruta asociada
                String rutaImagen = db.obtenerRutaImagenPorIdPartida(idPartida);
                       if (rutaImagen != null && !rutaImagen.isEmpty()) {
                            try {
                                // Cargar la imagen y mostrarla en el ImageView
                                Bitmap imagenPartida = BitmapFactory.decodeFile(rutaImagen);

                                ImageView imageViewPartida = findViewById(R.id.imageViewPartida);
                                imageViewPartida.setImageBitmap(imagenPartida);
                                imageViewPartida.setVisibility(View.VISIBLE); // Hacer visible el ImageView
                            } catch (Exception e) {
                                e.printStackTrace();
                                // Manejar la excepción (por ejemplo, mostrar un mensaje de error)
                                Toast.makeText(Calendario.this, "Error al cargar la imagen", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Si no hay una ruta de imagen asociada, ocultar el ImageView
                            ImageView imageViewPartida = findViewById(R.id.imageViewPartida);
                            imageViewPartida.setVisibility(View.GONE);
                        }
            }


        });


        // En el método onCreate de tu actividad CalendarioActivity
        Button btnVolver = findViewById(R.id.btnVolver);
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Player p = db.obtenerJugadorPorEmail(emailUsuario);
                String s = p.getId();
                Intent intent = new Intent(Calendario.this, InstruccionesActivity.class);
                intent.putExtra("ID_USUARIO", s);
                startActivity(intent);

                // Cierra la actividad actual (Calendario) para reiniciarla al volver a LogicaJuego
                finish();
            }
        });
    }
}


