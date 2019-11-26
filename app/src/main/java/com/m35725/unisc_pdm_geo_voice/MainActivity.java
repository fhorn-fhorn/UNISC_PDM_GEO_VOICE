package com.m35725.unisc_pdm_geo_voice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO= 1;
    public final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE= 2;

    private LocationManager locManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void btnGravarNovoAudioOnClick(View view) {

        //Se ainda nao foi criado
        if( locManager == null ) {
            locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }

        //Verifica se tem permissao ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "ACCESS_FINE_LOCATION: Sem Permissão",
            //        Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return;
        }

        //Verifica se tem permissao ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "ACCESS_COARSE_LOCATION: Sem Permissão",
            //        Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return;
        }

        //Se esta habilitado GPS
        if( locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            Intent intent= new Intent(MainActivity.this, GravarAudioActivity.class);
            startActivity(intent);
        }else {
            //Mensagem
            Toast.makeText(this, "GPS NÃO ESTÁ LIGADO!", Toast.LENGTH_SHORT).show();
        }

    }

    public void btnListarAudiosGravadosOnClick(View view) {
        Intent intent= new Intent(MainActivity.this, ListarAudiosActivity.class);
        intent.putExtra("acao", "player");
        startActivity(intent);
    }

    public void btnListarAudiosNoMapaOnClick(View view) {
        Intent intent= new Intent(MainActivity.this, ListarAudiosActivity.class);
        intent.putExtra("acao", "mapa");
        startActivity(intent);
        //Intent intent= new Intent(MainActivity.this, ListarAudiosMapaActivity.class);
        //startActivity(intent);
    }
}
