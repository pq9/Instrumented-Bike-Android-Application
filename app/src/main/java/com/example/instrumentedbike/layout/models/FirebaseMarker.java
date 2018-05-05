package com.example.instrumentedbike.layout.models;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 邱培杰 on 2018/3/13.
 */
@RequiresApi(api = Build.VERSION_CODES.N)
public class FirebaseMarker {

    public double latitude;
    public double longitude;
    public long currentTime = System.currentTimeMillis();
    public String time= stampToDate(currentTime);

    public double Acc1_x;
    public double Acc1_y;
    public double Acc1_z;

    public double Acc2_x;
    public double Acc2_y;
    public double Acc2_z;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String stampToDate(long timeMillis){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(timeMillis);
        return simpleDateFormat.format(date);
    }

    //required empty constructor
   public FirebaseMarker(){

   }

    public FirebaseMarker(double latitude, double longitude,double Acc1_x,double Acc1_y,double Acc1_z,double Acc2_x,double Acc2_y,double Acc2_z) {
        this.latitude = latitude;
        this.longitude = longitude;

        this.Acc1_x=Acc1_x;
        this.Acc1_y=Acc1_y;
        this.Acc1_z=Acc1_z;

        this.Acc2_x=Acc2_x;
        this.Acc2_y=Acc2_y;
        this.Acc2_z=Acc2_z;
    }


    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("longitude", longitude);
        result.put("latitude", latitude);

        result.put("Acc1_x",Acc1_x);
        result.put("Acc1_y",Acc1_y);
        result.put("Acc1_z",Acc1_z);

        result.put("Acc2_x",Acc1_x);
        result.put("Acc2_y",Acc1_y);
        result.put("Acc2_z",Acc2_z);
        result.put("time",time);
        return result;
    }
}

