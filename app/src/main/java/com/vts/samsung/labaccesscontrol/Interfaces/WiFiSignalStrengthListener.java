package com.vts.samsung.labaccesscontrol.Interfaces;

import com.vts.samsung.labaccesscontrol.Enums.StudentLocationRadius;

public interface WiFiSignalStrengthListener {

    public void onWifiStrengthChanged(StudentLocationRadius isNearLab);
}
