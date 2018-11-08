package com.vts.samsung.labaccesscontrol.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.vts.samsung.labaccesscontrol.LoginActivity;
import com.vts.samsung.labaccesscontrol.R;

public class
MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
    }
}
