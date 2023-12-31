package objetivoapp.rollsix.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import objetivoapp.rollsix.Database;
import objetivoapp.rollsix.InstruccionesActivity;
import objetivoapp.rollsix.Player;
import objetivoapp.rollsix.R;
import objetivoapp.rollsix.Registrar;
import objetivoapp.rollsix.Webview;
import objetivoapp.rollsix.ui.login.LoginViewModel;
import objetivoapp.rollsix.ui.login.LoginViewModelFactory;
import objetivoapp.rollsix.databinding.ActivityLoginBinding;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;
    private static final int RC_SIGN_IN = 9001; // Código de solicitud para el inicio de sesión con Google
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        mAuth = FirebaseAuth.getInstance(); // Inicializar Firebase Auth

        // Configurar el cliente de inicio de sesión de Google
        configureGoogleSignIn();
        Button googleSignInButton = binding.googleSignInButton;
        googleSignInButton.setOnClickListener(view -> signInWithGoogle());


        // Obtén una referencia al botón de registro
        Button registroButton = binding.registro;

        // Establece un OnClickListener para el botón de registro
        registroButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicia la actividad Registro.class
                Intent intent = new Intent(LoginActivity.this, Registrar.class);
                startActivity(intent);
            }
        });
        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            Toast.makeText(this, "Error en la autenticación con Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUiWithUser(user);
                    } else {
                        Toast.makeText(LoginActivity.this, "Error en la autenticación con Firebase", Toast.LENGTH_SHORT).show();
                    }
                });
    }


        private void updateUiWithUser(Object user) {
            Database database = new Database(this);

        if (user instanceof LoggedInUserView) {
            // Este bloque se ejecuta si el usuario es de tipo LoggedInUserView
            LoggedInUserView loggedInUser = (LoggedInUserView) user;
            // Puedes utilizar loggedInUser para obtener la información necesaria
        } else if (user instanceof FirebaseUser) {
            // Este bloque se ejecuta si el usuario es de tipo FirebaseUser
            FirebaseUser firebaseUser = (FirebaseUser) user;

            // Obtener el jugador de la base de datos local
            Player jugador = database.obtenerJugadorPorEmail(firebaseUser.getEmail());

            if (!database.existeJugador(firebaseUser.getEmail())) {
                // Si el jugador no existe, agrégalo a la base de datos local
                int saldoInicial = 1;
                jugador = new Player(firebaseUser.getUid(), firebaseUser.getEmail(), "1234", saldoInicial);
                database.insertJugador(jugador);
            }

            // Iniciar la actividad de instrucciones
            Intent intent = new Intent(LoginActivity.this, InstruccionesActivity.class);
            intent.putExtra("ID_USUARIO", jugador.getId());
            startActivity(intent);

            // Finalizar la actividad actual (LoginActivity)
            finish();
        } else {
            // Este bloque se ejecuta si el tipo de usuario no es reconocido
            Toast.makeText(this, "Tipo de usuario no reconocido", Toast.LENGTH_SHORT).show();
        }
    }


    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    public void openHelpActivity(View view) {
        Intent intent = new Intent(this, Webview.class);
        startActivity(intent);
    }

}