package com.vts.samsung.labaccesscontrol.Utils;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.vts.samsung.labaccesscontrol.R;

public class CheckNetwork {
    public static boolean checkNet(Context c, Application application) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            final WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo wifiInfo = wifiManager.getConnectionInfo();

            //todo Zavrsiti funkciju checkNet
        }
        return true;
    }
    public static void alertDialogNet(final Context c) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(c);
        alertDialogBuilder.setTitle(c.getResources().getString(R.string.dialogTitleConnection));
        alertDialogBuilder.setMessage(c.getResources().getString(R.string.dialogMessageNoConnection));
    }
}