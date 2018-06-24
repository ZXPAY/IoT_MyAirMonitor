package com.example.zxpay.chaiyilora;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivityPlotData extends AppCompatActivity implements Button.OnClickListener{

    // Claim Line Graph and claim the parameters
    int number_to_show = 50;
    int Series_Point_Size = 5;
    boolean flag = false;
    int ButtonID[] = {R.id.BT_BACK};
    String Indoor_Or_Outdoor = "";
    String para_unit = "";
    String para_name = "";
    GraphView graph;
    TextView show_txv;
    String TXV_STRING = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_plot_data);
        // Orientation will not change when the cell phone tilt.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Cell phone will keep screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        for(int id:ButtonID){
            Button btn = (Button) findViewById(id);
            btn.setOnClickListener(this);
        }

        graph = (GraphView) findViewById(R.id.GRAPH);
        show_txv = (TextView) findViewById(R.id.SHOW);

        Intent it_from_IndoorQuery = getIntent();
        ArrayList<String> key_all = new ArrayList<>();
        ArrayList<String> value_all = new ArrayList<>();
        Indoor_Or_Outdoor = it_from_IndoorQuery.getStringExtra("database");
        key_all = it_from_IndoorQuery.getStringArrayListExtra("keys");
        value_all = it_from_IndoorQuery.getStringArrayListExtra("values");
        para_name = it_from_IndoorQuery.getStringExtra("parameter");
        para_unit = it_from_IndoorQuery.getStringExtra("unit");
        TXV_STRING = "參數 : " + para_name + "\n";
        TXV_STRING += "起始日期 : " +key_all.get(0).toString() + "\n"
                + "結束日期 : " + key_all.get(key_all.size()-1) + "\n";

        Draw(key_all, value_all);

    }

    private void Draw(ArrayList key_list, ArrayList value_list){
        int data_numbers = 0;
        float mean_data = 0;
        float max_data = 0;
        float min_data = 0;
        float sum_data = 0;
        float max_minus_min = 0;
        float data = 0;
        Date d1 = new Date();
        Date d2 = new Date();
        PointsGraphSeries<DataPoint> series_point = new PointsGraphSeries<DataPoint>();
        LineGraphSeries<DataPoint> series_line = new LineGraphSeries<DataPoint>();
        series_point.setSize(Series_Point_Size);
        if(flag){
            graph.removeAllSeries();
            Log.e("Remove", "Remove it.");
        }
        else{
            flag = true;
        }

        if(key_list.size()==value_list.size()){
            String mon, day;
            Date date = new Date();
            data_numbers = key_list.size();
            TXV_STRING += "資料個數 : " + String.valueOf(data_numbers) + "\n";
            for(int i=0;i<data_numbers;i++){
                mon = key_list.get(i).toString().substring(5,7);
                day = key_list.get(i).toString().substring(8,10);
                Log.e("Date", mon+"/"+day);
                SimpleDateFormat dateStringFormat = new SimpleDateFormat("MM/dd");
                try{
                    date = dateStringFormat.parse(mon+"/"+day);
                }
                catch (ParseException e){
                    Log.e("Error", "transfer to date error ...");
                    e.printStackTrace();
                }

                data = Float.parseFloat(value_list.get(i).toString());

                sum_data += data;
                if(i==0) {
                    d1 = date;
                    min_data = data;
                }
                else if(i==(data_numbers-1)) d2 = date;

                if(data >= max_data){
                    max_data = data;
                }
                else if(data <= min_data){
                    min_data = data;
                }
                series_point.appendData(new DataPoint(i, Float.parseFloat(value_list.get(i).toString()))
                        , true, number_to_show);
                series_line.appendData(new DataPoint(i, Float.parseFloat(value_list.get(i).toString()))
                        , true, number_to_show);
            }
            mean_data = sum_data/data_numbers;
            max_minus_min = max_data - min_data;
            TXV_STRING += "最大值 : " + String.format("%.2f", max_data) + para_unit + "\n";
            TXV_STRING += "最小值 : " + String.format("%.2f", min_data) + para_unit + "\n";
            TXV_STRING += "最大-最小 : " + String.format("%.2f", max_minus_min) + para_unit + "\n";
            TXV_STRING += "平均值 : " + String.format("%.2f", mean_data) + para_unit + "\n";

            graph.getViewport().setMinX(0);
            graph.getViewport().setMaxX(data_numbers);
            graph.getViewport().setXAxisBoundsManual(true);

            GridLabelRenderer gridLabel = graph.getGridLabelRenderer();
            gridLabel.setVerticalAxisTitle(para_unit);
            gridLabel.setVerticalLabelsAlign(Paint.Align.LEFT);
//            gridLabel.setLabelFormatter(new DateAsXAxisLabelFormatter(getApplicationContext()));
//            gridLabel.setNumHorizontalLabels(data_numbers);
//            gridLabel.setHumanRounding(false);
            graph.setTitle(Indoor_Or_Outdoor + " " + para_name);
            graph.setTitleTextSize(90);
            graph.setTitleColor(Color.RED);
            // Show the graph we draw
            graph.addSeries(series_point);
            graph.addSeries(series_line);
        }
        else {
            //Toast toast = Toast.makeText(getApplicationContext(), "資料長度布一樣", Toast.LENGTH_SHORT);
            Log.e("Error", "Data Length Not the same !!!");
        }
        show_txv.setText(TXV_STRING);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.BT_BACK:
                Intent intent_back = new Intent();
                if(Indoor_Or_Outdoor.equals("Indoor")){
                    intent_back.setClass(MainActivityPlotData.this, MainAdvanceIndoorQuery.class);
                    finish();
                }
                else if(Indoor_Or_Outdoor.equals("Outdoor")){
                    intent_back.setClass(MainActivityPlotData.this, MainAdvanceOutdoorQuery.class);
                    finish();
                }
                break;
        }
    }
}
