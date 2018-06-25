package com.example.zxpay.chaiyilora;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.AwesomeSpeedometer;
import com.github.anastr.speedviewlib.DeluxeSpeedView;
import com.github.anastr.speedviewlib.Gauge;
import com.github.anastr.speedviewlib.ImageLinearGauge;
import com.github.anastr.speedviewlib.LinearGauge;
import com.github.anastr.speedviewlib.ProgressiveGauge;
import com.github.anastr.speedviewlib.RaySpeedometer;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.TubeSpeedometer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/*
   12000008:
        Temperature
        Humidity
   12000009:
        Temperature
        Humidity
   12000006:
        Temperature
        Humidity
   12000007:
        Temperature
        Humidity
        Atmosphere
        CO
        CO2
        DNS
        LPG
        PM
*/
public class MainActivity extends AppCompatActivity implements Button.OnClickListener{
    int ButtonID[] = {R.id.BT_DATA_UPDATE, R.id.BT_INDOOR_LABEL,R.id.BT_OUTDOOR_LABEL
            ,R.id.BT_LOOK_DATA_DETAILS, R.id.BT_ADVANCE_QUERY};

    int OutDoor_TXVID[] = {R.id.TXV_CO, R.id.TXV_CO2, R.id.TXV_LPG};

    TextView outdoor_txv;
    TextView txv_show_time;

    final boolean DEBUG = true;

    private final int CHOOSE_INDOOR = 1;
    private final int CHOOSE_OUTDOOR = 0;

    private String UsingOutdoor = "/OutDoor/0000000012000007/";
    private String UsingIndoor = "/InDoor/0000000012000008/";

    private String time_name = "time";
    private String rssi_name = "rssi";
    private String temp_name = "Temperature";
    private String humidity_name = "Humidity";
    private String CO_name = "CO";
    private String CO2_name = "CO2";
    private String pressure_name = "Atmosphere";
    private String location_name = "DNS";
    private String LPG_name = "LPG";
    private String dust_name = "PM";
    private int show_data_type = 0; // 0 ；indoor , 1：outdoor


    // Newest data will be saved.
    Map<String, String> Indoor_Data = new HashMap<String, String>();
    Map<String, String> Outdoor_Data = new HashMap<String, String>();
    Map<String, String> Temp_Datai = new HashMap<String, String>();  // i => indoor
    Map<String, String> Humi_Datai = new HashMap<String, String>();
    Map<String, String> Temp_Datao = new HashMap<String, String>(); // o => outdoor
    Map<String, String> Humi_Datao = new HashMap<String, String>();
    Map<String, String> LPG_Data = new HashMap<String, String>();
    Map<String, String> CO_Data = new HashMap<String, String>();
    Map<String, String> CO2_Data = new HashMap<String, String>();
    Map<String, String> Pressure_Data = new HashMap<String, String>();
    Map<String, String> Pressure_Datao = new HashMap<String, String>();
    Map<String, String> Loc_Data = new HashMap<String, String>();
    Map<String, String> Dust_Data = new HashMap<String, String>();
    Map<String, String> Dust_Datao = new HashMap<String, String>();


    //TextView show_indoor, show_outdoor;
    Button btn_new_window;
    ImageLinearGauge gauge_humidity;
    ImageLinearGauge gauge_temperature;
    ProgressiveGauge gauge_co2;
    AwesomeSpeedometer gauge_co;
    SpeedView gauge_lpg;
    DeluxeSpeedView gauge_pm;
    RaySpeedometer gauge_pressure;
    Toast toast;

    ScrollView first_scrollview, second_scrollview;

    private long timei = System.currentTimeMillis();
    int polling_time = 500;   // Query 資料間隔 500 millis



    // keys for indoor and outdoor (the array define in strings.xml)
    String[] indoor_array;
    String[] outdoor_array;
    float[] details_array = {0f,0f,0f,0f,0f,0f,0f,0f,0f};

    // Access a Cloud Firestore instance from your Activity
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    MyQuery myQuery = new MyQuery();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Orientation will not change when the cell phone tilt.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Cell phone will keep screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Log.e("Now", Get_Now());   // now

        for(int id:ButtonID){
            Button bt = findViewById(id);
            bt.setOnClickListener(this);
        }
        indoor_array = getResources().getStringArray(R.array.Indoor_Data);
        outdoor_array = getResources().getStringArray(R.array.Outdoor_Data);
        txv_show_time = (TextView) findViewById(R.id.TXV_SHOW_TIME);

        myQuery.VERBOSE = false;
        show_data_type = CHOOSE_INDOOR;
        data_update();
        gauge_humidity = (ImageLinearGauge ) findViewById(R.id.GAUGE_HUMIDITY);
        gauge_humidity.setOrientation(LinearGauge.Orientation.VERTICAL);
        gauge_humidity.setSpeedTextPosition(Gauge.Position.TOP_CENTER);
        gauge_humidity.setUnitTextSize(50);
        gauge_humidity.setSpeedTextSize(50);
        gauge_humidity.setUnit("%");
        gauge_humidity.setMaxSpeed(100);
        gauge_humidity.setMinSpeed(0);

        gauge_temperature = (ImageLinearGauge) findViewById(R.id.GAUGE_TEMP);
        gauge_temperature.setOrientation(LinearGauge.Orientation.HORIZONTAL);
        gauge_temperature.setSpeedTextPosition(Gauge.Position.TOP_CENTER);
        gauge_temperature.setUnit("\u00b0"+"C");
        gauge_temperature.setMaxSpeed(50);
        gauge_temperature.setMinSpeed(0);
        gauge_temperature.setUnitTextSize(50);
        gauge_temperature.setSpeedTextSize(50);

        gauge_co2 = (ProgressiveGauge) findViewById(R.id.GAUGE_CO2);
        gauge_co2.setMaxSpeed(2000);
        gauge_co2.setMinSpeed(0);
        gauge_co2.setUnit("ppm");
        gauge_co2.setUnitTextSize(50);

        gauge_co = (AwesomeSpeedometer) findViewById(R.id.GAUGE_CO);
        gauge_co.setMaxSpeed(2000);
        gauge_co.setMinSpeed(0);
        gauge_co.setUnit("ppm");
        gauge_co.setUnitTextSize(50);

        gauge_lpg = (SpeedView) findViewById(R.id.GAUGE_LPG);
        gauge_lpg.setSpeedTextPosition(Gauge.Position.TOP_CENTER);
        gauge_lpg.setMaxSpeed(1000);
        gauge_lpg.setMinSpeed(0);
        gauge_lpg.setUnit("ppm");
        gauge_lpg.setUnitTextSize(50);

        gauge_pm = (DeluxeSpeedView) findViewById(R.id.GAUGE_PM);
        gauge_pm.setMaxSpeed(200);
        gauge_pm.setMinSpeed(0);
        gauge_pm.setUnit("\u00b5"+"g/m"+"\u00b3");
        gauge_pm.setUnitTextSize(50);

        gauge_pressure = (RaySpeedometer) findViewById(R.id.GAUGE_PRESSURE);
        gauge_pressure.setMaxSpeed(150000);
        gauge_pressure.setMinSpeed(10000);
        gauge_pressure.setUnit("pa");
        gauge_pressure.setUnitTextSize(50);

        first_scrollview = (ScrollView) findViewById(R.id.FIRST_SCROLLVIEW);
        second_scrollview = (ScrollView) findViewById(R.id.SECOND_SCROLLVIEW);

        Indoor_Mode();

    }

    private void Outdoor_Mode(){
        gauge_co.setVisibility(gauge_co.INVISIBLE);
        gauge_co2.setVisibility(gauge_co2.INVISIBLE);
        gauge_lpg.setVisibility(gauge_lpg.INVISIBLE);
        gauge_pm.setVisibility(gauge_pm.VISIBLE);
        gauge_pressure.setVisibility(gauge_pressure.VISIBLE);
        for(int id:OutDoor_TXVID){
            outdoor_txv = (TextView) findViewById(id);
            outdoor_txv.setVisibility(outdoor_txv.INVISIBLE);
        }
        Button Bt_outdoor = findViewById(R.id.BT_OUTDOOR_LABEL);
        Bt_outdoor.setBackgroundColor(getResources().getColor(R.color.colorG, null));
        Bt_outdoor.setTextColor(Color.RED);
        Bt_outdoor.setTypeface(Typeface.DEFAULT_BOLD);
        String htmlString="<u>"+Bt_outdoor.getText().toString()+"</u>";
        Bt_outdoor.setText(Html.fromHtml(htmlString));

        Button Bt_indoor = findViewById(R.id.BT_INDOOR_LABEL);
        Bt_indoor.setBackgroundColor(getResources().getColor(R.color.colorW, null));
        Bt_indoor.setTextColor(Color.BLACK);
        Bt_indoor.setTypeface(Typeface.DEFAULT);
        Bt_indoor.setText(Bt_indoor.getText().toString());
    }
    private  void Indoor_Mode(){
        gauge_co.setVisibility(gauge_co.VISIBLE);
        gauge_co2.setVisibility(gauge_co2.VISIBLE);
        gauge_lpg.setVisibility(gauge_lpg.VISIBLE);
        gauge_pm.setVisibility(gauge_pm.VISIBLE);
        gauge_pressure.setVisibility(gauge_pressure.VISIBLE);
        second_scrollview.setVisibility(second_scrollview.VISIBLE);
        for(int id:OutDoor_TXVID){
            outdoor_txv = (TextView) findViewById(id);
            outdoor_txv.setVisibility(outdoor_txv.VISIBLE);
        }
        Button Bt_indoor = findViewById(R.id.BT_INDOOR_LABEL);
        Bt_indoor.setBackgroundColor(getResources().getColor(R.color.colorG, null));
        Bt_indoor.setTextColor(Color.RED);
        Bt_indoor.setTypeface(Typeface.DEFAULT_BOLD);
        String htmlString="<u>"+Bt_indoor.getText().toString()+"</u>";
        Bt_indoor.setText(Html.fromHtml(htmlString));

        Button Bt_outdoor = findViewById(R.id.BT_OUTDOOR_LABEL);
        Bt_outdoor.setBackgroundColor(getResources().getColor(R.color.colorW, null));
        Bt_outdoor.setTextColor(Color.BLACK);
        Bt_outdoor.setTypeface(Typeface.DEFAULT);
        Bt_outdoor.setText(Bt_outdoor.getText().toString());
    }

    private String Get_Now(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today).toString();
    }

    private class MyQuery{
        /*
        Example below:
        CollectionReference ref = db.collection("/OutDoor/0000000012000007/CO2");
        MyQuery myquery = new MyQuery();
        myquery.mode = 1;
        myquery.Query(ref, "time", "<=", "2018-06-17 15:00:00", 10, "DESCENDING");
         */
        ArrayList<String> Data_Array = new ArrayList<>();
        int mode = 0;
        int query_numbers = 0;
        boolean VERBOSE = true;

        /*
        This method can query data from firebase.
        key:parameter you want to query.
        operator:>,<,>=,== and so on, you can use these operator to find the data you want to query.
        object:key operator than what, for example temperature is key, > is operator, and value is object.
        num:how manny data you want to get
        direction:Ascending or Descending you can choose.
     */
        private void Query(CollectionReference ref, String key, String operator, String object, final int num, String direct){
            Data_Array = new ArrayList<>();
            query_numbers = num;
            // Default Query
            Query myQuery = ref.whereGreaterThan(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);;
            if(operator.equals(">")){
                if(direct.equals("ASCENDING"))
                    myQuery = ref.whereGreaterThan(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if(direct.equals("DESCENDING"))
                    myQuery = ref.whereGreaterThan(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }
            else if(operator.equals(">=")){
                if(direct.equals("ASCENDING"))
                    myQuery = ref.whereGreaterThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if(direct.equals("DESCENDING"))
                    myQuery = ref.whereGreaterThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }
            else if(operator.equals("<")){
                if(direct.equals("ASCENDING"))
                    myQuery = ref.whereLessThan(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if(direct.equals("DESCENDING"))
                    myQuery = ref.whereLessThan(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }
            else if(operator.equals("<=")){
                if(direct.equals("ASCENDING"))
                    myQuery = ref.whereLessThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if(direct.equals("DESCENDING"))
                    myQuery = ref.whereLessThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }
            else if(operator.equals("==")){
                if(direct.equals("ASCENDING"))
                    myQuery = ref.whereEqualTo(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if(direct.equals("DESCENDING"))
                    myQuery = ref.whereEqualTo(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }

            myQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        //Log.e("Query", "Success");
                        QuerySnapshot QSnap = task.getResult();
                        if(!QSnap.isEmpty()){
                            for(int CNT=0;CNT<num;CNT++){
                                try {
                                    Log.e("Query Data", String.valueOf(task.getResult().getDocuments().get(CNT).getData()));
                                    String Data = String.valueOf(task.getResult().getDocuments().get(CNT).getData());
                                    Data_Array.add(Data);
                                }
                                catch (Exception e){
                                    Log.e("Query Data", "Error");
                                    e.printStackTrace();
                                }
                            }
                            Run_Command();
                            Data_Array = new ArrayList<>();
                        }
                    }
                    else{
                        Log.e("Query", "Failure");
                    }
                }
            });
        }

        /*
            This method will run when query is finished.
            You can set mode to run the code.
         */
        private void Run_Command(){
            switch (mode){
                case 0:
                    Log.e("Run", "Start~~~");
                    break;
                case 1:
                    ArrayList<String> temp = new ArrayList<>();
                    ArrayList<String> humi = new ArrayList<>();
                    ArrayList<String> co = new ArrayList<>();
                    ArrayList<String> co2 = new ArrayList<>();
                    ArrayList<String> lpg = new ArrayList<>();
                    ArrayList<String> pressure = new ArrayList<>();
                    ArrayList<String> loc = new ArrayList<>();
                    ArrayList<String> dust = new ArrayList<>();
                    ArrayList<String> t = new ArrayList<>();
                    ArrayList<String> rssi = new ArrayList<>();
                    for(String data:Data_Array){
                        //Log.e("Data", data);
                        String data_split[] = data.substring(1,data.length()-1).split(",");
                        for(String d:data_split){
                            String each_data[] = d.split("=");
                            for(int i=0;i<each_data.length;i++){
                                try {
                                    //Log.e("data", each_data[i]);
                                    if(each_data[i].replaceAll("\\s", "").equals("TS")){
                                        if(VERBOSE) Log.e("Temperature:", each_data[i+1]);
                                        temp.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals("HS")){
                                        if(VERBOSE) Log.e("Humidity:", each_data[i+1]);
                                        humi.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals("CS")){
                                        if(VERBOSE) Log.e("CO:", each_data[i+1]);
                                        co.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals("IS")){
                                        if(VERBOSE) Log.e("CO2:", each_data[i+1]);
                                        co2.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals("AS")){
                                        if(VERBOSE) Log.e("AS:", each_data[i+1]);
                                        pressure.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals("MS")){
                                        if(VERBOSE) Log.e("Dust:", each_data[i+1]);
                                        dust.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals("LS")){
                                        if(VERBOSE) Log.e("LPG:", each_data[i+1]);
                                        lpg.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals("time")){
                                        if(VERBOSE) Log.e("time", each_data[i+1]);
                                        t.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals("DNLIST")){
                                        if(VERBOSE) Log.e("DNS", each_data[i+1]);
                                        loc.add(each_data[i+1]);
                                    }
                                    else if(each_data[i].replaceAll("\\s", "").equals("rssi")){
                                        if(VERBOSE) Log.e("rssi", each_data[i+1]);
                                        rssi.add(each_data[i+1]);
                                    }
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                    if(!temp.isEmpty()){
                        if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<temp.size();i++){
                                Temp_Datai.put(t.get(i), temp.get(i));
                                //Log.e("Tempi", Temp_Datai.toString());
                                gauge_temperature.speedTo(Float.parseFloat(temp.get(i)));
                                details_array[0] = Float.parseFloat(temp.get(i));
                            }
                        }
                        else if(show_data_type == CHOOSE_OUTDOOR){
                            for(int i=0;i<temp.size();i++){
                                Temp_Datao.put(t.get(i), temp.get(i));
                                //Log.e("Tempo", Temp_Datao.toString());
                                gauge_temperature.speedTo(Float.parseFloat(temp.get(i)));
                                details_array[7] = Float.parseFloat(temp.get(i));
                            }
                        }
                    }
                    if(!humi.isEmpty()){
                        if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<humi.size();i++){
                                Humi_Datai.put(t.get(i), humi.get(i));
//                                Log.e("ef", humi.get(i));
                                gauge_humidity.speedTo(Float.parseFloat(humi.get(i)));
                                details_array[1] = Float.parseFloat(humi.get(i));
                            }
                        }
                        else if(show_data_type == CHOOSE_OUTDOOR){
                            for(int i=0;i<humi.size();i++){
                                Humi_Datao.put(t.get(i), humi.get(i));
                                gauge_humidity.speedTo(Float.parseFloat(humi.get(i)));
                                details_array[8] = Float.parseFloat(humi.get(i));
                            }

                        }
                    }
                    if(!co.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR);
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<co.size();i++){
                                CO_Data.put(t.get(i), co.get(i));
                                gauge_co.speedTo(Float.parseFloat(co.get(i)));
                                details_array[2] = Float.parseFloat(co.get(i));
                            }
                        }
                    }
                    if(!co2.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR);
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<co2.size();i++){
                                CO2_Data.put(t.get(i), co2.get(i));
                                gauge_co2.speedTo(Float.parseFloat(co2.get(i)));
                                details_array[3] = Float.parseFloat(co2.get(i));
                            }
                        }
                    }
                    if(!lpg.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR);
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<lpg.size();i++){
                                LPG_Data.put(t.get(i), lpg.get(i));
                                gauge_lpg.speedTo(Float.parseFloat(lpg.get(i)));
                                details_array[4] = Float.parseFloat(lpg.get(i));
                            }
                        }
                    }
                    if(!dust.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR){
                            for(int i=0;i<dust.size();i++){
                                Dust_Datao.put(t.get(i), dust.get(i));
                                gauge_pm.speedTo(Float.parseFloat(dust.get(i)));
                            }
                        }
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<dust.size();i++){
                                Dust_Data.put(t.get(i), dust.get(i));
                                gauge_pm.speedTo(Float.parseFloat(dust.get(i)));
                                details_array[5] = Float.parseFloat(dust.get(i));
                            }
                        }
                    }
                    if(!pressure.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR){
                            for(int i=0;i<pressure.size();i++){
                                Pressure_Datao.put(t.get(i), pressure.get(i));
                                gauge_pressure.speedTo(Float.parseFloat(pressure.get(i)));
                            }
                        }
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<pressure.size();i++){
                                Pressure_Data.put(t.get(i), pressure.get(i));
                                gauge_pressure.speedTo(Float.parseFloat(pressure.get(i)));
                                details_array[6] = Float.parseFloat(pressure.get(i));
                            }
                        }
                    }
                    if(!loc.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR);
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<loc.size();i++){
                                Loc_Data.put(t.get(i), loc.get(i));
                            }

                        }
                    }
                    break;
                case 2:

                    break;
            }

        }

    }

    private void data_update(){
        if(show_data_type == CHOOSE_INDOOR){
            myQuery.mode = 1;
            CollectionReference ref = db.collection(UsingIndoor+temp_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            ref = db.collection(UsingIndoor+humidity_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            ref = db.collection(UsingIndoor+CO_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            ref = db.collection(UsingIndoor+CO2_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            ref = db.collection(UsingIndoor+LPG_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            ref = db.collection(UsingIndoor+dust_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            ref = db.collection(UsingIndoor+pressure_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
//            ref = db.collection(UsingIndoor+location_name);
//            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            txv_show_time.setText("最後更新時間 : " + Get_Now());
        }
        else if(show_data_type == CHOOSE_OUTDOOR){
            myQuery.mode = 1;
            CollectionReference ref = db.collection(UsingOutdoor+temp_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            ref = db.collection(UsingOutdoor+humidity_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
//            ref = db.collection(UsingOutdoor+CO_name);
//            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
//            ref = db.collection(UsingOutdoor+CO2_name);
//            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
//            ref = db.collection(UsingOutdoor+LPG_name);
//            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            ref = db.collection(UsingOutdoor+dust_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            ref = db.collection(UsingOutdoor+pressure_name);
            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
//            ref = db.collection(UsingOutdoor+location_name);
//            myQuery.Query(ref, time_name, "<=", Get_Now(), 1, "DESCENDING");
            txv_show_time.setText("最後更新時間 : " + Get_Now());
        }
        else{
            Log.e("show_data_type", "Not found Query show_data_type.");
        }
    }

    /*
        mode 0 : Showing recent indoor data or outdoor data.
     */

    private String PrepareShowData(Map data){
        String Reture_MSG = "";
        if(!data.isEmpty()){
            try{
                //            Log.e("Pre", data.toString());
//            Log.e("Pre Length", String.valueOf(data.toString().length()));
                Reture_MSG = data.toString().replace("}"," ").substring(1,Temp_Datai.toString().length()-1);
                Reture_MSG = Reture_MSG.replace("=", "\uD83D\uDC49");
                //Log.e("Return MSG", Reture_MSG);
            }
            catch (Exception e){
                Log.e("Error", "Prepare data error");
                e.printStackTrace();
            }
        }
        return Reture_MSG;
    }
//    private void show_data(int mode){
//        switch (mode){
//            case 0:
//                String msg = "";
//                String pre_msg;
//                if(show_data_type == CHOOSE_INDOOR){
//                    msg+="溫度 Temperature\n";
//                    pre_msg = PrepareShowData(Temp_Datai);
//                    msg += pre_msg+" \u00b0"+"C"+"\n";
//                    msg+="濕度 Humidity\n";
//                    pre_msg = PrepareShowData(Humi_Datai);
//                    msg+=pre_msg+" %";
//                }
//                else if(show_data_type == CHOOSE_OUTDOOR){
//                    msg+="溫度 Temperature\n";
//                    pre_msg = PrepareShowData(Temp_Datao);
//                    msg += pre_msg+" \u00b0"+"C"+"\n";
//                    msg+="濕度 Humidity\n";
//                    pre_msg = PrepareShowData(Humi_Datao);
//                    msg+=pre_msg+" %\n";
//                    msg+="一氧化碳 Carbon Monoxide\n";
//                    pre_msg = PrepareShowData(CO_Data);
//                    msg+=pre_msg+" ppm\n";
//                    msg+="二氧化碳 Carbon Dioxide\n";
//                    pre_msg = PrepareShowData(CO2_Data);
//                    msg+=pre_msg+" ppm\n";
//                    msg+="瓦斯 LPG\n";
//                    pre_msg = PrepareShowData(LPG_Data);
//                    msg+=pre_msg+" ppm\n";
//                    msg+="空氣微粒 PM\n";
//                    pre_msg = PrepareShowData(Dust_Data);
//                    msg+=pre_msg+"\u00b5"+"g/m"+"\u00b3"+"\n";
//                    msg+="大氣壓力 Pressure\n";
//                    pre_msg = PrepareShowData(Pressure_Data);
//                    msg+=pre_msg+" pa";
////                    msg+="經緯度 Location\n";
////                    pre_msg = PrepareShowData(Loc_Data);
////                    msg+=pre_msg+"";
//                }
//                //Log.e("MSG",msg);
////                show_data.setText(msg);
//                break;
//            case 1:
//
//                break;
//
//                default:
//
//                    break;
//
//        }
//
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.BT_DATA_UPDATE:
                if((System.currentTimeMillis() - timei) > polling_time){
                    Log.e("Button", "Data Update ...");
                    data_update();
                    timei = System.currentTimeMillis();
                }
                else {
                    if(toast != null){
                        toast.cancel();
                    }
                    toast = Toast.makeText(getApplicationContext(), "Please Query data later...", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;

            case R.id.BT_INDOOR_LABEL:
                if((System.currentTimeMillis() - timei) > polling_time){
                    try {
                        if(DEBUG) Log.e("Button", "BT_Indoor");
                        show_data_type = CHOOSE_INDOOR;
                        Indoor_Mode();
                        data_update();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    timei = System.currentTimeMillis();
                }
                else {
                    if(toast != null){
                        toast.cancel();
                    }
                    toast = Toast.makeText(getApplicationContext(), "Please Query data later...", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.BT_OUTDOOR_LABEL:
                if((System.currentTimeMillis() - timei) > polling_time){
                    try {
                        if(DEBUG) Log.e("Button", "BT_Outdoor");
                        show_data_type = CHOOSE_OUTDOOR;
                        Outdoor_Mode();
                        data_update();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    timei = System.currentTimeMillis();
                }
                else {
                    if(toast != null){
                        toast.cancel();
                    }
                    toast = Toast.makeText(getApplicationContext(), "Please Query data later...", Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.BT_LOOK_DATA_DETAILS:
                if(DEBUG) Log.e("Button", "BT_Detail");
                Intent intent_details = new Intent();
                intent_details.setClass(MainActivity.this, MainActivityDetails.class);
                Bundle bundle_details = new Bundle();
                bundle_details.putFloatArray("details",details_array);
                intent_details.putExtras(bundle_details);
                startActivity(intent_details);
                break;
            case R.id.BT_ADVANCE_QUERY:
                if(DEBUG) Log.e("Button", "BT Advance Query");
                if(show_data_type == CHOOSE_INDOOR){
                    Intent intent_indoor = new Intent();
                    intent_indoor.setClass(MainActivity.this, MainAdvanceIndoorQuery.class);
                    startActivity(intent_indoor);
                }
                else if(show_data_type == CHOOSE_OUTDOOR){
                    Intent intent_outdoor = new Intent();
                    intent_outdoor.setClass(MainActivity.this, MainAdvanceOutdoorQuery.class);
                    startActivity(intent_outdoor);
                }
                break;
            default:
                Log.e("Button", "No button Click Found !!!");
                break;
        }
    }

//    public void Query_Test(View view){
//        Log.e("Button", "Click");
//        CollectionReference ref = db.collection("/OutDoor/0000000012000007/CO2");
//        MyQuery a = new MyQuery();
//        a.mode = 1;
//        a.Query(ref, "time", "<=", "2018-06-17 15:00:00", 10, "DESCENDING");
//        Log.e("Run", "OK");
//    }

}
