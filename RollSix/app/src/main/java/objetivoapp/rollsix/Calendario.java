package objetivoapp.rollsix;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageView;
import android.widget.TextView;

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

    // Configurar el calendario para mostrar las fechas con partidas ganadas
    CalendarView calendarView = findViewById(R.id.calendarView);

    calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
        @Override
        public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
            String fechaSeleccionada = year + "-" + (month + 1) + "-" + dayOfMonth; // Formato de fecha a consultar
            List<String> fechasConVictorias = db.obtenerPartidasConVictoriasPorFechaYUbicacion(emailUsuario,fechaSeleccionada);
            // Verificar si la fecha seleccionada tiene una partida con victoria asociada
            if (fechasConVictorias.contains(fechaSeleccionada)) {
                // Verificar si la fecha seleccionada tiene una partida con victoria asociada
                if (!fechasConVictorias.isEmpty()) {
                    // Obtener la primera partida (asumiendo que solo hay una para esa fecha)
                    String partida = fechasConVictorias.get(0);

                    // Separar la ubicación y la fecha de la partida
                    String[] infoPartida = partida.split(","); // Ajusta esto según el formato de tu información

                    if (infoPartida.length >= 2) { // Verificar si hay al menos ubicación y fecha
                        String ubicacion = infoPartida[0].trim(); // Ubicación
                        String fecha = infoPartida[1].trim(); // Fecha

                        // Mostrar los TextView y establecer la información
                        ubicacionTextView.setVisibility(View.VISIBLE);
                        fechaTextView.setVisibility(View.VISIBLE);

                        ubicacionTextView.setText("Ubicación: " + ubicacion);
                        fechaTextView.setText("Fecha: " + fecha);
                    }
                } else {
                    // Si no hay partidas con victorias para esa fecha, ocultar los TextView
                    ubicacionTextView.setVisibility(View.GONE);
                    fechaTextView.setVisibility(View.GONE);
                }


                // Si hay una victoria en esta fecha, muestra la imagen correspondiente en el ImageView
                // Utiliza la lógica para cargar y mostrar la imagen en el ImageView
                // Aquí se asume que tienes una función para obtener la imagen de la partida por fecha
              /*  Bitmap imagenPartida = db.obtenerImagenPartidaPorFecha(emailJugador, fechaSeleccionada);

                // Luego de obtener la imagen, muestrala en el ImageView
                ImageView imageViewPartida = findViewById(R.id.imageViewPartida);
                imageViewPartida.setImageBitmap(imagenPartida);
                imageViewPartida.setVisibility(View.VISIBLE); // Hacer visible el ImageView*/
            } else {
                // Si no hay victoria en esta fecha, oculta el ImageView
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
            Intent intent = new Intent(Calendario.this, LogicaJuego.class);
            startActivity(intent);
        }
    });
    }
}


