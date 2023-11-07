package objetivoapp.rollsix;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
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
                + "FOREIGN KEY (" + COLUMN_JUGADOR_ID + ") REFERENCES " + TABLE_PLAYER + "(" + COLUMN_ID + ")"
                + ")";
        db.execSQL(CREATE_PARTIDA_TABLE);
    }

    // Resto de métodos como insertar jugador, verificar jugador existente, obtener datos del jugador, etc.
    // Necesitarán modificaciones para adaptarse al nuevo esquema de la base de datos.
    // Por ejemplo, al insertar una partida, deberás insertar en la tabla "partida" con el ID del jugador correspondiente.

    // Insertar una nueva partida en la tabla "partida"
    public void insertPartida(int idJugador, int ganancias) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_JUGADOR_ID, idJugador);
        values.put(COLUMN_GANANCIAS, ganancias);

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

    public void updateHistorial(int idJugador, int ganancias) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_JUGADOR_ID, idJugador);
        values.put(COLUMN_GANANCIAS, ganancias);

        long resultado = db.insert(TABLE_PARTIDA, null, values);
        db.close();
    }

    public ArrayList<Partida> obtenerHistorialCompleto() {
        ArrayList<Partida> historialPartidas = new ArrayList<Partida>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COLUMN_PARTIDA_ID,
                COLUMN_JUGADOR_ID,
                COLUMN_GANANCIAS
        };

        Cursor cursor = db.query(
                TABLE_PARTIDA,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        while (cursor.moveToNext()) {
            int partidaId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PARTIDA_ID));
            int jugadorId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_JUGADOR_ID));
            int ganancias = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_GANANCIAS));

            Partida partida = new Partida(partidaId, jugadorId, ganancias);
            historialPartidas.add(partida);
        }

        cursor.close();
        return historialPartidas;
    }




    //RXJAVA


    // Insertar una nueva partida en la tabla "partida" de manera asíncrona usando RXJava
    public Completable insertPartidaAsync(int idJugador, int ganancias) {
        return Completable.fromAction(() -> insertPartida(idJugador, ganancias))
                .subscribeOn(Schedulers.io());
    }

    // Insertar un jugador de manera asíncrona usando RXJava
    public Completable insertJugadorAsync(Player player) {
        return Completable.fromAction(() -> insertJugador(player))
                .subscribeOn(Schedulers.io());
    }

    // Verificar si un jugador existe de manera asíncrona usando RXJava
    public Observable<Boolean> existeJugadorAsync(String email, String password) {
        return Observable.fromCallable(() -> existeJugador(email, password))
                .subscribeOn(Schedulers.io());
    }

    // Obtener datos del jugador de manera asíncrona usando RXJava
    public Observable<Player> obtenerDatosJugadorAsync(String email, String password) {
        return Observable.fromCallable(() -> obtenerDatosJugador(email, password))
                .subscribeOn(Schedulers.io());
    }

    // Actualizar datos de un jugador de manera asíncrona usando RXJava
    public Completable updatePlayerAsync(Player player) {
        return Completable.fromAction(() -> updatePlayer(player))
                .subscribeOn(Schedulers.io());
    }

    // Obtener el historial de partidas de manera asíncrona usando RXJava
    public Observable<ArrayList<Partida>> obtenerHistorialCompletoAsync() {
        return Observable.fromCallable(this::obtenerHistorialCompleto)
                .subscribeOn(Schedulers.io());
    }
}
