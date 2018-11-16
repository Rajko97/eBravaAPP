package com.vts.samsung.labaccesscontrol.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import com.vts.samsung.labaccesscontrol.R;
import com.vts.samsung.labaccesscontrol.Utils.Application;
import com.vts.samsung.labaccesscontrol.Utils.CheckNetwork;


import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private Button button, button2;
    private SharedPreferences sharedPreferences;
    private Application application;
    private Socket RPi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences("аppSettings", Context.MODE_PRIVATE);
        application = (Application) getApplication();
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

        button = (Button) findViewById(R.id.btnUnlock);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RPi.emit("unlock", getMessageForRpi("unlock").toString());
            }
        });

        button2 = (Button) findViewById(R.id.btnLogOut);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor usereditor = sharedPreferences.edit();
                usereditor.clear().apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        });
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
    protected void onResume() {
        super.onResume();
        if (CheckNetwork.checkNet(this.getApplicationContext(), application)) {
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
            CheckNetwork.alertDialogNet(this, OvoDaSePrepravi);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        RPi.disconnect();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }
}
