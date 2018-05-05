package com.example.instrumentedbike.Tcp;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blanke.xsocket.tcp.client.bean.TcpMsg;
import com.example.instrumentedbike.Constant;
import com.example.instrumentedbike.LineChart.LineChartActivity;
import com.example.instrumentedbike.Maps.MapsActivity;
import com.example.instrumentedbike.R;
import com.example.instrumentedbike.Store_Firebase.MyApplication;
import com.example.instrumentedbike.Store_Firebase.TcpAgent;
import com.example.instrumentedbike.Store_Firebase.TcpData;
import com.example.instrumentedbike.layout.models.FirebaseMarker;
import com.example.instrumentedbike.layout.models.SignInActivity;
import com.example.instrumentedbike.utils.Seesion;
import com.google.firebase.auth.FirebaseAuth;
import com.example.instrumentedbike.layout.ConsoleLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.litepal.crud.callback.SaveCallback;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * The type Tcpserver activity.
 * /**
 * Created by 邱培杰 on 2018/3/13.
 */

public class TcpserverActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText tcpserverEditFileName;
    private Button tcpserverBuConnect;
    private ConsoleLayout tcpserverConsole;
    private FirebaseAuth mAuth;
    private boolean isRegisterBroadcast = false;
    private Button btnmap;
    private boolean isStart = false;
    private Button btnchart;
    private Button signout;
    String port="2222";

    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpserver);
        this.signout=(Button)findViewById (R.id.btn_signout);
        this.btnchart = (Button) findViewById(R.id.btn_chart);
        this.btnmap = (Button) findViewById(R.id.btn_map);
        tcpserverEditFileName = (EditText) findViewById(R.id.tcpserver_edit_filename);
        tcpserverBuConnect = (Button) findViewById(R.id.tcpserver_bu_connect);
        tcpserverConsole = (ConsoleLayout) findViewById(R.id.tcpserver_console);
        initFilter();
        signout.setOnClickListener (this);
        tcpserverBuConnect.setOnClickListener(this);
        btnchart.setOnClickListener(this);
        btnmap.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance ();
        String ipM = getIP(this);
        addMsg("LocalIP:" + ipM);
    }

    private void initFilter() {
        isRegisterBroadcast = true;
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constant.BROADCAST_TCPSERVICE_SUCCESSFUL);
        myIntentFilter.addAction(Constant.BROADCAST_TCPSERVICE_READ);
        myIntentFilter.addAction(Constant.BROADCAST_TCPSERVICE_WRITE);
        myIntentFilter.addAction(Constant.BROADCAST_TCPSERVICE_DISCONNECT);
        myIntentFilter.addAction(Constant.BROADCAST_TCPSERVICE_CLOSED);
        registerReceiver(mBroadcastReceiver, myIntentFilter);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String action = intent.getAction();

            if (action == null) {
                return;
            }
            if (action.equals(Constant.BROADCAST_TCPSERVICE_SUCCESSFUL)) {
                addMsg("Start Successfully");
            } else if (action.equals(Constant.BROADCAST_TCPSERVICE_READ)) {
                String ip = intent.getStringExtra(Constant.DEVICE_IP);
                final String data = intent.getStringExtra(Constant.DATA);

                addMsg (data);
            } else if (action.equals(Constant.BROADCAST_TCPSERVICE_WRITE)) {
                String data = intent.getStringExtra(Constant.DATA);
                String ip = intent.getStringExtra(Constant.DEVICE_IP);
                addMsg(data);
            } else if (action.equals(Constant.BROADCAST_TCPSERVICE_DISCONNECT)) {
                String data = intent.getStringExtra(Constant.DATA);
                String ip = intent.getStringExtra(Constant.DEVICE_IP);
                //addMsg("Disconnection: " + ip + "," + data);
            } else if (action.equals(Constant.BROADCAST_TCPSERVICE_CLOSED)) {
                //addMsg("Server Stop");
            }

        }
    };


    /**
     * Gets ip.
     *
     * @param context
     *         the context
     * @return the ip
     */
    public static String getIP(Context context) {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tcpserver_bu_connect) {
            tcpserverConsole.clearConsole();
            if (!isStart) {

                Seesion.fileName = tcpserverEditFileName.getText().toString();

                TcpAgent.getInstance().init(MyApplication.getMyApplication(), port);
                isStart = true;
            } else {
                TcpAgent.getInstance().stopSDk(MyApplication.getMyApplication());
                isStart = false;
            }
        } else if (v.getId() == R.id.btn_map) {
            start(MapsActivity.class);
        } else if (v.getId() == R.id.btn_chart) {
            start(LineChartActivity.class);
        } else if(v.getId ()==R.id.btn_signout){
            SignOut ();
            start(SignInActivity.class);
        }
    }
    private void SignOut() {
        mAuth.signOut();
    }
    private void start(Class activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegisterBroadcast) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    private void addMsg(String msg) {
        tcpserverConsole.addLog(msg);
    }


}

