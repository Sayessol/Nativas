package objetivoapp.rollsix;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import objetivoapp.rollsix.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

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
    }
}