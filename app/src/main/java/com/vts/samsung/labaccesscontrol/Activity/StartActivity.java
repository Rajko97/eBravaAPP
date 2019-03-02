package com.vts.samsung.labaccesscontrol.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.vts.samsung.labaccesscontrol.R;

public class StartActivity extends Activity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar_FullScreen);
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("аppSettings", Context.MODE_PRIVATE);
        checkAccess();
    }

    private void checkAccess() {
        String status = sharedPreferences.getString("doorPermission", "login");
        Intent intent;
        switch (status) {
            case "true": intent = new Intent(StartActivity.this, MainActivity.class);break;
            case "false": intent = new Intent(StartActivity.this, MainActivity.class);break;
            default: intent = new Intent(StartActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
