package objetivoapp.rollsix;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;
public class LogicaJuego extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logica_juego);

        // obtener referencia al TextView en tu layout
        TextView resultadoTextView = findViewById(R.id.resultadoTextView);

        // obtener referencia al botón en tu layout
        Button jugarButton = findViewById(R.id.jugarButton);

        // configurmos el OnClickListener para el botón con expresion lambda porque me daba alerta
        jugarButton.setOnClickListener(v -> {
            // lógica del juego
            int num1, num2, resultado;
            Random rand = new Random();
            num1 = rand.nextInt(6) + 1;
            num2 = rand.nextInt(6) + 1;
            resultado = num1 + num2;

            // crea string con el resultado
            String partida = getString(R.string.resultado_partida, resultado, num1, num2);

            // mostrar resultado en TextView
            resultadoTextView.setText(partida);
        });
    }
}
