package objetivoapp.rollsix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear la tabla "jugador"
        String CREATE_PLAYER_TABLE = "CREATE TABLE " + TABLE_PLAYER + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_EMAIL + " TEXT NOT NULL,"
                + COLUMN_PASSWORD + " TEXT NOT NULL,"
                + COLUMN_SALDO + " INTEGER NOT NULL"
                + ")";
        db.execSQL(CREATE_PLAYER_TABLE);

        // Crear la tabla "partida" para el historial
        String CREATE_PARTIDA_TABLE = "CREATE TABLE " + TABLE_PARTIDA + "("
                + COLUMN_PARTIDA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_JUGADOR_ID + " INTEGER,"
                + COLUMN_GANANCIAS + " INTEGER,"
                + "FechadePartida TEXT,"
                + "UbicacionJugador TEXT,"
                + "FOREIGN KEY (" + COLUMN_JUGADOR_ID + ") REFERENCES " + TABLE_PLAYER + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_PARTIDA_TABLE);
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



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades as needed
    }

    public void insertJugador(Player player) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, player.getId());
        values.put(COLUMN_EMAIL, player.getEmail());
        values.put(COLUMN_PASSWORD, player.getPassword());
        values.put(COLUMN_SALDO, player.getSaldo());


        long resultado = db.insert(TABLE_PLAYER, null, values);

        //db.close();

    }

    public boolean existeJugador(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_PLAYER + " WHERE " +
                COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

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





}
