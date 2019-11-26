package com.m35725.unisc_pdm_geo_voice;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String NOME_BANCO_DADOS = "GEO_VOICE_RECORDER";
    private static int VERSAO = 1;

    public static final int DB_COLUMN_ID = 0;
    public static final int DB_COLUMN_NOME_AUDIO = 1;
    public static final int DB_COLUMN_LATITUDE = 2;
    public static final int DB_COLUMN_LONGITUDE = 3;
    public static final int DB_COLUMN_ARQUIVO_AUDIO = 4;
    public static final int DB_COLUMN_DATA_HORA = 5;

    public static final String DB_TABLE_NAME_AUDIO = "audio";

    public DatabaseHelper(Context context){
        super(context, NOME_BANCO_DADOS, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE audio (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "nomeAudio TEXT," +
                "latitude DOUBLE," +
                "longitude DOUBLE," +
                "audioFile BLOB," +
                "dataHora TEXT" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS carro");
    }

}
