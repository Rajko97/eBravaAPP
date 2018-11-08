package com.vts.samsung.labaccesscontrol.Utils;

import java.util.ArrayList;

public class Application extends android.app.Application {

   /* private ArrayList<User> userArrayList;
    private ArrayList<Message> messageArrayList;*/

    private String deviceMac = null;
    private String routerMac = null;
    private String IPDevice;

    private String RPi = "http://160.99.39.135:443/";

    private String rpiRouteLogin = "login";
    private String rpiRouteAvailableUserInLab = "available-user";
    private String rpiRouteMyTime = "my-time";
    private String rpiRouteFireBaseToken = "firebase";
    private String rpiRouteChat = "chat";
    private String rpiRouteLogOut = "logout";

    @Override
    public void onCreate() {
        super.onCreate();
        //userArrayList = new ArrayList<>();
       // messageArrayList = new ArrayList<>();
    }

   /* public ArrayList<User> getUserArrayList() {
        return userArrayList;
    }

    public void setUserArrayList(ArrayList<User> userArrayList) {
        this.userArrayList = userArrayList;
    }

    public ArrayList<Message> getMessageArrayList() {
        return messageArrayList;
    }

    public void setMessageArrayList(ArrayList<Message> messageArrayList) {
        this.messageArrayList = messageArrayList;
    }*/

    public String getDeviceMac() {
        return deviceMac;
    }

    public void setDeviceMac(String deviceMac) {
        this.deviceMac = deviceMac;
    }

    public String getRouterMac() {
        return routerMac;
    }

    public void setRouterMac(String routerMac) {
        this.routerMac = routerMac;
    }

    public String getIPDevice() {
        return IPDevice;
    }

    public void setIPDevice(String IPDevice) {
        this.IPDevice = IPDevice;
    }

    public String getRPi() {
        return RPi;
    }

    public void setRPi(String RPi) {
        this.RPi = RPi;
    }

    public String getRpiRouteLogin() {
        return rpiRouteLogin;
    }

    public void setRpiRouteLogin(String rpiRouteLogin) {
        this.rpiRouteLogin = rpiRouteLogin;
    }

    public String getRpiRouteAvailableUserInLab() {
        return rpiRouteAvailableUserInLab;
    }

    public void setRpiRouteAvailableUserInLab(String rpiRouteAvailableUserInLab) {
        this.rpiRouteAvailableUserInLab = rpiRouteAvailableUserInLab;
    }

    public String getRpiRouteMyTime() {
        return rpiRouteMyTime;
    }

    public void setRpiRouteMyTime(String rpiRouteMyTime) {
        this.rpiRouteMyTime = rpiRouteMyTime;
    }

    public String getRpiRouteFireBaseToken() {
        return rpiRouteFireBaseToken;
    }

    public void setRpiRouteFireBaseToken(String rpiRouteFireBaseToken) {
        this.rpiRouteFireBaseToken = rpiRouteFireBaseToken;
    }

    public String getRpiRouteChat() {
        return rpiRouteChat;
    }

    public void setRpiRouteChat(String rpiRouteChat) {
        this.rpiRouteChat = rpiRouteChat;
    }

    public String getRpiRouteLogOut() {
        return rpiRouteLogOut;
    }

    public void setRpiRouteLogOut(String rpiRouteLogOut) {
        this.rpiRouteLogOut = rpiRouteLogOut;
    }
}
