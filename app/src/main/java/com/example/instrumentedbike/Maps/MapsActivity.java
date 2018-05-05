package com.example.instrumentedbike.Maps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.instrumentedbike.Constant;
import com.example.instrumentedbike.R;
import com.example.instrumentedbike.layout.models.FirebaseMarker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.List;


/**
 * The type Maps activity.
 /**
 * Created by 邱培杰 on 2018/3/13.
 */

public class MapsActivity extends FragmentActivity implements GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMarkerDragListener,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private GoogleMap mMap;
    private int soundID;
    private SoundPool sp;
    private boolean isRegisterBroadcast = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private AddressResultReceiver mResultReceiver;
    private boolean mAddressRequested;
    private Marker perth;
    private LatLng lastLatLng, perthLatLng;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance ().getReference ();
    List<LatLng> points=new ArrayList<LatLng>();
    private EditText threshold;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mDatabase.keepSynced (true);
        // return the result from FetchAddressIntentService
        mResultReceiver = new AddressResultReceiver(new Handler());
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        sp = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundID = sp.load(getApplicationContext(), R.raw.rington, 1);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        threshold=(EditText) findViewById (R.id.threshold);
        mapFragment.getMapAsync(this);
        initFilter();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRegisterBroadcast) {
            unregisterReceiver(mBroadcastReceiver);
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

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
                //showTost("Server Start Successfully");
            } else if (action.equals(Constant.BROADCAST_TCPSERVICE_READ)) {
                String data = intent.getStringExtra(Constant.DATA);
                String ip = intent.getStringExtra(Constant.DEVICE_IP);

                String[] info = data.split(",");
                if (mLastLocation != null) {
                    Log.i("MapsActivity", "Latitude-->" + String.valueOf(mLastLocation.getLatitude()));
                    Log.i("MapsActivity", "Longitude-->" + String.valueOf(mLastLocation.getLongitude()));
                }
                if (info.length == 8) {
                        showTost(data);
                    }

            } else if (action.equals(Constant.BROADCAST_TCPSERVICE_WRITE)) {
                String data = intent.getStringExtra(Constant.DATA);
                String ip = intent.getStringExtra(Constant.DEVICE_IP);
                //showTost("我发送： " + ip + "," + data);
            } else if (action.equals(Constant.BROADCAST_TCPSERVICE_DISCONNECT)) {
                String data = intent.getStringExtra(Constant.DATA);
                String ip = intent.getStringExtra(Constant.DEVICE_IP);
                //showTost("断开连接: " + ip + "," + data);
            } else if (action.equals(Constant.BROADCAST_TCPSERVICE_CLOSED)) {
                //showTost("服务停止");
            }

        }
    };

    private void showTost(String data) {
        Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType (GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMyLocationButtonClickListener (this);
        mMap.setOnMarkerDragListener (this);
        final List<String> lst = new ArrayList<String> ();
        lst.add ("Claer Map");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dsp : dataSnapshot.getChildren()) {
                    //Add file_name to an Array
                    String name=dsp.getKey ();
                    if (!(name.equals ("users"))){
                        lst.add(name); //add result into array filename
                        Log.w ("name", String.valueOf (lst));
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        MaterialSpinner spinner = (MaterialSpinner) findViewById (R.id.spinners);
        spinner.setItems (lst);

        spinner.setOnItemSelectedListener (new MaterialSpinner.OnItemSelectedListener<String> () {

            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Log.e ("Selected", item);
                String value=threshold.getText().toString();
                int length=threshold.length ();
                if (length!=0){
                    double Threshold=Double.valueOf (value);
                    points=null;
                    points=new ArrayList<LatLng>();

                    mMap.clear ();
                    AddBumps (item,Threshold);

                }else {
                    showTost("Threshold is empty! Please Enter Threshold!");
                    return;
                }


            }
        });


        Notification ();


    }

    public double getDistance(LatLng start, LatLng end) {
        double lat1 = (Math.PI / 180) * start.latitude;
        double lat2 = (Math.PI / 180) * end.latitude;
        double lon1 = (Math.PI / 180) * start.longitude;
        double lon2 = (Math.PI / 180) * end.longitude;
        double R = 6371;
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;
        return d * 1000;//(Meters)
    }

    //Setup notification
    private void Notification(){
         for (int n=0;n<points.size ();n++){
             if (mLastLocation != null) {
                 double CurrentLongitude=mLastLocation.getLongitude ();
                 double CurrentLatitude=mLastLocation.getLatitude ();
                 LatLng end=new LatLng (CurrentLatitude,CurrentLongitude);
                 double mi=getDistance (points.get (n),end);
                 if(mi<50){
                     soundID = sp.play(soundID, 0.8f, 0.8f, 1, 0, 1.0f);
                     showTost ("The bump is approaching");
                 }
             }

        }
    }
    private void AddSpinner(final List<String> lst) {


    }
    private void AddBumps(String name,final double Thres){
        if (!name.equals ("Clear Map")) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance ().getReference (name).child ("location");
            databaseReference.addValueEventListener (new ValueEventListener () {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<Double>Acc1x= new ArrayList<Double> ();
                    List<Double>Acc1y= new ArrayList<Double> ();
                    List<Double> Acc1z = new ArrayList<Double> ();
                    List<Double> Lat = new ArrayList<Double> ();
                    List<Double> Long = new ArrayList<Double> ();
                    for (DataSnapshot dsp2 : dataSnapshot.getChildren ()) {
                        FirebaseMarker firebaseMarker = dsp2.getValue (FirebaseMarker.class);
                        Acc1x.add (firebaseMarker.Acc1_x);
                        Acc1y.add (firebaseMarker.Acc1_y);
                        Acc1z.add (firebaseMarker.Acc1_z);
                        Lat.add (firebaseMarker.latitude);
                        Long.add (firebaseMarker.longitude);
                    }
                    //Retrive data and find out the bumps
                    //Setup windows to filter data and find out bumps
                    int windows_index = 10;
                    double max = 1;
                    double min = 1;
                    int index_max = 0;
                    int index_min = 0;
                    for (int j = 0; j < windows_index; j++) {
                        if (windows_index <= Acc1z.size ()) {
                            //Amplify Singal
                            double noml=Math.pow (Acc1x.get (j),2)+Math.pow (Acc1y.get (j),2)+Math.pow (Acc1z.get (j),2);
                            double Acc1=Math.sqrt (noml);


                            //find the maximum value of a window

                            if (Acc1>=max) {
                                max = Acc1;
                                index_max = j;
                            }

                            if(Acc1 <= min) {
                                min = Acc1;
                                index_min = j;

                            }


                            if (j >= windows_index - 1) {

                                double THreshold = Math.abs (max - min);
                                if (THreshold >= Thres) {
                                    int midpoint= (int) Math.floor ((index_max+index_min)/2);
                                    LatLng location=new LatLng (Lat.get (midpoint),Long.get (midpoint));
                                    //LatLng location = midpoint (Lat.get (index_max), Long.get (index_max), Lat.get (index_min), Long.get (index_min));
                                    points.add (location);
                                    //mMap.addMarker (new MarkerOptions ().position (location).title (String.valueOf (threshold)).flat (true));
                                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(location));


                                    // Add a circle in the bump
                                    Circle circle = mMap.addCircle (new CircleOptions ()
                                            .center (location)
                                            .radius (1)
                                            .strokeWidth (1)
                                            .strokeColor (Color.BLUE)
                                            .fillColor (Color.RED));
                                    circle.setTag (threshold);
                                    mMap.moveCamera (CameraUpdateFactory.newLatLng (location));
                                    mMap.animateCamera (CameraUpdateFactory.newLatLngZoom (location, 20));
                                }
                                windows_index = windows_index + 10;
                                max = 1;
                                min = 1;
                                index_max = j + 1;
                                index_min = j + 1;
                            }

                        } else
                            break;
                    }


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

    }






    private LatLng midpoint(double lat1,double lon1, double lat2,double lon2){
        double dLon=Math.toRadians (lon2-lon1);
        //covert to radians
        lat1=Math.toRadians (lat1);
        lat2=Math.toRadians (lat2);
        lon1=Math.toRadians (lon1);

        double Bx=Math.cos (lat2)* Math.cos (dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1)+Math.sin(lat2),Math.sqrt( (Math.cos(lat1)+Bx)*(Math.cos(lat1)+Bx) + By*By) );
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);
        LatLng midpoint=new LatLng (Math.toDegrees (lat3),Math.toDegrees (lon3));
        return midpoint;
    }

    private void checkIsGooglePlayConn() {
        Log.i("MapsActivity", "checkIsGooglePlayConn-->" + mGoogleApiClient.isConnected());
        if (mGoogleApiClient.isConnected() && mLastLocation != null) {
            startIntentService(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }
        mAddressRequested = true;
    }


    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(MapsActivity.this, LOCATION_PERMISSION_REQUEST_CODE,
                    android.Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        if (mLastLocation != null) {

            Log.i("MapsActivity", "Latitude-->" + String.valueOf(mLastLocation.getLatitude()));
            Log.i("MapsActivity", "Longitude-->" + String.valueOf(mLastLocation.getLongitude()));
        }
        if (lastLatLng != null)
            perth.setPosition(lastLatLng);
        checkIsGooglePlayConn();
        initCamera (lastLatLng);
        return false;
    }


    protected void startIntentService(LatLng latLng) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.LATLNG_DATA_EXTRA, latLng);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            Toast.makeText(getApplicationContext(), "Permission to access the location is missing.", Toast.LENGTH_LONG).show();
            return;
        }
        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("MapsActivity", "--onConnected--");
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permission to access the location is missing.", Toast.LENGTH_LONG).show();
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            lastLatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            displayPerth(true, lastLatLng);
            //initCamera(lastLatLng);
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, "No geocoder available", Toast.LENGTH_LONG).show();
                return;
            }
            if (mAddressRequested) {
                startIntentService(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            }
        }
    }

    private void displayPerth(boolean isDraggable, LatLng latLng) {
        if (perth == null) {
            perth = mMap.addMarker(new MarkerOptions().position(latLng).title("Your Position"));
            perth.setDraggable(isDraggable);
        }

    }



    private void initCamera(final LatLng sydney) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(800);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 20));
                    }
                });
            }
        }).start();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        perthLatLng = marker.getPosition();
        startIntentService(perthLatLng);
    }


    class AddressResultReceiver extends ResultReceiver {
        private String mAddressOutput;

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            mAddressOutput = resultData.getString(FetchAddressIntentService.RESULT_DATA_KEY);
            if (resultCode == FetchAddressIntentService.SUCCESS_RESULT) {
                Log.i("MapsActivity", "mAddressOutput-->" + mAddressOutput);
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle("Position")
                        .setMessage(mAddressOutput)
                        .create()
                        .show();
            }

        }
    }


}
