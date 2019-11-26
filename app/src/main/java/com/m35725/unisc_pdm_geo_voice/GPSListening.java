package com.m35725.unisc_pdm_geo_voice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import static android.content.Context.LOCATION_SERVICE;

public class GPSListening implements LocationListener {

    TextView txtViewLatitudeValor;
    TextView txtViewLongitudeValor;

    @Override
    public void onLocationChanged(Location location) {

        String lat = String.valueOf(location.getLatitude());
        txtViewLatitudeValor.setText(lat);

        String lon = String.valueOf(location.getLongitude());
        txtViewLongitudeValor.setText(lon);

        String alt = String.valueOf(location.getAltitude());
        String prov = String.valueOf(location.getProvider());
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

}
