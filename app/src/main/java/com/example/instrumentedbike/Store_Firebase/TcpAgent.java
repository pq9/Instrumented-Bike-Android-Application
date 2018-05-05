package com.example.instrumentedbike.Store_Firebase;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;


public class TcpAgent {
    private static TcpAgent instance;
    private MyTcpService mService;
    private String port;

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static TcpAgent getInstance() {
        if (instance == null) {
            instance = new TcpAgent();
        }
        return instance;
    }

    /**
     * Init.
     *
     * @param mContext
     *         the m context
     */
    public void init(Context mContext, String port) {
        this.port = port;
        bindService(mContext);
    }

    public void stopSDk(Context mContext) {
        unbindService(mContext);
    }

    /**
     * Send data.
     *
     * @param data
     *         the data
     */
    public void sendData(String data) {
        mService.sendData(data);
    }


    private void unbindService(Context mContext) {
        mContext.unbindService(connection);
    }


    private void bindService(Context mContext) {
        Intent service = new Intent(mContext, MyTcpService.class);
        mContext.bindService(service, connection, Context.BIND_AUTO_CREATE);
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            mService = ((MyTcpService.LoPongBinder) binder).getService();
            mService.port = port;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d("Out of connection", "connection miss:" + name);
        }
    };

}
