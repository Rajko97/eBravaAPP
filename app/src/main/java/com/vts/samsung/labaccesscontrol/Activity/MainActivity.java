package com.vts.samsung.labaccesscontrol.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;
import com.vts.samsung.labaccesscontrol.Adapter.CustomTypefaceSpan;
import com.vts.samsung.labaccesscontrol.Fragment.AccessControl;
import com.vts.samsung.labaccesscontrol.Fragment.ActiveMembers;
import com.vts.samsung.labaccesscontrol.Fragment.TimeInLab;
import com.vts.samsung.labaccesscontrol.R;
import com.vts.samsung.labaccesscontrol.Services.GPS_Service;
import com.vts.samsung.labaccesscontrol.Services.checkInLab_Service;
import com.vts.samsung.labaccesscontrol.Utils.Application;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigation;
    private ActionBarDrawerToggle mToggle;
    private SharedPreferences sharedPreferences;
    private BroadcastReceiver broadcastReceiver;
    private Application application;
    private Fragment accessControl, activeMembers, timeInLab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        application = (Application) getApplication();

        accessControl = new AccessControl();
        activeMembers = new ActiveMembers();
        timeInLab = new TimeInLab();

        sharedPreferences = getSharedPreferences("аppSettings", Context.MODE_PRIVATE);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        mNavigation = (NavigationView) findViewById(R.id.navigationView);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        {
            mDrawerLayout.addDrawerListener(mToggle);
            mToggle.syncState();

            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            mNavigation.setNavigationItemSelectedListener(this);
            mNavigation.setCheckedItem(R.id.menu_item_access);
        }
        setNavigationDrawerDesign();
        setNavigationDrawerFont();
        changeFragment(new AccessControl());

        //TODO Pokretanje servisa
        if (!isMyServiceRunning(checkInLab_Service.class)) {
            Intent i = new Intent(getApplicationContext(), checkInLab_Service.class);
            startService(i);
        }
        //TODO ----------------------------
    }

    //TODO provera servisa
    private boolean isMyServiceRunning(Class<checkInLab_Service> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
    //TODO ----------------------------

    @Override
    protected void onResume() {
        super.onResume();
        if(broadcastReceiver == null) {
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    //textView.setText("trenutna lok: "+intent.getExtras().get("coordinates"));
                }
            };
        }
        registerReceiver(broadcastReceiver, new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    private boolean getGPSpermission() {
        if(Build.VERSION.SDK_INT >= 23
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getGPSpermission();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
           int id = item.getItemId();
           //if(id != R.id.menu_item_logout)
               if(id == R.id.menu_item_access || id == R.id.menu_item_online || id == R.id.menu_item_time)//ToDO obrisati ovu liniju kad se odrade ostali fragmenti
               mNavigation.setCheckedItem(id);
           switch (id) {
               case R.id.menu_item_access:
                   changeFragment(accessControl);
                   break;
               case R.id.menu_item_online:
                   changeFragment(activeMembers);
                   break;
               case R.id.menu_item_time:
                   changeFragment(timeInLab);
                   break;
               case R.id.menu_item_messages:
                   Toast.makeText(this, "Fragment Poruke nije kreiran", Toast.LENGTH_SHORT).show();
                   break;
               /*case R.id.menu_item_logout:
                   logOutFromDevice();
                   break;*/
           }
           mDrawerLayout.closeDrawer(GravityCompat.START);
           return false;
    }

    public void changeFragment(Fragment fragment) {
        FragmentTransaction ft;
        ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
    }

    private void logOutFromDevice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getResources().getString(R.string.dialogTitleLogOut));
        builder.setMessage(getResources().getString(R.string.dialogMessageLogOut));
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getString(R.string.dialogButtonYes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences.Editor usereditor = sharedPreferences.edit();
                //todo Poslati LogOut signal RPi-u pre izlaska
                usereditor.clear().apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.dialogButtonNo), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setNavigationDrawerDesign() {
        Typeface typeface = Typeface.createFromAsset(getApplicationContext().getAssets(), "exo.ttf");

        View navHeaderView = mNavigation.getHeaderView(0);

        TextView tvName = navHeaderView.findViewById(R.id.tvHeaderName);
        TextView tvRank = navHeaderView.findViewById(R.id.tvHeaderRank);

        String firstName = sharedPreferences.getString("firstName", "errorName");
        String lastName = sharedPreferences.getString("lastName", "errorLastName");
        String rank = sharedPreferences.getString("rank", "errorRank");

        tvName.setText(firstName+" "+lastName);
        tvRank.setText(rank);

        tvName.setTypeface(typeface);
        tvName.setTypeface(typeface);

        ImageButton btnLogout = mNavigation.findViewById(R.id.btnLogOut);
        //btnLogout.setTypeface(typeface);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                logOutFromDevice();
            }
        });
    }

    private void setNavigationDrawerFont() {
        Menu menu = mNavigation.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);

            SubMenu subMenu = menuItem.getSubMenu();
            if(subMenu != null && subMenu.size() > 0) {
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem =subMenu.getItem(j);
                    applyFontToMenuItem(subMenuItem);
                }
            }
            applyFontToMenuItem(menuItem);
        }
    }

    private void applyFontToMenuItem(MenuItem menuItem) {
        Typeface font = Typeface.createFromAsset(getAssets(), "exo.ttf");
        SpannableString mNewTitle = new SpannableString(menuItem.getTitle());
        mNewTitle.setSpan(new CustomTypefaceSpan("", font), 0, mNewTitle.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        menuItem.setTitle(mNewTitle);
    }
}