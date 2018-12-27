package com.vts.samsung.labaccesscontrol.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vts.samsung.labaccesscontrol.Enums.StudentLocationRadius;
import com.vts.samsung.labaccesscontrol.Interfaces.WiFiSignalStrengthListener;
import com.vts.samsung.labaccesscontrol.Utils.Application;
import com.vts.samsung.labaccesscontrol.Utils.ConnectivityReceiver;
import com.vts.samsung.labaccesscontrol.Utils.ConnectivityReceiver.ConnectionType;

import org.json.JSONException;
import org.json.JSONObject;


public class checkInLab_Service extends Service implements ConnectivityReceiver.ConnectivityReceiverListener, WiFiSignalStrengthListener {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private SharedPreferences sharedPreferences;

    private String checkInStatus;
    private String uniqueToken;


    private String TAG = "SERVIS2";

    private BroadcastReceiver connectivityReceiver = new ConnectivityReceiver();
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Servis pokrenut");

        sharedPreferences = Application.getInstance().getSharedPreferences("appSettings", MODE_PRIVATE);
        checkInStatus = sharedPreferences.getString( "checkInStatus", "outsideLab");
        uniqueToken = sharedPreferences.getString("uniqueToken", "");
        //todo SharedPreferences ne moze da pristupi podesavanjima aplikacije

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(connectivityReceiver, filter);
        Application.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(connectivityReceiver);
    }

    @Override
    public void onNetworkConnectionChanged(ConnectionType connectionType, boolean onSamsungAppsLab) {
        Log.d(TAG, "Promenjena konekcija");
        checkInStatus = sharedPreferences.getString("checkInStatus", "outsideLab");
        if(onSamsungAppsLab) {
            Log.d(TAG, "U laboratoriji ste");
        } else {
           Log.d(TAG,"Niste u laboratoriji");
           Log.d(TAG, "Status je: "+checkInStatus);
            //if(checkInStatus.equals("inLab")) {
                sendLogOutSignal();
                //push notification
                //intent logout
           // }
        }
    }

    private void sendLogOutSignal() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        final JsonObjectRequest customRequest = new JsonObjectRequest(
                Request.Method.POST,
                Application.getInstance().getRpiRouteLogOut(),
                getPostRequestBody(),
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Uspesno primljen logout zahtev");
                String status = "";
                try {
                    status = response.getString("chackRequest");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(status.equals("chackOUT")) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("statusSTUD", "outsideLab");
                    editor.apply();
                    Log.d(TAG, "Uspesno odjavljen sa uredjaja");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "Greska na serveru pri slanju logout zahteva");
                NetworkResponse response = error.networkResponse;
                Log.d(TAG,"greka je "+response.statusCode);
            }
        });
        queue.add(customRequest);
    }

    private JSONObject getPostRequestBody() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("token", uniqueToken);
            jsonObject.put("deviceMac", Application.getInstance().getDeviceMac());
            jsonObject.put("action", "chackOUT");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public void onWifiStrengthChanged(StudentLocationRadius isNearLab) {

    }
}
