package com.vts.samsung.labaccesscontrol.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.TextUtils;

import com.vts.samsung.labaccesscontrol.R;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class CheckNetwork {
    public static boolean checkNet(Context c, Application application) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            final WifiManager wifiManager = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            {
                if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getMacAddress())) {
                    application.setRouterMac(wifiInfo.getBSSID());
                    // application.setIPDevice(wifiInfo.getIpAddress());
                    if (application.getRouterMac() != null && !application.getRouterMac().equals("")) {
                        // if (wifiInfo.getMacAddress() == "02:00:00:00:00:00") {
                        String mac;
                        try {
                            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                            for (NetworkInterface nif : all) {
                                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                                byte[] macBytes = nif.getHardwareAddress();
                                if (macBytes == null) {
                                    mac = "";
                                }

                                StringBuilder res1 = new StringBuilder();
                                for (byte b : macBytes) {
                                    res1.append(String.format("%02X:", b));
                                }

                                if (res1.length() > 0) {
                                    res1.deleteCharAt(res1.length() - 1);
                                }
                                mac = res1.toString();
                                application.setDeviceMac(mac.toUpperCase());
                            }
                        } catch (Exception ex) {
                            mac = "null";
                            application.setDeviceMac(wifiInfo.getMacAddress().toUpperCase());
                        }
                    }

                }
                return true;
            }
        } else {
            application.setDeviceMac(null);
            application.setRouterMac(null);
            return false;
        }
    }
}