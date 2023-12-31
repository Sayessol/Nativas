package objetivoapp.rollsix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

public class Database extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "my_database.db";
    private static final int DATABASE_VERSION = 2; // Cambia la versión de la base de datos

    // Nombres de las tablas y columnas
    public static final String TABLE_PLAYER = "jugador";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_SALDO = "saldo";

    public static final String TABLE_PARTIDA = "partida";
    public static final String COLUMN_PARTIDA_ID = "id_partida";
    public static final String COLUMN_JUGADOR_ID = "id_jugador";
    public static final String COLUMN_GANANCIAS = "ganancias";

    public static final String TABLE_RUTA = "ruta";
    public static final String COLUMN_PARTIDA_IDR = "id_partidaR";
    public static final String COLUMN_RUTA_ID = "id_ruta";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla "jugador"
        String CREATE_PLAYER_TABLE = "CREATE TABLE " + TABLE_PLAYER + "("
                + COLUMN_ID + " TEXT PRIMARY KEY,"
                + COLUMN_EMAIL + " TEXT NOT NULL,"
                + COLUMN_PASSWORD + " TEXT,"
                + COLUMN_SALDO + " INTEGER"
                + ")";
        db.execSQL(CREATE_PLAYER_TABLE);

        // Crear la tabla "partida" para el historial
        String CREATE_PARTIDA_TABLE = "CREATE TABLE " + TABLE_PARTIDA + "("
                + COLUMN_PARTIDA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_JUGADOR_ID + " TEXT,"
                + COLUMN_GANANCIAS + " INTEGER,"
                + "FechadePartida TEXT,"
                + "UbicacionJugador TEXT,"
                + "FOREIGN KEY (" + COLUMN_JUGADOR_ID + ") REFERENCES " + TABLE_PLAYER + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_PARTIDA_TABLE);

        // Crear la tabla "ruta" para asociar rutas de imagen con partidas
        String CREATE_RUTA_TABLE = "CREATE TABLE " + TABLE_RUTA + "("
                + COLUMN_PARTIDA_IDR + " INTEGER,"
                + COLUMN_RUTA_ID + " TEXT NOT NULL,"
                + "FOREIGN KEY (" + COLUMN_PARTIDA_IDR + ") REFERENCES " + TABLE_PARTIDA + "(" + COLUMN_PARTIDA_ID + ")"
                + ")";
        db.execSQL(CREATE_RUTA_TABLE);

    }

    // Resto de métodos como insertar jugador, verificar jugador existente, obtener datos del jugador, etc.
    // Necesitarán modificaciones para adaptarse al nuevo esquema de la base de datos.
    // Por ejemplo, al insertar una partida, deberás insertar en la tabla "partida" con el ID del jugador correspondiente.

    public void insertPartida(int idJugador, int ganancias, String fechaPartida, String ubicacion) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_JUGADOR_ID, idJugador);
        values.put(COLUMN_GANANCIAS, ganancias);
        values.put("FechadePartida", fechaPartida); // Nueva columna FechadePartida
        values.put("UbicacionJugador", ubicacion); // Nueva columna UbicacionJugador

        long resultado = db.insert(TABLE_PARTIDA, null, values);
        db.close();
    }

    public void insertarRuta(String idRuta, int idPartida) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PARTIDA_IDR, idPartida);
        values.put(COLUMN_RUTA_ID, idRuta);

        long resultado = db.insert(TABLE_RUTA, null, values);
        db.close();
    }

    public int obtenerUltimoIdPartida() {
        SQLiteDatabase db = this.getReadableDatabase();
        int ultimoIdPartida = -1;

        String query = "SELECT MAX(" + COLUMN_PARTIDA_ID + ") AS UltimoId FROM " + TABLE_PARTIDA;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex("UltimoId");
                if (columnIndex != -1) {
                    ultimoIdPartida = cursor.getInt(columnIndex);
                }
            }
            cursor.close();
        }

        return ultimoIdPartida;
    }

    public int obtenerUltimoIdPartidaconRuta(String fechaSeleccionada) {
        SQLiteDatabase db = this.getReadableDatabase();
        int ultimoIdPartida = -1;

        String query = "SELECT DISTINCT p." + COLUMN_PARTIDA_ID + " FROM " + TABLE_PARTIDA + " p " +
                "INNER JOIN " + TABLE_RUTA + " r ON p." + COLUMN_PARTIDA_ID + " = r." + COLUMN_PARTIDA_IDR +
                " WHERE FechadePartida = ?"; // Agregar la comparación de fechas aquí

        String[] selectionArgs = {fechaSeleccionada}; // Pasar la fecha seleccionada como argumento

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null) {
            if (cursor.moveToLast()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_PARTIDA_ID);
                if (columnIndex != -1) {
                    ultimoIdPartida = cursor.getInt(columnIndex);
                }
            }
            cursor.close();
        }

        return ultimoIdPartida;
    }



    public String obtenerRutaImagenPorIdPartida(int idPartida) {
        SQLiteDatabase db = this.getReadableDatabase();
        String rutaImagen = "";

        String query = "SELECT " + COLUMN_RUTA_ID + " FROM " + TABLE_RUTA + " WHERE " + COLUMN_PARTIDA_IDR + " = ?";
        String[] selectionArgs = {String.valueOf(idPartida)};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndex(COLUMN_RUTA_ID);
                if (columnIndex != -1) {
                    rutaImagen = cursor.getString(columnIndex);
                }
            }
            cursor.close();
        }

        return rutaImagen;
    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades as needed
    }

    public void insertJugador(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();
        String idJugador = String.valueOf(player.getId()); // Convierte el ID a una cadena

        String query = "INSERT INTO " + TABLE_PLAYER + " (" + COLUMN_ID + ", " + COLUMN_EMAIL + ", " +
                COLUMN_SALDO + ", " + COLUMN_PASSWORD + ") VALUES ('" +
                idJugador + "', '" + player.getEmail() + "', " + player.getSaldo() + ", '" +
                player.getPassword() + "')";

        db.execSQL(query);

        //db.close();
    }



    public boolean existeJugador(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PLAYER + " WHERE " +
                COLUMN_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.rawQuery(query, selectionArgs);
        boolean jugadorExiste = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return jugadorExiste;
    }

    public Player obtenerDatosJugador(String email, String password) {
        Player player = null;
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_ID,
                COLUMN_EMAIL,
                COLUMN_SALDO
        };

        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(
                TABLE_PLAYER,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            String userPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            String saldo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SALDO));

            // Como el historial de partidas ahora está en una tabla separada, no se recupera aquí

            player = new Player(id, userEmail, userPassword, Integer.parseInt(saldo)); // No se recupera el historial en esta función
        }

        cursor.close();
        return player;
    }

    public void updatePlayer(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SALDO, player.getSaldo());

        // No se actualiza el historial de partidas en esta función

        db.update(TABLE_PLAYER, values, COLUMN_ID + " = ?", new String[]{String.valueOf(player.getId())});
    }

    public void updateHistorial(String idJugador, String ganancias, String fechaPartida, String ubicacion) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_JUGADOR_ID, idJugador);
        values.put(COLUMN_GANANCIAS, ganancias);
        values.put("FechadePartida", fechaPartida); // Reemplaza "FechadePartida" por el nombre de tu columna
        values.put("UbicacionJugador", ubicacion); // Reemplaza "UbicacionJugador" por el nombre de tu columna

        long resultado = db.insert(TABLE_PARTIDA, null, values);
        db.close();
    }

    public int obtenerGananciaMasAltaPorIdJugador(String idJugador) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_GANANCIAS
        };

        String selection = COLUMN_JUGADOR_ID + " = ?";
        String[] selectionArgs = {idJugador};

        Cursor cursor = db.query(
                TABLE_PARTIDA,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_GANANCIAS + " DESC", // Ordenar por ganancias en orden descendente
                "1" // Limitar el resultado a 1 fila para obtener la ganancia más alta
        );

        int gananciaMasAlta = 0;

        if (cursor.moveToFirst()) {
            gananciaMasAlta = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GANANCIAS));
        }

        cursor.close();
        return gananciaMasAlta;
    }


    // Actualizar la función de obtener partidas por ID del jugador para incluir FechadePartida y UbicacionJugador

    public ArrayList<String> obtenerPartidasPorIdJugador(String idJugador) {
        ArrayList<String> partidasJugador = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_PARTIDA_ID,
                COLUMN_JUGADOR_ID,
                COLUMN_GANANCIAS,
                "FechadePartida", // Nueva columna FechadePartida
                "UbicacionJugador" // Nueva columna UbicacionJugador
        };

        String selection = COLUMN_JUGADOR_ID + " = ?";
        String[] selectionArgs = {idJugador};

        Cursor cursor = db.query(
                TABLE_PARTIDA,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            String partidaId = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PARTIDA_ID)));
            String jugadorId = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUGADOR_ID)));
            String ganancias = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GANANCIAS)));
            String fechaPartida = cursor.getString(cursor.getColumnIndexOrThrow("FechadePartida"));
            String ubicacion = cursor.getString(cursor.getColumnIndexOrThrow("UbicacionJugador"));

            partidasJugador.add("En la partida ID: " + partidaId + ", el Jugador ID " + jugadorId +
                    " ha ganado " + ganancias + " en la fecha " + fechaPartida + " en " + ubicacion);
        }

        cursor.close();
        return partidasJugador;
    }

    public Observable<Object> obtenerPartidasPorIdJugadorRx(String idJugador) {
        return Observable.create(emitter -> {
            ArrayList<String> partidasJugador = new ArrayList<>();
            SQLiteDatabase db = this.getReadableDatabase();

            String[] projection = {
                    COLUMN_PARTIDA_ID,
                    COLUMN_JUGADOR_ID,
                    COLUMN_GANANCIAS,
                    //"FechadePartida", // Nueva columna FechadePartida
                    //  "UbicacionJugador" // Nueva columna UbicacionJugador
            };

            String selection = COLUMN_JUGADOR_ID + " = ?";
            String[] selectionArgs = {idJugador};

            Cursor cursor = db.query(
                    TABLE_PARTIDA,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            while (cursor.moveToNext()) {
                String partidaId = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PARTIDA_ID)));
                String jugadorId = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUGADOR_ID)));
                String ganancias = String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GANANCIAS)));
                String fechaPartida = cursor.getString(cursor.getColumnIndexOrThrow("FechadePartida"));
                String ubicacion = cursor.getString(cursor.getColumnIndexOrThrow("UbicacionJugador"));

                partidasJugador.add("En la partida ID: " + partidaId + ", el Jugador ID " + jugadorId +
                        " ha ganado " + ganancias + " en la fecha " + fechaPartida + " en " + ubicacion);
            }

            cursor.close();

            emitter.onNext(partidasJugador);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io());
    }



    //Carga de datos

    // Función para cargar datos de jugadores y partidas
    /*public void cargarDatos() {
        // Insertar jugadores
        insertJugador(new Player("1", "jugador1@email.com", "password1", 1000)); // Reemplaza con los valores deseados
        insertJugador(new Player("2", "jugador2@email.com", "password2", 1500)); // Reemplaza con los valores deseados

        // Insertar partidas asociadas a los jugadores
        insertPartida(1, 500); // Aquí asumo que el jugador 1 tiene la ID 1
        insertPartida(1, 700); // Estas son partidas del jugador 1
        insertPartida(2, 800); // Aquí asumo que el jugador 2 tiene la ID 2
        insertPartida(2, 1200); // Estas son partidas del jugador 2
    }*/


    // Obtener los datos de un jugador por su ID
    public Player obtenerJugadorPorId(String playerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Player player = null;

        String[] projection = {
                COLUMN_ID,
                COLUMN_EMAIL,
                COLUMN_PASSWORD,
                COLUMN_SALDO
        };

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = { playerId };

        Cursor cursor = db.query(
                TABLE_PLAYER,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            String userPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            String saldo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SALDO));

            player = new Player(id, userEmail, userPassword, Integer.parseInt(saldo));
        }

        cursor.close();
        return player;
    }


    // Obtener un jugador por su email
    public Player obtenerJugadorPorEmail(String emailJugador) {
        SQLiteDatabase db = this.getReadableDatabase();
        Player jugador = null;

        String[] projection = {
                COLUMN_ID,
                COLUMN_EMAIL,
                COLUMN_PASSWORD,
                COLUMN_SALDO
        };

        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = { emailJugador };

        Cursor cursor = db.query(
                TABLE_PLAYER,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
            String userEmail = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
            String userPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            String saldo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SALDO));

            jugador = new Player(id, userEmail, userPassword, Integer.parseInt(saldo));
        }

        cursor.close();
        return jugador;
    }


    //PRODUCTO 2

    public List<String> obtenerPartidasConVictoriasPorFechaYUbicacion(String emailJugador, String fecha) {
        List<String> partidasConVictoriasYUbicacion = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT FechadePartida, UbicacionJugador FROM " + TABLE_PARTIDA +
                " WHERE " + COLUMN_JUGADOR_ID + " = (SELECT " + COLUMN_ID + " FROM " + TABLE_PLAYER + " WHERE " + COLUMN_EMAIL + " = ?)" +
                " AND FechadePartida = ? AND " + COLUMN_GANANCIAS + " > 0";
        String[] selectionArgs = { emailJugador, fecha };

        Cursor cursor = db.rawQuery(query, selectionArgs);

        while (cursor.moveToNext()) {
            String fechaPartida = cursor.getString(cursor.getColumnIndexOrThrow("FechadePartida"));
            String ubicacionJugador = cursor.getString(cursor.getColumnIndexOrThrow("UbicacionJugador"));

            String partidaInfo = "Fecha: " + fechaPartida + ", Ubicación: " + ubicacionJugador;
            partidasConVictoriasYUbicacion.add(partidaInfo);
        }

        cursor.close();
        return partidasConVictoriasYUbicacion;
    }
        public void actualizarBoteComun(int dineroPerdido) {
            DatabaseReference boteReference = FirebaseDatabase.getInstance("https://rollsix-c5f13-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Bote");

            // Actualiza el bote común sumando el dinero perdido
            boteReference.runTransaction(new Transaction.Handler() {
                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                    Integer boteActual = mutableData.getValue(Integer.class);
                    if (boteActual == null) {
                        boteActual = 0;
                    }

                    // Actualiza el bote sumando el dinero perdido
                    boteActual = boteActual + dineroPerdido;

                    // Guarda el nuevo valor del bote
                    mutableData.setValue(boteActual);

                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot currentData) {
                    if (databaseError != null) {
                        // Maneja errores, si es necesario
                    } else {
                        // Operación completada con éxito
                    }
                }
            });
        }

    public int obtenerCantidadBoteComun() {
        DatabaseReference boteReference = FirebaseDatabase.getInstance("https://rollsix-c5f13-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Bote");

        final int[] cantidadBote = {0}; // Variable para almacenar la cantidad del bote

        // Lee la cantidad actual del bote común
        boteReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer boteActual = dataSnapshot.getValue(Integer.class);
                if (boteActual != null) {
                    // Almacena la cantidad del bote en la variable
                    cantidadBote[0] = boteActual;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Maneja errores, si es necesario
            }
        });

        // Retorna la cantidad del bote
        return cantidadBote[0];
    }


        private void entregarBoteAlGanador(int cantidadBote) {
            // Lógica para entregar el bote al jugador ganador, por ejemplo, actualiza su saldo
            // ...

            // Vacía el bote después de entregarlo al ganador
            vaciarBoteComun();
        }

        public void vaciarBoteComun() {
            DatabaseReference boteReference = FirebaseDatabase.getInstance("https://rollsix-c5f13-default-rtdb.europe-west1.firebasedatabase.app/").getReference("Bote");

            // Restablece el valor del bote a 0 en Firebase
            boteReference.setValue(0);
        }
}