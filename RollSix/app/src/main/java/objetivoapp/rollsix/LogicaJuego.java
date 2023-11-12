package objetivoapp.rollsix;

import android.content.Intent;
import android.os.Bundle;
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
        myImageView.setImageResource(R.drawable.dice);
        EditText editTextNumber = findViewById(R.id.editTextNumber2);

        // configurmos el OnClickListener para el botón con expresión lambda porque me daba alerta
        jugarButton.setOnClickListener(v -> {

            // Obtener la cantidad apostada del EditText

                    int cantidadApostada = Integer.parseInt(editTextNumber.getText().toString());

                    // Verificar si la apuesta es menor o igual al saldo del jugador
                    if (cantidadApostada <= s) {
                        // lógica del juego
                        // lógica del juego
                        int num1, num2, resultado;
                        Random rand = new Random();
                        num1 = rand.nextInt(6) + 1;
                        num2 = rand.nextInt(6) + 1;
                        resultado = num1 + num2;

                        // crea string con el resultado
                        String partida = getString(R.string.resultado_partida, resultado, num1, num2);

                        // verificar el resultado del juego
                        String resultadoFinal = verificarResultado(num1, num2,cantidadApostada);

                        // Actualizar la base de datos y el saldoTextView si el jugador ganó
                        if (resultadoFinal.equals("¡Ganaste!")) {
                            jugador.setSaldo(jugador.getSaldo() + cantidadApostada);
                            database.updatePlayer(jugador);

                            // Actualizar saldoTextView
                            saldoTextView.setText(String.valueOf(jugador.getSaldo()));

                            // Añadir nueva partida a la tabla "partida"
                            database.updateHistorial(jugador.getId(), String.valueOf(cantidadApostada));
                        }

                        // mostrar resultado en TextView
                        resultadoTextView.setText(partida + "\n" + resultadoFinal);
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        Intent intent2 = getIntent();
                        intent2 = new Intent(LogicaJuego.this, Historial.class);
                        String email = intent2.getStringExtra(jugador.getEmail());
                        int s2 = jugador.getSaldo();
                        String s3 = intent2.getStringExtra(String.valueOf(s2));
                        intent2.putExtra("EMAIL_USUARIO", email);
                        intent2.putExtra("SALDO",s3);
                        startActivity(intent2);

                    } else {
                        // Mostrar mensaje si la apuesta es mayor que el saldo
                        Toast.makeText(LogicaJuego.this, "No puedes apostar más dinero del que tienes.", Toast.LENGTH_SHORT).show();
                    }
        });
    }

    // Método para verificar el resultado del juego
    private String verificarResultado(int dado1, int dado2,int cantidadApostada) {
        int suma = dado1 + dado2;

        // Lógica del juego
        if ((suma > 6 && apuestaMayor) || (suma < 6 && apuestaMenor)) {

            return "¡Ganaste!";
        } else if (suma == 6) {
            return "Empate! intenta de nuevo";
        } else if (suma == 6 && apuestaIgual) {
            return "Has empatado, inténtalo de nuevo";
        } else {
            return "Perdiste. Intenta de nuevo.";
        }
    }
}


