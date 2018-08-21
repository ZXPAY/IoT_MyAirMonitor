package com.example.zxpay.chaiyilora;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anastr.speedviewlib.Gauge;
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


public class IndoorActivity extends AppCompatActivity implements Button.OnClickListener{

    private TubeSpeedometer gaugeTemp, gaugeHumidity, gaugeCO, gaugeCO2, gaugePM, gaugeLPG;
    int BT_Img[] = {R.id.IMG_BACK, R.id.IMG_SET, R.id.IMG_LOVE};

    TextView TXVShow, TXVUpdateTime;
    LinearLayout myLinearlayout, LinearCO, LinearCO2, LinearPM, LinearLPG;

    int GaugeBarTextSize = 50;
    int GaugeTextSize = 80;
    int GaugeUnitSize = 60;
    Toast toast;

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

    int COWarning = 0;
    int CO2Warning = 0;
    int PMWarning = 0;
    int LPGWarning = 0;

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
        setContentView(R.layout.activity_indoor);
        // Orientation will not change when the cell phone tilt.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Cell phone will keep screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        show_data_type = CHOOSE_INDOOR;
        TXVShow = (TextView) findViewById(R.id.TXV_TITLE);
        TXVUpdateTime = (TextView) findViewById(R.id.TXV_UPDATE);
        myLinearlayout = (LinearLayout) findViewById(R.id.linearLayout_UP);
        LinearCO = (LinearLayout) findViewById(R.id.LinearLayoutCO);
        LinearCO2 = (LinearLayout) findViewById(R.id.LinearLayoutCO2);
        LinearPM = (LinearLayout) findViewById(R.id.LinearLayoutPM);
        LinearLPG = (LinearLayout) findViewById(R.id.LinearLayoutLPG);

        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        getSupportActionBar().setTitle("Indoor");
        getSupportActionBar().setLogo(R.drawable.icon_08x);

        Intent it_get = getIntent();
        String dateTime = it_get.getStringExtra("date");

        for(int id:BT_Img){
            ImageView img = (ImageView) findViewById(id);
            img.setOnClickListener(this);
        }

        setupGaugeTemp();
        setupGaugeHumidity();
        setupGaugeCO();
        setupGaugeCO2();
        setupGaugePM();
        setupGaugeLPG();

        setToastAndShow("Query "+dateTime);
        queryIndoorData(dateTime);
        setGoodInterface();

    }

    private void setupGaugeTemp(){
        gaugeTemp = (TubeSpeedometer) findViewById(R.id.GAUGE_Temp);
        gaugeTemp.setSpeedTextPosition(Gauge.Position.CENTER);
        gaugeTemp.setUnit("\u00b0"+"C");
        gaugeTemp.setSpeedTextSize(GaugeTextSize);
        gaugeTemp.setUnitTextSize(GaugeUnitSize);
        gaugeTemp.setTextSize(GaugeBarTextSize);
        gaugeTemp.speedTo(0);

    }
    private void setupGaugeHumidity(){
        gaugeHumidity = (TubeSpeedometer) findViewById(R.id.GAUGE_Humidity);
        gaugeHumidity.setSpeedTextPosition(Gauge.Position.CENTER);
        gaugeHumidity.setUnit("%");
        gaugeHumidity.setSpeedTextSize(GaugeTextSize);
        gaugeHumidity.setUnitTextSize(GaugeUnitSize);
        gaugeHumidity.setTextSize(GaugeBarTextSize);
        gaugeHumidity.speedTo(0);
    }
    private void setupGaugeCO(){
        gaugeCO = (TubeSpeedometer) findViewById(R.id.GAUGE_CO);
        gaugeCO.setSpeedTextPosition(Gauge.Position.CENTER);
        gaugeCO.setUnit("ppm");
        gaugeCO.setSpeedTextSize(GaugeTextSize);
        gaugeCO.setUnitTextSize(GaugeUnitSize);
        gaugeCO.setTextSize(GaugeBarTextSize);
        gaugeCO.speedTo(0);
    }
    private void setupGaugeCO2(){
        gaugeCO2 = (TubeSpeedometer) findViewById(R.id.GAUGE_CO2);
        gaugeCO2.setSpeedTextPosition(Gauge.Position.CENTER);
        gaugeCO2.setUnit("ppm");
        gaugeCO2.setSpeedTextSize(GaugeTextSize);
        gaugeCO2.setUnitTextSize(GaugeUnitSize);
        gaugeCO2.setTextSize(GaugeBarTextSize);
        gaugeCO2.speedTo(0);
    }
    private void setupGaugePM(){
        gaugePM = (TubeSpeedometer) findViewById(R.id.GAUGE_PM);
        gaugePM.setSpeedTextPosition(Gauge.Position.CENTER);
        gaugePM.setUnit("\u00b5"+"g/m"+"\u00b3");
        gaugePM.setSpeedTextSize(GaugeTextSize);
        gaugePM.setUnitTextSize(GaugeUnitSize);
        gaugePM.setTextSize(GaugeBarTextSize);
        gaugePM.speedTo(0);
    }
    private void setupGaugeLPG(){
        gaugeLPG = (TubeSpeedometer) findViewById(R.id.GAUGE_LPG);
        gaugeLPG.setSpeedTextPosition(Gauge.Position.CENTER);
        gaugeLPG.setUnit("ppm");
        gaugeLPG.setSpeedTextSize(GaugeTextSize);
        gaugeLPG.setUnitTextSize(GaugeUnitSize);
        gaugeLPG.setTextSize(GaugeBarTextSize);
        gaugeLPG.speedTo(0);
    }

    private void setGoodInterface(){
        myLinearlayout.setBackgroundColor(getResources().getColor(R.color.colorGood));
        TXVShow.setText("良好");
    }
    private void setSoSoInterface(){
        myLinearlayout.setBackgroundColor(getResources().getColor(R.color.colorSoSo));
        TXVShow.setText("正常");
    }
    private void setBadInterface(){
        myLinearlayout.setBackgroundColor(getResources().getColor(R.color.colorBad));
        TXVShow.setText("普通");
    }
    private void setDangerInterface(){
        myLinearlayout.setBackgroundColor(getResources().getColor(R.color.colorDanger));
        TXVShow.setText("危險");
    }

    private int getCOWarning(String dataSensor){
        if(Float.parseFloat(dataSensor)<10){
            return 0;
        }
        else if(Float.parseFloat(dataSensor)<20){
            return 1;
        }
        else if(Float.parseFloat(dataSensor)<50){
            return 2;
        }
        else if(Float.parseFloat(dataSensor)<100){
            return 3;
        }
        else{
            return 4;
        }
    }
    private int getCO2Warning(String dataSensor){
        if(Float.parseFloat(dataSensor)<600){
            return 0;
        }
        else if(Float.parseFloat(dataSensor)<800){
            return 1;
        }
        else if(Float.parseFloat(dataSensor)<1000){
            return 2;
        }
        else if(Float.parseFloat(dataSensor)<2500){
            return 3;
        }
        else{
            return 4;
        }
    }
    private int getPMWarning(String dataSensor){
        if(Float.parseFloat(dataSensor)<50){
            return 0;
        }
        else if(Float.parseFloat(dataSensor)<120){
            return 1;
        }
        else if(Float.parseFloat(dataSensor)<200){
            return 2;
        }
        else if(Float.parseFloat(dataSensor)<250){
            return 3;
        }
        else{
            return 4;
        }
    }

    private int getLPGWarning(String dataSensor){
        if(Float.parseFloat(dataSensor)<200){
            return 0;
        }
        else if(Float.parseFloat(dataSensor)<500){
            return 1;
        }
        else if(Float.parseFloat(dataSensor)<1000){
            return 2;
        }
        else if(Float.parseFloat(dataSensor)<1500){
            return 3;
        }
        else{
            return 4;
        }
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
                    if(!t.isEmpty()){
                        TXVUpdateTime.setText("資料儲存時間："+t.get(0));
                    }
                    if(!temp.isEmpty()){
                        if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<temp.size();i++){
                                Temp_Datai.put(t.get(i), temp.get(i));
                                //Log.e("Tempi", Temp_Datai.toString());
                                gaugeTemp.speedTo(Float.parseFloat(temp.get(i)));
                                details_array[0] = Float.parseFloat(temp.get(i));
                            }
                        }
                        else if(show_data_type == CHOOSE_OUTDOOR){
                            for(int i=0;i<temp.size();i++){
                                Temp_Datao.put(t.get(i), temp.get(i));
                                //Log.e("Tempo", Temp_Datao.toString());
                                gaugeTemp.speedTo(Float.parseFloat(temp.get(i)));
                                details_array[7] = Float.parseFloat(temp.get(i));
                            }
                        }
                    }
                    if(!humi.isEmpty()){
                        if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<humi.size();i++){
                                Humi_Datai.put(t.get(i), humi.get(i));
//                                Log.e("ef", humi.get(i));
                                gaugeHumidity.speedTo(Float.parseFloat(humi.get(i)));
                                details_array[1] = Float.parseFloat(humi.get(i));
                            }
                        }
                        else if(show_data_type == CHOOSE_OUTDOOR){
                            for(int i=0;i<humi.size();i++){
                                Humi_Datao.put(t.get(i), humi.get(i));
                                gaugeHumidity.speedTo(Float.parseFloat(humi.get(i)));
                                details_array[8] = Float.parseFloat(humi.get(i));
                            }

                        }
                    }
                    if(!co.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR){

                        }
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<co.size();i++){
                                CO_Data.put(t.get(i), co.get(i));
                                gaugeCO.speedTo(Float.parseFloat(co.get(i)));
                                details_array[2] = Float.parseFloat(co.get(i));
                                COWarning = getCOWarning(co.get(i));
                                if(COWarning==0){
                                    LinearCO.setBackgroundColor(Color.BLACK);
                                }
                                else if(COWarning==1){
                                    LinearCO.setBackgroundColor(getResources().getColor(R.color.colorSoSo));
                                }
                                else if(COWarning==2){
                                    LinearCO.setBackgroundColor(getResources().getColor(R.color.colorBad));
                                }
                                else{
                                    LinearCO.setBackgroundColor(getResources().getColor(R.color.colorDanger));
                                }
                            }
                        }
                    }
                    if(!co2.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR){

                        }
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<co2.size();i++){
                                CO2_Data.put(t.get(i), co2.get(i));
                                gaugeCO2.speedTo(Float.parseFloat(co2.get(i)));
                                details_array[3] = Float.parseFloat(co2.get(i));
                                CO2Warning = getCO2Warning(co2.get(i));
                                if(CO2Warning==0){
                                    LinearCO2.setBackgroundColor(Color.BLACK);
                                }
                                else if(CO2Warning==1){
                                    LinearCO2.setBackgroundColor(getResources().getColor(R.color.colorSoSo));
                                }
                                else if(CO2Warning==2){
                                    LinearCO2.setBackgroundColor(getResources().getColor(R.color.colorBad));
                                }
                                else{
                                    LinearCO2.setBackgroundColor(getResources().getColor(R.color.colorDanger));
                                }
                            }
                        }
                    }
                    if(!lpg.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR){

                        }
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<lpg.size();i++){
                                LPG_Data.put(t.get(i), lpg.get(i));
                                gaugeLPG.speedTo(Float.parseFloat(lpg.get(i)));
                                details_array[4] = Float.parseFloat(lpg.get(i));
                                LPGWarning = getLPGWarning(lpg.get(i));
                                if(LPGWarning==0){
                                    LinearLPG.setBackgroundColor(Color.BLACK);
                                }
                                else if(LPGWarning==1){
                                    LinearLPG.setBackgroundColor(getResources().getColor(R.color.colorSoSo));
                                }
                                else if(LPGWarning==2){
                                    LinearLPG.setBackgroundColor(getResources().getColor(R.color.colorBad));
                                }
                                else{
                                    LinearLPG.setBackgroundColor(getResources().getColor(R.color.colorDanger));
                                }
                            }
                        }
                    }
                    if(!dust.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR){
                            for(int i=0;i<dust.size();i++){
                                Dust_Datao.put(t.get(i), dust.get(i));
                                gaugePM.speedTo(Float.parseFloat(dust.get(i)));
                            }
                        }
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<dust.size();i++){
                                Dust_Data.put(t.get(i), dust.get(i));
                                gaugePM.speedTo(Float.parseFloat(dust.get(i)));
                                details_array[5] = Float.parseFloat(dust.get(i));
                                PMWarning = getPMWarning(dust.get(i));
                                if(PMWarning==0){
                                    LinearPM.setBackgroundColor(Color.BLACK);
                                }
                                else if(PMWarning==1){
                                    LinearPM.setBackgroundColor(getResources().getColor(R.color.colorSoSo));
                                }
                                else if(PMWarning==2){
                                    LinearPM.setBackgroundColor(getResources().getColor(R.color.colorBad));
                                }
                                else{
                                    LinearPM.setBackgroundColor(getResources().getColor(R.color.colorDanger));
                                }
                            }
                        }
                    }
                    if(!pressure.isEmpty()){
                        if(show_data_type == CHOOSE_OUTDOOR){
                            for(int i=0;i<pressure.size();i++){
                                Pressure_Datao.put(t.get(i), pressure.get(i));
                                //gauge_pressure.speedTo(Float.parseFloat(pressure.get(i)));
                            }
                        }
                        else if(show_data_type == CHOOSE_INDOOR){
                            for(int i=0;i<pressure.size();i++){
                                Pressure_Data.put(t.get(i), pressure.get(i));
                                //gauge_pressure.speedTo(Float.parseFloat(pressure.get(i)));
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

                    if(COWarning>=3||CO2Warning>=3||PMWarning>=3||LPGWarning>=3){
                        setDangerInterface();
                    }
                    else if(COWarning>=2||CO2Warning>=2||PMWarning>=2||LPGWarning>=2){
                        setBadInterface();
                    }
                    else if(COWarning>=1||CO2Warning>=1||PMWarning>=1||LPGWarning>=1){
                        setSoSoInterface();
                    }
                    else{
                        setGoodInterface();
                    }

                    break;
                case 2:

                    break;
            }

        }

    }

    private void queryIndoorData(String dateToQuery){
        myQuery.mode = 1;
        CollectionReference ref = db.collection(UsingIndoor+temp_name);
        myQuery.Query(ref, time_name, "<=", dateToQuery, 1, "DESCENDING");
        ref = db.collection(UsingIndoor+humidity_name);
        myQuery.Query(ref, time_name, "<=", dateToQuery, 1, "DESCENDING");
        ref = db.collection(UsingIndoor+CO_name);
        myQuery.Query(ref, time_name, "<=", dateToQuery, 1, "DESCENDING");
        ref = db.collection(UsingIndoor+CO2_name);
        myQuery.Query(ref, time_name, "<=", dateToQuery, 1, "DESCENDING");
        ref = db.collection(UsingIndoor+LPG_name);
        myQuery.Query(ref, time_name, "<=", dateToQuery, 1, "DESCENDING");
        ref = db.collection(UsingIndoor+dust_name);
        myQuery.Query(ref, time_name, "<=", dateToQuery, 1, "DESCENDING");

    }

    private String Get_Now(){
        DateFormat dateFormats = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        return dateFormats.format(today).toString();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.IMG_BACK:
                Intent it_back = new Intent(this, WelcomeActivity.class);
                startActivity(it_back);
                finish();
            break;
            case R.id.IMG_SET:

                break;
            case R.id.IMG_LOVE:

                break;
        }
    }

    private void setToastAndShow(String Words){
        if(toast != null){
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), Words, Toast.LENGTH_SHORT);
        toast.show();
    }
}
