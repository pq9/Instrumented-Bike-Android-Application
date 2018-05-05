package com.example.instrumentedbike.Store_Firebase;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.blanke.xsocket.tcp.client.TcpConnConfig;
import com.blanke.xsocket.tcp.client.XTcpClient;
import com.blanke.xsocket.tcp.client.bean.TcpMsg;
import com.blanke.xsocket.tcp.server.TcpServerConfig;
import com.blanke.xsocket.tcp.server.XTcpServer;
import com.blanke.xsocket.tcp.server.listener.TcpServerListener;
import com.example.instrumentedbike.Constant;
import com.example.instrumentedbike.layout.models.FirebaseMarker;
import com.example.instrumentedbike.utils.Seesion;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.litepal.crud.callback.SaveCallback;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The type My tcp service.
 */
public class MyTcpService extends Service implements TcpServerListener {
    private IBinder binder = new MyTcpService.LoPongBinder ();
    private XTcpServer mXTcpServer;
    private DatabaseReference mDatabase;
    /**
     * The Port.
     */
    public String port = "2222";
    private boolean isRegisterBroadcast = false;

    /**
     * The type Lo pong binder.
     */
    public class LoPongBinder extends Binder {
        /**
         * Gets service.
         *
         * @return the service return local data
         */
        MyTcpService getService() {
            System.out.println ("MyService is LoPongBinder");
            return MyTcpService.this;
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println ("MyService onStartCommand()：Called by the system every time a client explicitly starts the service by calling android.content.Context.startService, providing the arguments it supplied and a unique integer token representing the start request.");
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        if (mXTcpServer == null) {
            mXTcpServer = XTcpServer.getTcpServer (Integer.parseInt (port));
            mXTcpServer.addTcpServerListener (this);
            mXTcpServer.config (new TcpServerConfig.Builder ()
                    .setTcpConnConfig (new TcpConnConfig.Builder ().create ()).create ());
        }
        mXTcpServer.startServer ();
        System.out.println ("MyService onCreate()：Called by the system when the service is first created");
    }



    /**
     * Send data.
     *
     * @param data the data
     */
    public void sendData(String data) {
        if (mXTcpServer != null) {
            mXTcpServer.sendMsgToAll (data);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        System.out.println ("MyService onUnbind()：Called when all clients have disconnected from a particular interface published by the service.");
        return super.onUnbind (intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy ();
        if (mXTcpServer != null) {
            mXTcpServer.removeTcpServerListener (this);
            mXTcpServer.stopServer ();
        }

  }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreated(XTcpServer server) {
        MyApplication.getMyApplication ().sendPipeBroad (Constant.BROADCAST_TCPSERVICE_SUCCESSFUL, null);
//        addMsg("Server Start");

    }

    @Override
    public void onListened(XTcpServer server) {
//        addMsg("Server listenling " + server.getPort());
    }

    @Override
    public void onAccept(XTcpServer server, XTcpClient tcpClient) {
//        addMsg("Received Data " + tcpClient.getTargetInfo().getIp());
    }

    @Override
    public void onSended(XTcpServer server, XTcpClient tcpClient, TcpMsg tcpMsg) {
        MyApplication.getMyApplication ().sendPipeBroad (Constant.BROADCAST_TCPSERVICE_WRITE, tcpClient.getTargetInfo ().getIp (), tcpMsg.getSourceDataString ());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onReceive(XTcpServer server, XTcpClient tcpClient, TcpMsg tcpMsg) {
        String fileName = Seesion.fileName;
        String[] info = tcpMsg.getSourceDataString ().split (",");
        //Log.w ("Received data", tcpMsg.getSourceDataString ());
        Log.w("Filename",fileName);
        if (info.length == 8) {
        double Long=Double.valueOf (info[0]);
        double Lat=Double.valueOf (info[1]);
        double Acc1x=Double.valueOf (info[2]);
        double Acc1y=Double.valueOf (info[3]);
        double Acc1z=Double.valueOf (info[4]);
        double Acc2x=Double.valueOf (info[5]);
        double Acc2y=Double.valueOf (info[6]);
        double Acc2z=Double.valueOf (info[7]);
        setFireBaseValue (fileName,tcpMsg,Lat,Long,Acc1x,Acc1y,Acc1z,Acc2x,Acc2y,Acc2z);
        } else
            Log.w ("error", "message does not receive");

        MyApplication.getMyApplication ().sendPipeBroad (Constant.BROADCAST_TCPSERVICE_READ, tcpClient.getTargetInfo ().getIp (), tcpMsg.getSourceDataString ());

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setFireBaseValue(String filename,TcpMsg tcpMsg,double latitude,double longitude,double Acc1_x,double Acc1_y,double Acc1_z,double Acc2_x,double Acc2_y,double Acc2_z) {
        mDatabase = FirebaseDatabase.getInstance ().getReference (filename);
        String key = mDatabase.child ("location").push ().getKey ();
        FirebaseMarker firebaseMarker = new FirebaseMarker (latitude, longitude, Acc1_x, Acc1_y, Acc1_z, Acc2_x, Acc2_y, Acc2_z);
        java.util.Map<String, Object> marker = firebaseMarker.toMap ();
        Map<String, Object> childUpdates = new HashMap<> ();
        childUpdates.put ("/location/" + key, marker);
        mDatabase.updateChildren (childUpdates);

        TcpData tcpData = new TcpData ();
        tcpData.setReleaseDate (new Date ());
        tcpData.setData (tcpMsg.getSourceDataString ());
        Log.e ("MyTcpService", tcpMsg.getSourceDataString ());
        tcpData.saveAsync ().listen (new SaveCallback () {
            @Override
            public void onFinish(boolean success) {
                Log.i ("Save Data", "The stored data：" + success);
            }
        });
    }


    @Override
    public void onValidationFail(XTcpServer server, XTcpClient client, TcpMsg tcpMsg) {

    }

    @Override
    public void onClientClosed(XTcpServer server, XTcpClient tcpClient, String msg, Exception e) {
        MyApplication.getMyApplication ().sendPipeBroad (Constant.BROADCAST_TCPSERVICE_DISCONNECT, tcpClient.getTargetInfo ().getIp (), msg);
    }

    @Override
    public void onServerClosed(XTcpServer server, String msg, Exception e) {
        MyApplication.getMyApplication ().sendPipeBroad (Constant.BROADCAST_TCPSERVICE_CLOSED, msg);
    }

}