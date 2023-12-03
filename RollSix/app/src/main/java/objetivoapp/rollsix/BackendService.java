package objetivoapp.rollsix;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Body;

public interface BackendService {

    // Método para obtener el top ten de puntuaciones
    @GET("top-ten")
    Call<List<Integer>> obtenerTopTen();

    // Método para enviar la ganancia más alta al backend
    @POST("enviar-ganancia")
    Call<Void> enviarGananciaMasAlta(@Body int nuevaGanancia);

    Call<List<Integer>> getTopTen();
    @POST("ruta_para_enviar_ganancia")
    Call<Void> enviarNuevaGanancia(@Body int nuevaGanancia);
}
