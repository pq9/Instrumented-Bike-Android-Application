package com.example.instrumentedbike;

import com.example.instrumentedbike.Store_Firebase.MyApplication;

public class Constant {

    public static final String PACKAGE_NAME = MyApplication.getMyApplication().getPackageName();

    public static final String BROADCAST_TCPSERVICE_SUCCESSFUL = PACKAGE_NAME + ".successful";
    public static final String BROADCAST_TCPSERVICE_READ = PACKAGE_NAME + ".read";
    public static final String BROADCAST_TCPSERVICE_DISCONNECT= PACKAGE_NAME + ".disconnect";
    public static final String BROADCAST_TCPSERVICE_CLOSED= PACKAGE_NAME + ".Closed";
    public static final String BROADCAST_TCPSERVICE_WRITE = PACKAGE_NAME + ".write";
    //public static final String BROADCAST_TCPSERVICE_FILENAME=PACKAGE_NAME+".filename";
    public static final String DATA = "data";

    public static final String DEVICE_IP = "deviceIp";
    //public static final String FileName="filename";
}
