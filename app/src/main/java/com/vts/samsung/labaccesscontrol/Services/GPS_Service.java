package com.vts.samsung.labaccesscontrol.Services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;

public class GPS_Service extends Service {

    private boolean currentyInLab = false;

    private LocationListener listener;
    private LocationManager locationManager;

    private double labLocationLongitude = 21.88929428346455;
    private double labLocationLatitude =  43.32994246389717;

    private double longitudeOffset = Math.abs(labLocationLongitude-21.88926142640412);
    private double latitudeOffset =  Math.abs(labLocationLatitude-43.32994929514825);

    private Location appsLabLocation = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {


               if(distanceToLabLocationInMeters(location) < 250) {
                    Intent i = new Intent("location_update");
                    i.putExtra("coordinates", "Usli ste u laboratoriju\n" +
                            "Razlika je: "+distanceToLabLocationInMeters(location)+"m");
                    sendBroadcast(i);
                }
                else {
                    Intent i = new Intent("location_update");
                    i.putExtra("coordinates", "Napustili ste laboratoriju\n" +
                            "Razlika je: "+distanceToLabLocationInMeters(location)+"m");
                    sendBroadcast(i);
                }
                /*Location.distanceBetween(labLocationLatitude, labLocationLongitude, location.getLatitude(), location.getLongitude(), dist);
                if (dist[0] > longitudeOffset) { //inLab
                    // if(!currentyInLab)
                    currentyInLab = true;
                    Intent i = new Intent("location_update");
                    i.putExtra("coordinates", "Usli ste u laboratoriju");
                    sendBroadcast(i);
                    //}
                }
                else { //OutLab
                   // if(currentyInLab) {
                        currentyInLab = false;
                        Intent i = new Intent("location_update");
                        i.putExtra("coordinates", "Napustili ste laboratoriju");
                        sendBroadcast(i);
                   // }
                }*/
            }

            private double distanceToLabLocationInMeters(Location location) {
                double earthRadius = 6371; //1 km

                double dLat = Math.toRadians(location.getLatitude()-labLocationLatitude);
                double dLng = Math.toRadians(location.getLongitude()-labLocationLongitude);

                double sindLat = Math.sin(dLat/2);
                double sindLng = Math.sin(dLng/2);

                double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                        * Math.cos(Math.toRadians(labLocationLatitude)) * Math.cos(Math.toRadians(labLocationLongitude));

                double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
                double dist = earthRadius * c;
                return dist * 1000;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, listener);

        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null) {
            locationManager.removeUpdates(listener);
        }
    }
}
