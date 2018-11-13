package com.vts.samsung.labaccesscontrol.Activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.vts.samsung.labaccesscontrol.R;
import com.vts.samsung.labaccesscontrol.Utils.Application;
import com.vts.samsung.labaccesscontrol.Utils.CheckNetwork;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class LoginActivity extends AppCompatActivity {

    private ImageButton btnSign;
    private EditText txtUser, txtPass;
    private Application application;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("podesavanja", Context.MODE_PRIVATE);
        Typeface typeface = Typeface.createFromAsset(getApplicationContext().getAssets(), "exo.ttf");

        btnSign = findViewById(R.id.imgBtnSingUp);
        txtUser = findViewById(R.id.txtUsername);
        txtPass = findViewById(R.id.txtPassword);

        TextView viewPrijava = findViewById(R.id.tvSignUp);
        viewPrijava.setTypeface(typeface);
        txtUser.setTypeface(typeface);
        txtPass.setTypeface(typeface);

        application = (Application) getApplication();
        CheckNet();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        CheckNet();
    }

    private void CheckNet() {
        if (CheckNetwork.checkNet(this.getApplicationContext(), application)) {
            btnSend();
        } else {
            CheckNetwork.alertDialogNet(this);
        }
    }

    private void btnSend() {
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSign.setImageResource(R.drawable.button_press);
                if (txtUser.getText().toString().length() > 0 && txtPass.getText().toString().length() > 0) {
                    sendLoginRequest();

                } else {
                    btnSign.setImageResource(R.drawable.button);
                    Toast.makeText(LoginActivity.this, (R.string.obavezna_polja), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendLoginRequest() {
        final ProgressDialog progressDialog;
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setProgressStyle(R.style.ProgressBar);
        progressDialog.setMessage(getResources().getString(R.string.dialogAuthorizing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        JSONObject loginInfo = new JSONObject();
        try {
            loginInfo.put("macAddressDevice", application.getDeviceMac());
            loginInfo.put("username", txtUser.getText().toString());
            loginInfo.put("password", txtPass.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        final JsonObjectRequest customRequest = new JsonObjectRequest(Request.Method.POST, application.getRPi() + application.getRpiRouteLogin(),loginInfo, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String message = response.getString("message");
                    switch (message) {
                        case "success":
                            Intent intent;
                            boolean doorPermission = response.getBoolean("doorPermission");
                            SharedPreferences.Editor usereditor = sharedPreferences.edit();
                            usereditor.putString("username", response.getString("username"));
                            usereditor.putString("ime", response.getString("name"));
                            usereditor.putString("prezime", response.getString("lastName"));
                            usereditor.putString("zvanje", response.getString("rank"));
                            usereditor.putString("macAdressaRuterInLab", response.getString("samsungAppsLabRouterMacAddress"));
                            usereditor.putString("JedinstveniToken", response.getString("uniqueToken"));

                            if (doorPermission) {
                                usereditor.putString("AccessLab", "true").apply();
                                //intent = new Intent(Login.this, NavActivity.class);
                                intent = new Intent(LoginActivity.this, MainActivity.class);
                            } else {
                                usereditor.putString("AccessLab", "false").apply();
                                //intent = new Intent(Login.this, WaitActiveActivity.class);
                                intent = new Intent(LoginActivity.this, MainActivity.class);
                            }
                            usereditor.apply();
                            progressDialog.dismiss();
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                            break;
                        case "notValidMacAddress":
                            Toast.makeText(LoginActivity.this, R.string.neovlascen_uredjaj, Toast.LENGTH_SHORT).show();
                            break;
                        case "notValidPassword":
                            Toast.makeText(LoginActivity.this, R.string.neispravno_lozinka, Toast.LENGTH_SHORT).show();
                            break;
                        case "notValidUsername":
                            Toast.makeText(LoginActivity.this, R.string.neispravno_korisnicko_ime, Toast.LENGTH_SHORT).show();
                            break;
                        case "notValidError":
                            Toast.makeText(LoginActivity.this, R.string.nedostupanServer, Toast.LENGTH_SHORT).show();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, R.string.nedostupanServer, Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                NetworkResponse response = error.networkResponse;

                if (error instanceof TimeoutError || error instanceof NoConnectionError)
                    Toast.makeText(LoginActivity.this, "Nemate internet konekciju!", Toast.LENGTH_SHORT).show();
                else if (response != null && response.data != null) {
                    switch (response.statusCode) {
                        /*case 400:
                            break;
                        case 500:
                            break;*/
                        default:
                            Toast.makeText(LoginActivity.this,"Greska na serveru", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        queue.add(customRequest);
    }
}
