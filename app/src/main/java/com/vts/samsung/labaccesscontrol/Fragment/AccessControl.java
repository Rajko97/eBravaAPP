package com.vts.samsung.labaccesscontrol.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.vts.samsung.labaccesscontrol.Activity.MainActivity;
import com.vts.samsung.labaccesscontrol.R;
import com.vts.samsung.labaccesscontrol.Utils.Application;
import com.vts.samsung.labaccesscontrol.Utils.ConnectivityReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class AccessControl extends Fragment implements  ConnectivityReceiver.ConnectivityReceiverListener {


    public AccessControl() {
        // Required empty public constructor
    }

    private View v;
    private MainActivity mainActivity;
    private Application application;
    private Socket RPi;
    private ImageButton btnUnlock;
    private SharedPreferences sharedPreferences;
    private ImageButton btnCheckIn, btnCheckOut;

    private BroadcastReceiver connectivityReceiver = new ConnectivityReceiver();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_access_control, container, false);

        mainActivity = (MainActivity) getActivity();

        sharedPreferences =mainActivity.getSharedPreferences("аppSettings", Context.MODE_PRIVATE);

        application = (Application) mainActivity.getApplication();
        {
            IO.Options opts = new IO.Options();
            opts.reconnection = true;
            opts.reconnectionDelay = 500;
            opts.timeout = 3000;

            try {
                RPi = IO.socket(application.getRPi(), opts);
            } catch (URISyntaxException e) {
                Log.e("Socket.IO", String.valueOf(e));
            }

        }
        RPi.connect();

        btnUnlock = (ImageButton) v.findViewById(R.id.btnUnlock);
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RPi.emit("unlock", getMessageForRpi("unlock").toString());
            }
        });


        btnCheckIn = (ImageButton) v.findViewById(R.id.btnCheckIn);
        btnCheckOut = (ImageButton) v.findViewById(R.id.btnCheckOut);
        onCheckInOutListener();

        initClock(v);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        getActivity().registerReceiver(connectivityReceiver, filter);

        return v;
    }

    private void initClock(View v) {
        Typeface font = Typeface.createFromAsset(getActivity().getAssets(), "exo.ttf");
        ((TextView) v.findViewById(R.id.tvAccessControlClock)).setTypeface(font);
        ((TextView) v.findViewById(R.id.tvAccessControlDate)).setTypeface(font);
    }

    private void onCheckInOutListener() {
        String checkInStatus = sharedPreferences.getString("checkInStatus", "outsideLab");
        if (checkInStatus.equals("inLab")) {
            btnCheckIn.setEnabled(false);
        } else {
            btnCheckOut.setEnabled(false);
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String checkInStatus = sharedPreferences.getString("checkInStatus", "outsideLab");
                if (checkInStatus.equals("inLab")) { //ako je u lab, moze da klikne samo na CheckOut tako da mu CheckOut disabluje a enabluje drugi
                    checkInStatus = "outsideLab";
                    btnCheckIn.setEnabled(true);
                    btnCheckOut.setEnabled(false);
                    RPi.emit("check", getMessageForRpi("checkOUT").toString());
                } else {
                    checkInStatus = "inLab";
                    btnCheckIn.setEnabled(false);
                    btnCheckOut.setEnabled(true);
                    RPi.emit("check", getMessageForRpi("checkIN").toString());
                }
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("checkInStatus", checkInStatus).apply();
            }
        };
        btnCheckIn.setOnClickListener(onClickListener);
        btnCheckOut.setOnClickListener(onClickListener);
    }

    public JSONObject getMessageForRpi(String a) {
        JSONObject jsonData = new JSONObject();
        String uniqueToken = sharedPreferences.getString("uniqueToken", null);
        try {
            jsonData.put("uniqueToken", uniqueToken);
            jsonData.put("macAddressDevice", application.getDeviceMac());
            jsonData.put("IPDevice", application.getIPDevice());
            jsonData.put("action", a);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonData;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ConnectivityReceiver.isConnected()) {
            RPi.connect();
            /*
            RPi.on("error", onError);
            RPi.on("unlocked", onNewMessageUnlocked);
            RPi.on("checkRequest", onNewMessageCheck);

            if (sharedPreferences.getBoolean("primljenaPoruka", false)) {
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.putBoolean("primljenaPoruka", true).apply();
                onNavigationItemSelected(navigationView.getMenu().getItem(3));
            } else {
                onNavigationItemSelected(navigationView.getMenu().getItem(0));
            }*/
        } else {
            ConnectivityReceiver.alertDialogNet(mainActivity);
        }
        Application.getInstance().setConnectivityListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        RPi.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(connectivityReceiver);
    }

    @Override
    public void onNetworkConnectionChanged(ConnectivityReceiver.ConnectionType connectionType, boolean onSamsungAppsLab) {
        if(onSamsungAppsLab) {
            btnUnlock.setEnabled(true);
        } else {
            btnUnlock.setEnabled(false);
        }
    }
}
