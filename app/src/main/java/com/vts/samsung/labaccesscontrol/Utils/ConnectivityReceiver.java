package com.vts.samsung.labaccesscontrol.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.Toast;

import com.vts.samsung.labaccesscontrol.Enums.StudentLocationRadius;
import com.vts.samsung.labaccesscontrol.Interfaces.WiFiSignalStrengthListener;
import com.vts.samsung.labaccesscontrol.R;

import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class ConnectivityReceiver extends BroadcastReceiver {
    public enum ConnectionType{
        CONNECTION_NO_INTERNET,
        CONNECTION_MOBILE_DATA,
        CONNECTION_WIFI
    };
    private static SharedPreferences sharedPreferences;
    public static ConnectivityReceiverListener connectivityReceiverListener;
    public static WiFiSignalStrengthListener wiFiSignalStrengthListener;

    public ConnectivityReceiver() {
        super();
        sharedPreferences = Application.getInstance().getSharedPreferences("аppSettings", Context.MODE_PRIVATE);
    }

    @Override
    public void onReceive(Context context, Intent arg1) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean onSamsungAppsLab = false;
        ConnectionType connectionType = ConnectionType.CONNECTION_NO_INTERNET;

        if(activeNetwork != null) {
            connectionType = getConnectionType(Application.getInstance().getApplicationContext());
            onSamsungAppsLab=connectedOnAppsLab();
        }

        if (connectivityReceiverListener != null) {
            connectivityReceiverListener.onNetworkConnectionChanged(connectionType, onSamsungAppsLab);
        }

        if (wiFiSignalStrengthListener != null){
            //TODO: Proveri jacinu signala
            //Ako je jacina <50
            wiFiSignalStrengthListener.onWifiStrengthChanged(StudentLocationRadius.INLAB);
            //Ako je jacina >50 && < 80
            wiFiSignalStrengthListener.onWifiStrengthChanged(StudentLocationRadius.NEAR_LAB);
            //Onda
            wiFiSignalStrengthListener.onWifiStrengthChanged(StudentLocationRadius.OUTSIDE_LAB);
        }
    }

    public static boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) Application.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null;
    }

    public static boolean connectedOnAppsLab() {
        ConnectivityManager cm = (ConnectivityManager) Application.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mWifi.isConnected()) {
            return false;
        }


       if(Application.getInstance().getRouterMac() == null || Application.getInstance().getRouterMac().equals("02:00:00:00:00:00")) {
            findMacAdresses();
       }

       String currentMac = Application.getInstance().getRouterMac().toUpperCase();
        String labMac = sharedPreferences.getString("routerMAC", "00:00:00:00:00");
        String testMac = "c8:3a:35:13:63:70".toUpperCase();
        if(currentMac.equals(testMac))
            return true;
        if(currentMac.equals(labMac))
            return true;
        else if(Application.getInstance().getRouterMac().equals("02:00:00:00:00:00"))
        {   //Android 8.1 - API Level 27 - Android Oreo (Go edition)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                Toast.makeText(Application.getInstance().getApplicationContext(), "Da bi ste imali pristup lab, morate upaliti navigaciju", Toast.LENGTH_SHORT);
            }
            else {
                Toast.makeText(Application.getInstance().getApplicationContext(), "Greska pri pribavljanju MAC adrese rutera", Toast.LENGTH_SHORT);
                return false;
            }
        }
        return false;
    }

    private static void findMacAdresses() {
        WifiManager wifiManager = (WifiManager) Application.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if (Application.getInstance().getDeviceMac() == null) {
            if (wifiInfo != null && !TextUtils.isEmpty(wifiInfo.getMacAddress())) {
                Application.getInstance().setRouterMac(wifiInfo.getBSSID());
                // application.setIPDevice(wifiInfo.getIpAddress());

                String mac;
                if (Application.getInstance().getRouterMac() != null && !Application.getInstance().getRouterMac().equals("")) {
                    // if (wifiInfo.getMacAddress() == "02:00:00:00:00:00") {

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
                            Application.getInstance().setDeviceMac(mac.toUpperCase());
                        }
                    } catch (Exception ex) {
                        mac = "null";
                        Application.getInstance().setDeviceMac(wifiInfo.getMacAddress().toUpperCase());
                    }
                }
            }
        }
    }

    public static ConnectionType getConnectionType(Context context) {
        ConnectionType result = ConnectionType.CONNECTION_NO_INTERNET;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = ConnectionType.CONNECTION_WIFI;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = ConnectionType.CONNECTION_MOBILE_DATA;
                    }
                }
                else {
                    Application.getInstance().setDeviceMac(null);
                    Application.getInstance().setRouterMac(null);
                }
            }
        } else {
            if (cm != null) {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (activeNetwork != null) {
                    if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                        result = ConnectionType.CONNECTION_WIFI;
                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        result = ConnectionType.CONNECTION_MOBILE_DATA;
                    }
                } else {
                    Application.getInstance().setRouterMac(null);
                    Application.getInstance().setDeviceMac(null);
                }
            }
        }
        return result;
    }

    public interface ConnectivityReceiverListener {
        void onNetworkConnectionChanged(ConnectionType connectionType, boolean onSamsungAppsLab);
    }

    public static AlertDialog alertDialogNet(final Context c) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(c);
        alertDialogBuilder.setTitle(c.getResources().getString(R.string.dialogTitleConnection));
        alertDialogBuilder.setMessage(c.getResources().getString(R.string.dialogMessageNoConnection));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(c.getResources().getString(R.string.dialogButtonConnect),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        ((Activity) c).startActivity(intent);
                    }
                });
        alertDialogBuilder.setNegativeButton(c.getResources().getString(R.string.dialogButtonExit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        ((Activity) c).finish();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        return alertDialog;
    }

    public static AlertDialog alertDialogWifi(final Context c) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(c);
        alertDialogBuilder.setTitle(c.getResources().getString(R.string.dialogTitleMacAdress));
        alertDialogBuilder.setMessage(c.getResources().getString(R.string.dialogMessageNotAssigned));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(c.getResources().getString(R.string.dialogButtonTurnON),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                        ((Activity) c).startActivity(intent);
                    }
                });
        alertDialogBuilder.setNegativeButton(c.getResources().getString(R.string.dialogButtonExit),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        ((Activity) c).finish();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        return  alertDialog;
    }
}