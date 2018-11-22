package com.vts.samsung.labaccesscontrol.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.vts.samsung.labaccesscontrol.Activity.MainActivity;
import com.vts.samsung.labaccesscontrol.R;
import com.vts.samsung.labaccesscontrol.Utils.Application;
import com.vts.samsung.labaccesscontrol.Utils.CheckNetwork;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class AccessControl extends Fragment {


    public AccessControl() {
        // Required empty public constructor
    }

    private View v;
    private MainActivity mainActivity;
    private Application application;
    private Socket RPi;
    private Button button;
    private SharedPreferences sharedPreferences;
    private Button btnCheckIn, btnCheckOut;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_access_control, container, false);
        // Inflate the layout for this fragment
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

        button = (Button) v.findViewById(R.id.btnUnlock);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RPi.emit("unlock", getMessageForRpi("unlock").toString());
            }
        });


        btnCheckIn = (Button) v.findViewById(R.id.btnCheckIn);
        btnCheckOut = (Button) v.findViewById(R.id.btnCheckOut);
        onCheckInOutListener();

        return v;
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
                } else {
                    checkInStatus = "inLab";
                    btnCheckIn.setEnabled(false);
                    btnCheckOut.setEnabled(true);

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
        if (CheckNetwork.checkNet(mainActivity.getApplicationContext(), application)) {
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
            boolean[] OvoDaSePrepravi = new boolean[2];
            CheckNetwork.alertDialogNet(mainActivity, OvoDaSePrepravi);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        RPi.disconnect();
    }
}
