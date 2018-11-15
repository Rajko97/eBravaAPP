package com.vts.samsung.labaccesscontrol.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
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

public class LoginActivity extends AppCompatActivity {

    private ImageButton btnSign;
    private EditText txtUser, txtPass;
    private Application application;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;

    //0 - dialog No Connection
    //1 - dialog No MAC (WIFI should be on)
    private boolean[] showingDialog = {false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        Typeface typeface = Typeface.createFromAsset(getApplicationContext().getAssets(), "exo.ttf");

        btnSign = findViewById(R.id.btnSingUp);
        txtUser = findViewById(R.id.inputUsername);
        txtPass = findViewById(R.id.inputPassword);

        TextView tvSignUp = findViewById(R.id.tvSignUp);
        tvSignUp.setTypeface(typeface);
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
            if (application.getDeviceMac() != null && !application.getDeviceMac().equals("02:00:00:00:00:00")) {
                showingDialog[0] = showingDialog[1] = false;
                btnSend();
            }
            else {
                if (!showingDialog[1]) {
                    CheckNetwork.alertDialogWifi(this, showingDialog);
                    showingDialog[1] = true;
                }
            }
        } else {
            if(!showingDialog[0]) {
                CheckNetwork.alertDialogNet(this, showingDialog);
                showingDialog[0] = true;
            }
        }
    }

    private void btnSend() {
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSign.setImageResource(R.drawable.button_press);
                if (txtUser.getText().toString().length() > 0 && txtPass.getText().toString().length() > 0) {
                    /*if (application.getDeviceMac() == null || application.getDeviceMac().equals("02:00:00:00:00:00")) //greska kada mac adresa nije dodeljena
                        CheckNetwork.alertDialogWifi(LoginActivity.this);
                    else*/
                        sendLoginRequest();

                } else {
                    btnSign.setImageResource(R.drawable.button);
                    Toast.makeText(LoginActivity.this, (R.string.obavezna_polja), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendLoginRequest() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setProgressStyle(R.style.ProgressBar);
        progressDialog.setMessage(getResources().getString(R.string.dialogAuthorizing));
        progressDialog.setCancelable(false);
        progressDialog.show();

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        final JsonObjectRequest customRequest = new JsonObjectRequest(Request.Method.POST, application.getRPi() + application.getRpiRouteLogin(),getPostRequestBody(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                progressDialog.dismiss();
                onRequestSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               progressDialog.dismiss();
               onRequestError(error);
            }
        });
        queue.add(customRequest);
    }

    private JSONObject getPostRequestBody() {
        JSONObject loginInfo = new JSONObject();
        try {
            loginInfo.put("macAddressDevice", application.getDeviceMac());
            loginInfo.put("username", txtUser.getText().toString());
            loginInfo.put("password", txtPass.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return loginInfo;
    }

    public void onRequestSuccess(JSONObject response) {
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
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                    break;
                case "notValidMacAddress":
                    Toast.makeText(LoginActivity.this, R.string.errNotValidMac, Toast.LENGTH_SHORT).show();
                    break;
                case "notValidPassword":
                    Toast.makeText(LoginActivity.this, R.string.errPassword, Toast.LENGTH_SHORT).show();
                    break;
                case "notValidUsername":
                    Toast.makeText(LoginActivity.this, R.string.errUserName, Toast.LENGTH_SHORT).show();
                    break;
                case "notValidError":
                    Toast.makeText(LoginActivity.this, R.string.errOnSrv, Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(LoginActivity.this, R.string.errOnSrv, Toast.LENGTH_SHORT).show();
        }
    }

    public void onRequestError(VolleyError error) {
        NetworkResponse response = error.networkResponse;

        if (error instanceof TimeoutError || error instanceof NoConnectionError)
            Toast.makeText(LoginActivity.this, getResources().getString(R.string.errConnectionFailure), Toast.LENGTH_SHORT).show();
        else if (response != null && response.data != null) {
            switch (response.statusCode) {
                        /*case 400:
                            break;
                        case 500:
                            break;*/
                default:
                    Toast.makeText(LoginActivity.this,getResources().getString(R.string.errUnhandled)+" ("+response.statusCode+")", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
