package objetivoapp.rollsix;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.Manifest;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.widget.PopupWindow;
import android.view.Gravity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;



import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class LogicaJuego extends AppCompatActivity {

    // Variables para almacenar las apuestas (debes configurarlas según tu lógica de apuestas)
    private FusedLocationProviderClient fusedLocationClient;
    private boolean apuestaMayor = true;  // Cambia a false si apuestas menor
    private boolean apuestaMenor = false; // Cambia a true si apuestas menor
    private boolean apuestaIgual = false; // Cambia a true si apuestas igual
    private static final int REQUEST_CODE_LOCATION = 1001;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logica_juego);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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


            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String fechaActual = dateFormat.format(calendar.getTime());

String ubicacionActual=  obtenerUbicacionActual();
            // Obtener la ubicación actual


            // actualizar la base de datos y el saldoTextView si el jugador ganó
            if (resultadoFinal.equals("¡Ganaste!")) {
                jugador.setSaldo(jugador.getSaldo() + cantidadApostada);
                database.updatePlayer(jugador);


                // Actualizar saldoTextView
                saldoTextView.setText(String.valueOf(jugador.getSaldo()));

                // Añadir nueva partida a la tabla "partida"
                database.updateHistorial(jugador.getId(), String.valueOf(cantidadApostada), fechaActual, ubicacionActual);

                // Mostrar ventana emergente si el jugador ganó
                mostrarVentanaEmergente();
            }

            // Actualizar la base de datos y el saldoTextView si el jugador perdió
            if (resultadoFinal.equals("Perdiste. Intenta de nuevo.")) {
                jugador.setSaldo(jugador.getSaldo() - cantidadApostada);
                database.updatePlayer(jugador);

                // Actualizar saldoTextView
                saldoTextView.setText(String.valueOf(jugador.getSaldo()));

                // Añadir nueva partida a la tabla "partida" con ganancias negativas
                database.updateHistorial(jugador.getId(), String.valueOf(cantidadApostada), fechaActual, ubicacionActual);
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
            }, 8000); // Ajusta el tiempo de demora según tus preferencias
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

    private void mostrarVentanaEmergente() {
        // Inflar el diseño de la ventana emergente
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window_layout, null);

        // Configurar los botones en la ventana emergente
        Button capturaBtn = popupView.findViewById(R.id.capturaBtn);
        Button cerrarBtn = popupView.findViewById(R.id.cerrarBtn);

        // Configurar el OnClickListener para el botón de captura
        capturaBtn.setOnClickListener(v -> {
            // Captura de pantalla y guardar en la carpeta "screenshots"
            captureScreen();
            popupWindow.dismiss(); // Cerrar la ventana emergente después de capturar
        });

        // Configurar el OnClickListener para el botón de cerrar
        cerrarBtn.setOnClickListener(v -> popupWindow.dismiss());

        // Crear y mostrar la ventana emergente
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void captureScreen() {
        View rootView = getWindow().getDecorView().getRootView();
        rootView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getDrawingCache());
        rootView.setDrawingCacheEnabled(false);

        // Guardar la captura de pantalla en la carpeta "Pictures/Screenshots"
        String filename = "screenshot_" + System.currentTimeMillis() + ".png";
        String directoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Screenshots/";
        String filePath = directoryPath + filename;

        try {
            // Crear directorio si no existe
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Crear el archivo
            File file = new File(filePath);

            // Crear el flujo de salida para el archivo
            FileOutputStream outputStream = new FileOutputStream(file);

            // Comprimir y guardar la captura de pantalla en el archivo
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Escanear el archivo para que aparezca en la galería
            MediaScannerConnection.scanFile(this, new String[]{file.getPath()}, null, null);

            Toast.makeText(this, "Captura de pantalla guardada en la carpeta 'Pictures/Screenshots'", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al guardar la captura de pantalla", Toast.LENGTH_SHORT).show();
        }
    }

    private String obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
            return null;
        }
        AtomicReference<String> ubicacionActual= new AtomicReference<>("0,0");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitud = location.getLatitude();
                        double longitud = location.getLongitude();

                        ubicacionActual.set(latitud + ", " + longitud);

                        // Utiliza la ubicación obtenida como sea necesario aquí
                        Toast.makeText(LogicaJuego.this, "Ubicación: " + ubicacionActual, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(this, e -> {
                    // Manejar cualquier error al obtener la ubicación
                    Toast.makeText(LogicaJuego.this, "Error al obtener la ubicación: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
        return ubicacionActual.get();
    }



}
