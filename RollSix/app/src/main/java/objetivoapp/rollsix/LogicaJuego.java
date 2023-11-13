package objetivoapp.rollsix;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class LogicaJuego extends AppCompatActivity {

    // Variables para almacenar las apuestas (debes configurarlas según tu lógica de apuestas)
    private boolean apuestaMayor = true;  // Cambia a false si apuestas menor
    private boolean apuestaMenor = false; // Cambia a true si apuestas menor
    private boolean apuestaIgual = false; // Cambia a true si apuestas igual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logica_juego);

        // Recibir los datos del jugador pasados desde la actividad anterior
        Intent intent = getIntent();
        String idUsuario = intent.getStringExtra("ID_USUARIO");

        Database database = new Database(this);

        Player jugador = database.obtenerJugadorPorId(idUsuario);

        TextView emailTextView = findViewById(R.id.emailTextView);
        emailTextView.setText(jugador.getEmail());

        TextView saldoTextView = findViewById(R.id.saldoTextView);
        int s = jugador.getSaldo();
        saldoTextView.setText(String.valueOf(s));

        // obtener referencia al TextView en tu layout
        TextView resultadoTextView = findViewById(R.id.resultadoTextView);

        // obtener referencia al botón en tu layout
        Button jugarButton = findViewById(R.id.jugarButton);

        //Declarar la imagen en la pantalla de juego
        ImageView myImageView = findViewById(R.id.myIMG);
        myImageView.setImageResource(R.drawable.transpdices);
        EditText editTextNumber = findViewById(R.id.editTextNumber2);

        // obtener referencia al botón para apostar MENOR
        Button botonApostarMenor = findViewById(R.id.menorBtn);

        // obtener referencia al botón para apostar MAYOR
        Button botonApostarMayor = findViewById(R.id.mayorBtn);

        //configuramos el OnClickListener para el botón MENOR
        botonApostarMenor.setOnClickListener(v -> {
            apuestaMenor = true;
            apuestaMayor = false;
            apuestaIgual = false;
        });

        // configuramos el OnClickListener para el botón MAYOR
        botonApostarMayor.setOnClickListener(v -> {
            apuestaMenor = false;
            apuestaMayor = true;
            apuestaIgual = false;
        });

        // configurmos el OnClickListener para el botón con expresión lambda porque me daba alerta
        jugarButton.setOnClickListener(v -> {
            // Obtener la cantidad apostada del EditText
            String cantidadApostadaStr = editTextNumber.getText().toString();

            // Verificar si la cantidad apostada es válida
            if (cantidadApostadaStr.isEmpty()) {
                // Mostrar mensaje si no se ha ingresado ninguna cantidad
                Toast.makeText(LogicaJuego.this, "Debes ingresar una cantidad para apostar.", Toast.LENGTH_SHORT).show();
                return;
            }

            int cantidadApostada = Integer.parseInt(cantidadApostadaStr);

            // Verificar si la apuesta es menor o igual al saldo del jugador
            if (cantidadApostada <= 0) {
                // Mostrar mensaje si la apuesta es cero o menor que cero
                Toast.makeText(LogicaJuego.this, "La cantidad apostada debe ser mayor que cero.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (cantidadApostada > s) {
                // Mostrar mensaje si la apuesta es mayor que el saldo
                Toast.makeText(LogicaJuego.this, "No puedes apostar más dinero del que tienes.", Toast.LENGTH_SHORT).show();
                return;
            }

            // lógica del juego
            int num1, num2, resultado;
            Random rand = new Random();
            num1 = rand.nextInt(6) + 1;
            num2 = rand.nextInt(6) + 1;
            resultado = num1 + num2;

            // crea string con el resultado
            String partida = getString(R.string.resultado_partida, resultado, num1, num2);

            // verificar el resultado del juego
            String resultadoFinal = verificarResultado(num1, num2, cantidadApostada);

            // actualizar la base de datos y el saldoTextView si el jugador ganó
            if (resultadoFinal.equals("¡Ganaste!")) {
                jugador.setSaldo(jugador.getSaldo() + cantidadApostada);
                database.updatePlayer(jugador);

                // Actualizar saldoTextView
                saldoTextView.setText(String.valueOf(jugador.getSaldo()));

                // Añadir nueva partida a la tabla "partida"
                database.updateHistorial(jugador.getId(), String.valueOf(cantidadApostada));
            }

            // Actualizar la base de datos y el saldoTextView si el jugador perdió
            if (resultadoFinal.equals("Perdiste. Intenta de nuevo.")) {
                jugador.setSaldo(jugador.getSaldo() - cantidadApostada);
                database.updatePlayer(jugador);

                // Actualizar saldoTextView
                saldoTextView.setText(String.valueOf(jugador.getSaldo()));

                // Añadir nueva partida a la tabla "partida" con ganancias negativas
                database.updateHistorial(jugador.getId(), "-" + cantidadApostada);
            }

            // mostrar resultado en TextView
            resultadoTextView.setText(partida + "\n" + resultadoFinal);

            // Introducir una demora de 3 segundos (3000 milisegundos)
            new Handler().postDelayed(() -> {
                Intent intent2 = new Intent(LogicaJuego.this, Historial.class);
                intent2.putExtra("EMAIL_USUARIO", jugador.getEmail());
                intent2.putExtra("SALDO", String.valueOf(jugador.getSaldo()));
                startActivity(intent2);

                // Finalizar la actividad actual (LogicaJuego)
                finish();
            }, 3000); // Ajusta el tiempo de demora según tus preferencias
        });
    }

    // método para verificar el resultado del juego
    private String verificarResultado(int dado1, int dado2, int cantidadApostada) {
        int suma = dado1 + dado2;

        // Lógica del juego
        if (suma == 6 && apuestaIgual) {
            return "Has empatado, inténtalo de nuevo";
        } else if ((suma > 6 && apuestaMayor) || (suma < 6 && apuestaMenor)) {
            return "¡Ganaste!";
        } else {
            return "Perdiste. Intenta de nuevo.";
        }
    }
}