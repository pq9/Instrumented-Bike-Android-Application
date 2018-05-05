package com.example.instrumentedbike.Tcp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.blanke.xsocket.tcp.client.TcpConnConfig;
import com.blanke.xsocket.tcp.client.XTcpClient;
import com.blanke.xsocket.tcp.client.bean.TargetInfo;
import com.blanke.xsocket.tcp.client.bean.TcpMsg;
import com.blanke.xsocket.tcp.client.helper.stickpackage.AbsStickPackageHelper;
import com.blanke.xsocket.tcp.client.listener.TcpClientListener;
import com.blanke.xsocket.utils.StringValidationUtils;
import com.example.instrumentedbike.R;
import com.example.instrumentedbike.layout.ConsoleLayout;
import com.example.instrumentedbike.layout.StaticPackageLayout;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class TcpclientActivity extends AppCompatActivity implements View.OnClickListener, TcpClientListener {

    private Button tcpclientBuConnect;
    private EditText tcpclientEdit;
    private EditText tcpclientEditIp;
    private Button tcpclientBuSend;
    private StaticPackageLayout tcpclientStaticpackagelayout;
    private ConsoleLayout tcpclientConsole;
    private SwitchCompat tcpclientSwitchReconnect;
    private XTcpClient xTcpClient;

//    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcpclient);
        tcpclientBuConnect = (Button) findViewById(R.id.tcpclient_bu_connect);
        tcpclientEdit = (EditText) findViewById(R.id.tcpclient_edit);
        tcpclientBuSend = (Button) findViewById(R.id.tcpclient_bu_send);
        tcpclientStaticpackagelayout = (StaticPackageLayout) findViewById(R.id.tcpclient_staticpackagelayout);
        tcpclientEditIp = (EditText) findViewById(R.id.tcpclient_edit_ip);
        tcpclientConsole = (ConsoleLayout) findViewById(R.id.tcpclient_console);
        tcpclientSwitchReconnect = (SwitchCompat) findViewById(R.id.tcpclient_switch_reconnect);
        tcpclientBuConnect.setOnClickListener(this);
        tcpclientBuSend.setOnClickListener(this);
        String ipM = getIP(this);
        addMsg(ipM);
//        database = FirebaseFirestore.getInstance();
        //setFireBaseValue("123", "123");
    }

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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tcpclient_bu_connect) {
            tcpclientConsole.clearConsole();
            if (xTcpClient != null && xTcpClient.isConnected()) {
                xTcpClient.disconnect();
            } else {
                AbsStickPackageHelper stickHelper = tcpclientStaticpackagelayout.getStickPackageHelper();
                if (stickHelper == null) {
                    addMsg("Error");
                    return;
                }
                String temp = tcpclientEditIp.getText().toString().trim();
                String[] temp2 = temp.split(":");
                if (temp2.length == 2 && StringValidationUtils.validateRegex(temp2[0], StringValidationUtils.RegexIP)
                        && StringValidationUtils.validateRegex(temp2[1], StringValidationUtils.RegexPort)) {
                    TargetInfo targetInfo = new TargetInfo(temp2[0], Integer.parseInt(temp2[1]));
                    xTcpClient = XTcpClient.getTcpClient(targetInfo);
                    xTcpClient.addTcpClientListener(this);
                    xTcpClient.config(new TcpConnConfig.Builder()
                            .setStickPackageHelper(stickHelper)
                            .setIsReconnect(tcpclientSwitchReconnect.isChecked())
                            .create());
                    if (xTcpClient.isDisconnected()) {
                        xTcpClient.connect();
                    } else {
                        addMsg("Connection Exist");
                    }
                } else {
                    addMsg("Wrong format of ip:port");
                }
            }
        } else {//send msg
            String text = tcpclientEdit.getText().toString().trim();
            if (xTcpClient != null) {
                xTcpClient.sendMsg(text);
            } else {
                addMsg("None connection");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (xTcpClient != null) {
            xTcpClient.removeTcpClientListener(this);
//            xTcpClient.disconnect();
        }
    }

    private void addMsg(String msg) {
        this.tcpclientConsole.addLog(msg);
    }

    @Override
    public void onConnected(XTcpClient client) {
        addMsg(client.getTargetInfo().getIp() + "Connection Success");
    }

    @Override
    public void onSended(XTcpClient client, TcpMsg tcpMsg) {
        addMsg("Me:" + tcpMsg.getSourceDataString());
    }

    @Override
    public void onDisconnected(XTcpClient client, String msg, Exception e) {
        addMsg(client.getTargetInfo().getIp() + "Disconnection " + msg + e);
    }

    @Override
    public void onReceive(XTcpClient client, TcpMsg msg) {
        byte[][] res = msg.getEndDecodeData();
        byte[] bytes = new byte[0];
        for (byte[] i : res) {
            bytes = i;
            break;
        }
        try {
            addMsg(client.getTargetInfo().getIp() + ":" + " len= " + bytes.length + ", "
                    + msg.getSourceDataString() + " HexDecimal bytes=" + Arrays.toString(bytes) + "ToString:" + new String(bytes, "UTF-8"));


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void setFireBaseValue(String longitude, String latitude) {
        Map<String, Object> user = new HashMap<>();
        user.put("longitude", longitude);
        user.put("latitude", latitude);
        user.put("time", ""+System.currentTimeMillis());

//        database.collection("location")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference v) {
//                        Log.d("a", "DocumentSnapshot added with ID: ");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("a", "Error adding document", e);
//                    }
//                });
    }

    @Override
    public void onValidationFail(XTcpClient client, TcpMsg tcpMsg) {

    }
}

