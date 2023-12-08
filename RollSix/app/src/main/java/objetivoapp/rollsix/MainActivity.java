package objetivoapp.rollsix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import objetivoapp.rollsix.ui.login.LoginActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private static MediaPlayer mediaPlayer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Thread.sleep(9000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Database database = new Database(this);
        //database.cargarDatos();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);

        setTheme(R.style.Theme_RollSix);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar el MediaPlayer con el archivo de música
        mediaPlayer = MediaPlayer.create(this, R.raw.musicafondo);
        // Iniciar la reproducción en bucle (si lo deseas)
        mediaPlayer.setLooping(true);
        // Iniciar la reproducción
        mediaPlayer.start();
        //Inicializar Firebase
        FirebaseApp.initializeApp(this);


    }
    public static MediaPlayer obtenerMediaPlayer() {
        return mediaPlayer;
    }


    public static void controlarReproduccionMusica(MediaPlayer mediaPlayer, boolean reproducir) {
        if (reproducir) {
            // Iniciar la reproducción si no está reproduciendo
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } else {
            // Pausar la reproducción si está reproduciendo
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "onDestroy called");

        // Obtener la instancia actual de MediaPlayer
        MediaPlayer mediaPlayer = obtenerMediaPlayer();

        // Detener la reproducción de la música si está reproduciendo
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }



}