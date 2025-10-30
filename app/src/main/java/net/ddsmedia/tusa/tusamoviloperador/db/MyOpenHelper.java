package net.ddsmedia.tusa.tusamoviloperador.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class MyOpenHelper extends SQLiteOpenHelper {

    private static final String CREAR_TABLA_BITACORA="CREATE TABLE bitacora(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "desde INTEGER UNIQUE NOT NULL, " +
            "hasta INTEGER UNIQUE NOT NULL, " +
            "actividad TEXT NOT NULL)";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NOMBRE = "base.sqlite";

    public MyOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NOMBRE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREAR_TABLA_BITACORA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int old_version, int new_version) {
        db.execSQL("DROP TABLE IF EXISTS usuarios");
        onCreate(db);
    }
}

