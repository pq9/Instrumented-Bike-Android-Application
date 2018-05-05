package com.example.instrumentedbike.LineChart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.instrumentedbike.Constant;
import com.example.instrumentedbike.R;
import com.example.instrumentedbike.Store_Firebase.TcpData;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.example.instrumentedbike.layout.LineChartEntity;
import com.example.instrumentedbike.layout.LineChartInViewPager;
import com.example.instrumentedbike.layout.NewMarkerView;
import com.example.instrumentedbike.layout.RealListEntity;
import com.example.instrumentedbike.layout.YoyListEntity;

import org.litepal.crud.DataSupport;
import org.litepal.crud.callback.FindMultiCallback;
import org.litepal.crud.callback.UpdateOrDeleteCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
/**
 * Created by 邱培杰 on 2018/3/13.
 */

public class LineChartActivity extends AppCompatActivity {
    private LineChart mChart;
    private DecimalFormat mFormat;
    private List<Entry> values1, values2;
    private List<RealListEntity> realList;
    private List<YoyListEntity> yoyList;
    private RealListEntity realListEntity;
    private YoyListEntity yoyListEntity;
    private boolean isRegisterBroadcast = false;
    private Button btnRemove;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linechart);
        initView();
        initData();
       // initFilter();
//        initViews();
    }

    private void initView() {
        btnRemove = (Button) findViewById(R.id.btn_remove);
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DataSupport.deleteAllAsync(TcpData.class).listen(new UpdateOrDeleteCallback() {
                    @Override
                    public void onFinish(int rowsAffected) {
                        Log.i("TCP", "Delete Eqiupment：" + rowsAffected);
                        realList.clear();
                        yoyList.clear();
                        initViews();
                    }
                });
            }
        });
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
               // showTost("服务启动成功");
            } else if (action.equals(Constant.BROADCAST_TCPSERVICE_READ)) {
                String data = intent.getStringExtra(Constant.DATA);
                String ip = intent.getStringExtra(Constant.DEVICE_IP);
                String[] info = data.split(",");
                if (info.length == 8) {
                    RealListEntity realListEntity = new RealListEntity();
                    YoyListEntity yoyListEntity = new YoyListEntity();
                    realListEntity.setAmount(info[4]);
                    realListEntity.setMonth(new Date().getTime() + "");
                    realListEntity.setYear("Accelerometer1");
                    realList.add(realListEntity);
                    yoyListEntity.setAmount(info[7]);
                    yoyListEntity.setMonth(new Date().getTime() + "");
                    yoyListEntity.setYear("Accelerometer2");
                    yoyList.add(yoyListEntity);
                    initViews();
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

    private void initData() {
        yoyList = new ArrayList<>();
        realList = new ArrayList<>();
        DataSupport.findAllAsync(TcpData.class).listen(new FindMultiCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public <T> void onFinish(List<T> t) {
                List<TcpData> tcpDataList = (List<TcpData>) t;
                for (TcpData tcpData : tcpDataList) {
                    String[] info = tcpData.getData().split(",");
                   if (info.length == 8) {
                        Log.d("Line", "Time Manifest:" + tcpData.getReleaseDate() + "\n" + tcpData.getReleaseDate());
                        RealListEntity realListEntity = new RealListEntity();
                        YoyListEntity yoyListEntity = new YoyListEntity();
                        realListEntity.setAmount(info[4]);
                        realListEntity.setMonth(tcpData.getReleaseDate().getTime()+"");
                        realListEntity.setYear("Accelerometer1");
                        realList.add(realListEntity);
                        yoyListEntity.setAmount(info[7]);
                        yoyListEntity.setMonth(tcpData.getReleaseDate().getTime()+"");
                        yoyListEntity.setYear("Accelerometer2");
                        yoyList.add(yoyListEntity);

                    }
                }
                initViews();
            }
        });
    }


    public void initViews() {
        mFormat = new DecimalFormat("#,###.##");
        mChart = (LineChartInViewPager) findViewById(R.id.chart);
        if (realList.size() == 0) {
            showTost("No Data");
            finish();
            return;
        }
        if (yoyList.size() == 0) {
            showTost("No Data");
            finish();
            return;
        }
        values1 = new ArrayList<>();
        values2 = new ArrayList<>();
        for (int i = 0; i < yoyList.size(); i++) {
            yoyListEntity = yoyList.get(i);
            String amount = yoyListEntity.getAmount();
            if (amount != null) {
                float f = 0;
                try {
                    f = Float.parseFloat(amount);
                } catch (Exception e) {
                    e.printStackTrace();
                    f = 0;
                }
                Entry entry = new Entry(i + 1, f);
                values1.add(entry);
            }
        }

        for (int i = 0; i < realList.size(); i++) {
            realListEntity = realList.get(i);
            String amount = realListEntity.getAmount();
            if (amount != null) {
                float f = 0;
                try {
                    f = Float.parseFloat(amount);
                } catch (Exception e) {
                    e.printStackTrace();
                    f = 0;
                }
                Entry entry = new Entry(i + 1, f);
                values2.add(entry);
            }
        }


        Drawable[] drawables = {
                ContextCompat.getDrawable(this, R.drawable.chart_thisyear_blue),
                ContextCompat.getDrawable(this, R.drawable.chart_callserice_call_casecount)
        };
        int[] callDurationColors = {Color.parseColor("#45A2FF"), Color.parseColor("#5fd1cc")};
        String thisYear = "";
        if (realList.size() > 0) {
            thisYear = realList.get(0).getYear();
        }

        String lastYear = "";
        if (yoyList.size() > 0) {
            lastYear = yoyList.get(0).getYear();
        }
        String[] labels = new String[]{thisYear, lastYear};
        updateLinehart(yoyList, realList, mChart, callDurationColors, drawables, "", values1, values2, labels);
    }

    private void updateLinehart(final List<YoyListEntity> yoyList, final List<RealListEntity> realList, LineChart lineChart, int[] colors, Drawable[] drawables,
                                final String unit, List<Entry> values2, List<Entry> values1, final String[] labels) {
        List<Entry>[] entries = new ArrayList[2];
        entries[0] = values1;
        entries[1] = values2;
        LineChartEntity lineChartEntity = new LineChartEntity(lineChart, entries, labels, colors, Color.parseColor("#999999"), 12f);
        lineChartEntity.drawCircle(true);
        lineChart.setScaleMinima(1.0f, 1.0f);
        toggleFilled(lineChartEntity, drawables, colors);

        /**
         * 这里切换平滑曲线或者折现图
         */
//        lineChartEntity.setLineMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineChartEntity.setLineMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
        lineChartEntity.initLegend(Legend.LegendForm.CIRCLE, 12f, Color.parseColor("#999999"));
        lineChartEntity.updateLegendOrientation(Legend.LegendVerticalAlignment.TOP, Legend.LegendHorizontalAlignment.RIGHT, Legend.LegendOrientation.HORIZONTAL);
        lineChartEntity.setAxisFormatter(
                new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        if (value == 1.0f) {
                            return mFormat.format(value);
                        }
                        String monthStr = mFormat.format(value);
                        if (monthStr.contains(".")) {
                            return "";
                        } else {
                            return monthStr;
                        }
//                        return mMonthFormat.format(value);
                    }
                },
                new IAxisValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, AxisBase axis) {
                        return mFormat.format(value) + unit;
                    }
                });

        lineChartEntity.setDataValueFormatter(new IValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return mFormat.format(value) + unit;
            }
        });

        final NewMarkerView markerView = new NewMarkerView(this, R.layout.custom_marker_view_layout);
        markerView.setCallBack(new NewMarkerView.CallBack() {
            @Override
            public void onCallBack(float x, String value) {
                int index = (int) (x);
                if (index < 0) {
                    return;
                }
                if (index > yoyList.size() && index > realList.size()) {
                    return;
                }
                String textTemp = "";

                if (index <= yoyList.size()) {
                    textTemp += yoyList.get(index - 1).getYear() + "." + index + "  " + mFormat.format(Float.parseFloat(yoyList.get(index - 1).getAmount())) + unit;
                }

                if (index <= realList.size()) {
                    textTemp += "\n";
                    textTemp += realList.get(index - 1).getYear() + "." + index + "  " + mFormat.format(Float.parseFloat(realList.get(index - 1).getAmount())) + unit;
                }
                markerView.getTvContent().setText(textTemp);
            }
        });
        lineChartEntity.setMarkView(markerView);
        lineChart.getData().setDrawValues(false);
    }

    /**
     * 双平滑曲线添加线下的阴影
     *
     * @param lineChartEntity
     * @param drawables
     * @param colors
     */
    private void toggleFilled(LineChartEntity lineChartEntity, Drawable[] drawables, int[] colors) {
        lineChartEntity.toggleFilled(drawables, null, true);
    }
}
