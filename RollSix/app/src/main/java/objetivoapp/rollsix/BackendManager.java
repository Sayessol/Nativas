package objetivoapp.rollsix;
import android.widget.ListView;

import java.util.List;


// Clase para manejar la comunicación con el backend
public class BackendManager {
    // Método para enviar la ganancia más alta al backend
    public void enviarGananciaMasAlta(int nuevaGanancia) {
        // Lógica para enviar la nueva ganancia al backend (usando Retrofit, Volley u otra librería)
    }

    // Método para recuperar el top ten de puntuaciones del backend
    public List<Integer> obtenerTopTen() {
        // Lógica para recuperar el top ten del backend y devolverlo como una lista
        return listaTopTen;
    }
}
