package com.example.instrumentedbike.Store_Firebase;

import android.app.Application;
import android.content.Intent;

import com.example.instrumentedbike.Constant;

import org.litepal.LitePal;


public class MyApplication extends Application {
    private static MyApplication myApplication;

    /**
     * On create.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        LitePal.initialize(this);
    }

    /**
     * Send pipe broad.
     *
     * @param action
     *         the action
     * @param data
     *         the data
     */
    public void sendPipeBroad(String action, String data) {
        Intent intent = new Intent(action);
        if (data != null) {
            intent.putExtra(Constant.DATA, data);
        }
        MyApplication.this.sendBroadcast(intent);
    }

    /**
     * Send pipe broad.
     *
     * @param action
     *         the action
     * @param ip
     *         the ip
     * @param data
     *         the data
     */
    public void sendPipeBroad(String action, String ip, String data) {
        Intent intent = new Intent(action);
        intent.putExtra(Constant.DEVICE_IP, ip);
        if (data != null) {
            intent.putExtra(Constant.DATA, data);
        }
        MyApplication.this.sendBroadcast(intent);
    }


    /**
     * Gets my application.
     *
     * @return the my application
     */
    public static MyApplication getMyApplication() {
        return myApplication;
    }


}
