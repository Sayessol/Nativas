package objetivoapp.rollsix;
import android.widget.ListView;

import java.util.List;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;



// Clase para manejar la comunicación con el backend
public class BackendManager {
    private static final String BASE_URL = "URL_DEL_BACKEND"; // Reemplaza con la URL de tu backend

    private Retrofit retrofit;
    private BackendService backendService;

    public BackendManager() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        backendService = retrofit.create(BackendService.class);
    }

    public void enviarGananciaMasAlta(int nuevaGanancia, TopTenCallback2 callback) {
        // Lógica para enviar la nueva ganancia al backend utilizando Retrofit
        Call<Void> call = backendService.enviarNuevaGanancia(nuevaGanancia);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // La nueva ganancia se ha enviado correctamente al backend
                    callback.onSuccess(); // Llamar al callback onSuccess
                } else {
                    // La solicitud no fue exitosa, manejar el error si es necesario
                    callback.onFailure(); // Llamar al callback onFailure
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Error al enviar la nueva ganancia, manejar el fallo si es necesario
                callback.onFailure(); // Llamar al callback onFailure
            }
        });
    }

    // ... (otros métodos)

    // Callback para manejar el resultado de enviar la nueva ganancia
    public interface TopTenCallback2 {
        void onSuccess();
        void onFailure();
    }



    // Método para recuperar el top ten de puntuaciones del backend
    public void obtenerTopTen(final TopTenCallback callback) {
        // Lógica para recuperar el top ten del backend
        // Lógica para recuperar el top ten del backend
        backendService.getTopTen().enqueue(new Callback<List<Integer>>() {
            @Override
            public void onResponse(Call<List<Integer>> call, Response<List<Integer>> response) {
                if (response.isSuccessful()) {
                    List<Integer> topTenList = response.body();
                    callback.onSuccess(topTenList); // Envía el top ten al callback
                } else {
                    callback.onFailure(); // Manejar la falla
                }
            }

            @Override
            public void onFailure(Call<List<Integer>> call, Throwable t) {
                callback.onFailure(); // Manejar la falla
            }
        });
    }
    // Callback para manejar el resultado de obtener el top ten
    public interface TopTenCallback {
        void onSuccess(List<Integer> topTenList);

        void onFailure();
    }

}
