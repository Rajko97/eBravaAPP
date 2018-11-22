package com.vts.samsung.labaccesscontrol.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.vts.samsung.labaccesscontrol.Adapter.CustomTypefaceSpan;
import com.vts.samsung.labaccesscontrol.Fragment.AccessControl;
import com.vts.samsung.labaccesscontrol.Fragment.ActiveMembers;
import com.vts.samsung.labaccesscontrol.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigation;
    private ActionBarDrawerToggle mToggle;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("аppSettings", Context.MODE_PRIVATE);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        mNavigation = (NavigationView) findViewById(R.id.navigationView);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        {
            mDrawerLayout.addDrawerListener(mToggle);
            mToggle.syncState();

            if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                /*TextView tv = new TextView(getApplicationContext());
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
                tv.setLayoutParams(lp);
                tv.setText("Welcome!");
                        tv.setTextSize(20);
                tv.setTextColor(Color.parseColor("#FFFFFF"));
                //Typeface tf = Typeface.createFromAsset(getAssets(), "Asap-Medium.otf");
                tv.setTypeface(typeface);
                getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
                getSupportActionBar().setCustomView(tv);*/
            }



            mNavigation.setNavigationItemSelectedListener(this);
            mNavigation.setCheckedItem(R.id.menu_item_access);
        }
        setNavigationDrawerDesign();
        setNavigationDrawerFont();
        changeFragment(new AccessControl());
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
               if(id == R.id.menu_item_access || id == R.id.menu_item_online)//ToDO obrisati ovu liniju kad se odrade ostali fragmenti
               mNavigation.setCheckedItem(id);
           switch (id) {
               case R.id.menu_item_access:
                   changeFragment(new AccessControl());
                   break;
               case R.id.menu_item_online:
                   changeFragment(new ActiveMembers());
                   break;
               case R.id.menu_item_time:
                   Toast.makeText(this, "Fragment Provedeno Vreme nije kreiran", Toast.LENGTH_SHORT).show();
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

    private void changeFragment(Fragment fragment) {
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

        Button btnLogout = mNavigation.findViewById(R.id.btnLogOut);
        btnLogout.setTypeface(typeface);
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