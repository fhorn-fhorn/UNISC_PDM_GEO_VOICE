package com.m35725.unisc_pdm_geo_voice;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;

public class ListarAudiosMapaActivity extends AppCompatActivity {

    //GOOGLE API_KEY= AIzaSyBKtV8B2CHi6uhjIHa0ZA6UdQ9M3CWWBKU
    //https://developers.google.com/maps/documentation/maps-static/dev-guide

    private String urlBase= "https://maps.googleapis.com/maps/api/staticmap?zoom=17" +
            "&key=AIzaSyBKtV8B2CHi6uhjIHa0ZA6UdQ9M3CWWBKU" +
            "&size=400x400" +
            "&markers=color:blue|";
    private String urlBaseNoSize= "https://maps.googleapis.com/maps/api/staticmap?zoom=17" +
            "&key=AIzaSyBKtV8B2CHi6uhjIHa0ZA6UdQ9M3CWWBKU" +
            "&markers=color:blue|";

    private DatabaseHelper  helper= null;

    private String latitudeMapa;
    private String longitudeMapa;

    private WebView mapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listar_audios_mapa);

        Bundle extras= getIntent().getExtras();
        String idAudio= extras.getString("id");

        //Banco de Dados
        helper= new DatabaseHelper(this);
        String query= "SELECT * FROM audio WHERE id=" + idAudio;
        SQLiteDatabase db= helper.getReadableDatabase();
        Cursor cursor= db.rawQuery(query, null);
        if( cursor.moveToFirst() ) {
            String nomeAudio = cursor.getString(DatabaseHelper.DB_COLUMN_NOME_AUDIO);
            Double latitude = cursor.getDouble(DatabaseHelper.DB_COLUMN_LATITUDE);
            Double longitude = cursor.getDouble(DatabaseHelper.DB_COLUMN_LONGITUDE);
            String dtHr = cursor.getString(DatabaseHelper.DB_COLUMN_DATA_HORA);

            //Nome do audio
            TextView txtNomeAudio= (TextView) findViewById(R.id.txtViewAudioNoMapaNomeAudio);
            txtNomeAudio.setText(nomeAudio);

            //Latitude
            DecimalFormat df = new DecimalFormat("#.###");
            latitudeMapa= df.format(latitude);
            TextView txtLatitude= (TextView) findViewById(R.id.txtViewAudioNoMapaLatitude);
            txtLatitude.setText("Lat: " + latitudeMapa);
            latitudeMapa= latitudeMapa.replace(",", ".");

            //Longitude
            longitudeMapa= df.format(longitude);
            TextView txtLongitude= (TextView) findViewById(R.id.txtViewAudioNoMapaLongitude);
            txtLongitude.setText("Lon: " + longitudeMapa);
            longitudeMapa= longitudeMapa.replace(",", ".");

            //Data Hora
            TextView txtDataHora= (TextView) findViewById(R.id.txtViewAudioNoMapaDataHora);
            txtDataHora.setText(dtHr);

            //WebView
            StringBuilder urlMapa= new StringBuilder();
            urlMapa.append(urlBaseNoSize);
            urlMapa.append(latitudeMapa);
            urlMapa.append(",");
            urlMapa.append(longitudeMapa);
            urlMapa.append("&size=");
            urlMapa.append("400");
            urlMapa.append("x");
            urlMapa.append("400");
            mapa= (WebView) findViewById(R.id.webViewMapaAudios);
            mapa.loadUrl(urlMapa.toString());
            mapa.setBackgroundColor(Color.TRANSPARENT);

        }else {
            Toast.makeText(this, "Audio não encontrado: " + idAudio,
                    Toast.LENGTH_SHORT).show();
        }
         cursor.close();

    }
}
