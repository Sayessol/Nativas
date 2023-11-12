package objetivoapp.rollsix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import objetivoapp.rollsix.ui.login.LoginActivity;

public class Registrar extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText repeatPasswordEditText;
    private Button confirmarButton;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        repeatPasswordEditText = findViewById(R.id.repeatPassword);
        confirmarButton = findViewById(R.id.confirmar);
        loadingProgressBar = findViewById(R.id.loading);

        confirmarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String repeatPassword = repeatPasswordEditText.getText().toString();

        // Verificar si el email ya existe en la base de datos
        if (emailExiste(email)) {
            mostrarMensaje("El correo electrónico ya está registrado");
            return;
        }

        // Verificar que las contraseñas tengan al menos 5 caracteres
        if (password.length() < 5 || repeatPassword.length() < 5) {
            mostrarMensaje("Las contraseñas deben tener al menos 5 caracteres");
            return;
        }

        // Verificar que las contraseñas coincidan
        if (!password.equals(repeatPassword)) {
            mostrarMensaje("Las contraseñas no coinciden");
            return;
        }

        // Guardar el nuevo jugador en la base de datos
        Database database = new Database(this);
        Player nuevoJugador = new Player(email, password, 100); // Nuevo jugador con saldo inicial de 100
        database.insertJugador(nuevoJugador);

        // Mostrar mensaje de éxito y cambiar a la pantalla de inicio de sesión
        mostrarMensaje("Usuario registrado exitosamente");
        cambiarPantallaLogin();
    }

    private boolean emailExiste(String email) {
        // Lógica para verificar si el email ya existe en la base de datos
        Database database = new Database(this);
        Player jugadorExistente = database.obtenerJugadorPorEmail(email);
        return jugadorExistente != null;
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    private void cambiarPantallaLogin() {
        // Lógica para cambiar a la pantalla de inicio de sesión (por ejemplo, mediante un Intent)
        Intent intent = new Intent(Registrar.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Para cerrar la actividad actual y evitar que el usuario vuelva atrás con el botón de retroceso
    }
}
