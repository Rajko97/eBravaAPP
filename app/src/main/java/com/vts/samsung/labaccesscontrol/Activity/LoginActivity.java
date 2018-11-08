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

public class Login extends AppCompatActivity {

    private ImageButton btnSign;
    private EditText txtUser, txtPass;
    private JSONObject json;
    private ProgressDialog progressDialog;
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

//    private String md5(String in) {
//        MessageDigest digest;
//        try {
//            digest = MessageDigest.getInstance("MD5");
//            digest.reset();
//            digest.update(in.getBytes());
//            byte[] a = digest.digest();
//            int len = a.length;
//            StringBuilder sb = new StringBuilder(len << 1);
//            for (byte anA : a) {
//                sb.append(Character.forDigit((anA & 0xf0) >> 4, 16));
//                sb.append(Character.forDigit(anA & 0x0f, 16));
//            }
//            return sb.toString();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    private void btnSend() {
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSign.setImageResource(R.drawable.button_press);
                if (txtUser.getText().toString().length() > 0 && txtPass.getText().toString().length() > 0) {
                    new LoginAsyncTask().execute();
                } else {
                    btnSign.setImageResource(R.drawable.button);
                    Toast.makeText(Login.this, (R.string.obavezna_polja), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    class LoginAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(Login.this);
            progressDialog.setProgressStyle(R.style.ProgressBar);
            progressDialog.setMessage("Autorizacija korisnika, Molimo sačekajte . . . ");
            progressDialog.show();

            json = new JSONObject();
            try {
                json.put("macAddressDevice", "11:11:22:33:44:AC");
                json.put("username", txtUser.getText().toString());
                json.put("password", txtPass.getText().toString());
                Log.d("JSON", json.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String data = json.toString();
            BufferedReader reader;

            try {
                URL url = new URL(application.getRPi() + application.getRpiRouteLogin());
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(2000);
                conn.setReadTimeout(2000);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                OutputStreamWriter wr;
                wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line).append("\n");
                }

                return sb.toString();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Exception";
        }

        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String message;
            try {
                JSONObject jObject = new JSONObject(s);
                message = jObject.getString("message");
                switch (message) {
                    case "success":
                        Intent intent;
                        boolean doorPermission = jObject.getBoolean("doorPermission");
                        SharedPreferences.Editor usereditor = sharedPreferences.edit();
                        usereditor.putString("username", jObject.getString("username"));
                        usereditor.putString("ime", jObject.getString("name"));
                        usereditor.putString("prezime", jObject.getString("lastName"));
                        usereditor.putString("zvanje", jObject.getString("rank"));
                        usereditor.putString("macAdressaRuterInLab", jObject.getString("samsungAppsLabRouterMacAddress"));
                        usereditor.putString("JedinstveniToken", jObject.getString("uniqueToken"));

                        if (doorPermission) {
                            usereditor.putString("AccessLab", "true").apply();
                            intent = new Intent(Login.this, NavActivity.class);
                        } else {
                            usereditor.putString("AccessLab", "false").apply();
                            intent = new Intent(Login.this, WaitActiveActivity.class);
                        }
                        usereditor.apply();
                        progressDialog.dismiss();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        break;
                    case "notValidMacAddress":
                        Toast.makeText(Login.this, R.string.neovlascen_uredjaj, Toast.LENGTH_SHORT).show();
                        break;
                    case "notValidPassword":
                        Toast.makeText(Login.this, R.string.neispravno_lozinka, Toast.LENGTH_SHORT).show();
                        break;
                    case "notValidUsername":
                        Toast.makeText(Login.this, R.string.neispravno_korisnicko_ime, Toast.LENGTH_SHORT).show();
                        break;
                    case "notValidError":
                        Toast.makeText(Login.this, R.string.nedostupanServer, Toast.LENGTH_SHORT).show();
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(Login.this, R.string.nedostupanServer, Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }
}
