package com.example.zxpay.chaiyilora;

    import android.app.DatePickerDialog;
    import android.app.TimePickerDialog;
    import android.content.Intent;
    import android.content.pm.ActivityInfo;
    import android.support.annotation.NonNull;
    import android.support.v7.app.AppCompatActivity;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.View;
    import android.view.WindowManager;
    import android.widget.Button;
    import android.widget.DatePicker;
    import android.widget.EditText;
    import android.widget.RadioGroup;
    import android.widget.TextView;
    import android.widget.TimePicker;
    import android.widget.Toast;

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
    import java.util.LinkedHashMap;
    import java.util.Map;


public class MainAdvanceIndoorQuery extends AppCompatActivity implements Button.OnClickListener,
        RadioGroup.OnCheckedChangeListener{

    private int ButtonID[] = {R.id.BT_BACK, R.id.BT_PLOT};
    boolean DEBUG = true;
    Map<String, String> Temp_Datai = new LinkedHashMap<String, String>();  // i => indoor
    Map<String, String> Humi_Datai = new LinkedHashMap<String, String>();
    Map<String, String> LPG_Data = new LinkedHashMap<String, String>();
    Map<String, String> CO_Data = new LinkedHashMap<String, String>();
    Map<String, String> CO2_Data = new LinkedHashMap<String, String>();
    Map<String, String> Pressure_Data = new LinkedHashMap<String, String>();
    Map<String, String> Loc_Data = new LinkedHashMap<String, String>();
    Map<String, String> Dust_Data = new LinkedHashMap<String, String>();

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
    boolean it_flag = false;

    RadioGroup mygroup;
    TextView show_data;
    Intent it_save;
    Toast toast;

    private EditText EditText_date;
    private int mYear, mMonth, mDay;

    String query_name = "Temperature";
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    MyQuery myQuery = new MyQuery();
    String choose_date="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_advance_indoor_query);
        // Orientation will not change when the cell phone tilt.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Cell phone will keep screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        for(int id:ButtonID){
            Button btn = (Button) findViewById(id);
            btn.setOnClickListener(this);
        }

        mygroup = (RadioGroup) findViewById(R.id.RADIO_GROUP);
        mygroup.setOnCheckedChangeListener(this);

        EditText_date = (EditText) findViewById(R.id.EDIT_DATE);
        EditText_date.setOnClickListener(this);

        show_data = (TextView) findViewById(R.id.TXV_SHOW);

        choose_date = Get_Now();

        EditText_date.setText(choose_date);
        advanced_update();
    }


    private String Get_Now(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today).toString();
    }


    private class MyQuery {
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
        boolean VERBOSE = false;

        /*
        This method can query data from firebase.
        key:parameter you want to query.
        operator:>,<,>=,== and so on, you can use these operator to find the data you want to query.
        object:key operator than what, for example temperature is key, > is operator, and value is object.
        num:how manny data you want to get
        direction:Ascending or Descending you can choose.
     */
        private void Query(CollectionReference ref, String key, String operator, String object, final int num, String direct) {
            Data_Array = new ArrayList<>();
            query_numbers = num;
            // Default Query
            Query myQuery = ref.whereGreaterThan(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
            ;
            if (operator.equals(">")) {
                if (direct.equals("ASCENDING"))
                    myQuery = ref.whereGreaterThan(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if (direct.equals("DESCENDING"))
                    myQuery = ref.whereGreaterThan(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            } else if (operator.equals(">=")) {
                if (direct.equals("ASCENDING"))
                    myQuery = ref.whereGreaterThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if (direct.equals("DESCENDING"))
                    myQuery = ref.whereGreaterThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            } else if (operator.equals("<")) {
                if (direct.equals("ASCENDING"))
                    myQuery = ref.whereLessThan(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if (direct.equals("DESCENDING"))
                    myQuery = ref.whereLessThan(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            } else if (operator.equals("<=")) {
                if (direct.equals("ASCENDING"))
                    myQuery = ref.whereLessThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if (direct.equals("DESCENDING"))
                    myQuery = ref.whereLessThanOrEqualTo(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            } else if (operator.equals("==")) {
                if (direct.equals("ASCENDING"))
                    myQuery = ref.whereEqualTo(key, object).limit(num).orderBy(key, Query.Direction.ASCENDING);
                else if (direct.equals("DESCENDING"))
                    myQuery = ref.whereEqualTo(key, object).limit(num).orderBy(key, Query.Direction.DESCENDING);
            }

            myQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        //Log.e("Query", "Success");
                        QuerySnapshot QSnap = task.getResult();
                        if (!QSnap.isEmpty()) {
                            for (int CNT = 0; CNT < num; CNT++) {
                                try {
                                    Log.e("Query Data", String.valueOf(task.getResult().getDocuments().get(CNT).getData()));
                                    String Data = String.valueOf(task.getResult().getDocuments().get(CNT).getData());
                                    Data_Array.add(Data);
                                }
                                catch (Exception e){
                                    Log.e("Query Data", "Error...");
                                    e.printStackTrace();
                                }
                            }
                            Run_Command();
                            Data_Array = new ArrayList<>();
                        }
                        else {
                            show_data.setText("未Query到資料");
                            it_flag = false;
                        }
                    }
                    else {
                        Log.e("Query", "Failure");
                    }
                }
            });
        }

        /*
            This method will run when query is finished.
            You can set mode to run the code.
         */
        private void Run_Command() {
            switch (mode) {
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
                    for (String data : Data_Array) {
                        Log.e("Data", data);
                        String data_split[] = data.substring(1, data.length() - 1).split(",");
                        for (String d : data_split) {
                            String each_data[] = d.split("=");
                            for (int i = 0; i < each_data.length; i++) {
                                try {
                                    //Log.e("data", each_data[i]);
                                    if (each_data[i].replaceAll("\\s", "").equals("TS")) {
                                        if (VERBOSE) Log.e("Temperature:", each_data[i + 1]);
                                        temp.add(each_data[i + 1]);
                                    }
                                    else if (each_data[i].replaceAll("\\s", "").equals("HS")) {
                                        if (VERBOSE) Log.e("Humidity:", each_data[i + 1]);
                                        humi.add(each_data[i + 1]);
                                    }
                                    else if (each_data[i].replaceAll("\\s", "").equals("CS")) {
                                        if (VERBOSE) Log.e("CO:", each_data[i + 1]);
                                        co.add(each_data[i + 1]);
                                    }
                                    else if (each_data[i].replaceAll("\\s", "").equals("IS")) {
                                        if (VERBOSE) Log.e("CO2:", each_data[i + 1]);
                                        co2.add(each_data[i + 1]);
                                    }
                                    else if (each_data[i].replaceAll("\\s", "").equals("AS")) {
                                        if (VERBOSE) Log.e("AS:", each_data[i + 1]);
                                        pressure.add(each_data[i + 1]);
                                    }
                                    else if (each_data[i].replaceAll("\\s", "").equals("MS")) {
                                        if (VERBOSE) Log.e("Dust:", each_data[i + 1]);
                                        dust.add(each_data[i + 1]);
                                    }
                                    else if (each_data[i].replaceAll("\\s", "").equals("LS")) {
                                        if (VERBOSE) Log.e("LPG:", each_data[i + 1]);
                                        lpg.add(each_data[i + 1]);
                                    }
                                    else if (each_data[i].replaceAll("\\s", "").equals("time")) {
                                        if (VERBOSE) Log.e("time", each_data[i + 1]);
                                        t.add(each_data[i + 1]);
                                    }
                                    else if (each_data[i].replaceAll("\\s", "").equals("DNLIST")) {
                                        if (VERBOSE) Log.e("DNS", each_data[i + 1]);
                                        loc.add(each_data[i + 1]);
                                    }
                                    else if (each_data[i].replaceAll("\\s", "").equals("rssi")) {
                                        if (VERBOSE) Log.e("rssi", each_data[i + 1]);
                                        rssi.add(each_data[i + 1]);
                                    }
                                }
                                catch (Exception e){
                                    Log.e("Error", "Split Data Error");
                                    e.printStackTrace();
                                }

                            }
                        }
                    }
                    if (!temp.isEmpty()) {
                        for (int i = 0; i < temp.size(); i++) {
                            Temp_Datai.put(t.get(i), temp.get(i));
                        }
                        advance_show(Temp_Datai, "溫度", "\u00b0"+"C");
                        Temp_Datai = new LinkedHashMap<String, String>();
                    }
                    if (!humi.isEmpty()) {
                        for (int i = 0; i < humi.size(); i++) {
                            Humi_Datai.put(t.get(i), humi.get(i));
                        }
                        advance_show(Humi_Datai, "濕度", "%");
                        Humi_Datai = new LinkedHashMap<String, String>();
                    }
                    if (!co.isEmpty()) {
                        for (int i = 0; i < co.size(); i++) {
                            CO_Data.put(t.get(i), co.get(i));
                        }
                        advance_show(CO_Data, "一氧化碳", "ppm");
                        CO_Data = new LinkedHashMap<String, String>();
                    }
                    if (!co2.isEmpty()) {
                        for (int i = 0; i < co2.size(); i++) {
                            CO2_Data.put(t.get(i), co2.get(i));
                        }
                        advance_show(CO2_Data, "二氧化碳", "ppm");
                        CO2_Data = new LinkedHashMap<String, String>();
                    }
                    if (!lpg.isEmpty()) {
                        for (int i = 0; i < lpg.size(); i++) {
                            LPG_Data.put(t.get(i), lpg.get(i));
                        }
                        advance_show(LPG_Data, "瓦斯", "ppm");
                        LPG_Data = new LinkedHashMap<String, String>();
                    }
                    if (!dust.isEmpty()) {
                        for (int i = 0; i < dust.size(); i++) {
                            Dust_Data.put(t.get(i), dust.get(i));
                        }
                        advance_show(Dust_Data, "空氣微粒", "\u00b5"+"g/m"+"\u00b3");
                        Dust_Data = new LinkedHashMap<String, String>();
                    }
                    if (!pressure.isEmpty()) {
                        for (int i = 0; i < pressure.size(); i++) {
                            Pressure_Data.put(t.get(i), pressure.get(i));
                        }
                        advance_show(Pressure_Data, "壓力", "pa");
                        Pressure_Data = new LinkedHashMap<String, String>();
                    }

                    break;
                case 2:

                    break;
            }

        }
    }

    private void advance_show(Map myparameter, String query_para, String query_unit){
        Log.e("Show", "Data Show");
        ArrayList<String> key_all = new ArrayList<>();
        ArrayList<String> value_all = new ArrayList<>();
        String msg = query_para+"\n";
        if(!myparameter.isEmpty()){
            for (Object key:myparameter.keySet() ) {
                key_all.add(key.toString());
                value_all.add(myparameter.get(key).toString());
                msg += key;
                msg += "\uD83D\uDC49";
                msg += myparameter.get(key);
                msg += query_unit;
                msg += "\n";
            }
            show_data.setText(msg);
            it_save = new Intent(this, MainActivityPlotData.class);
            it_save.putExtra("database", "Indoor");
            it_save.putExtra("keys", key_all);
            it_save.putExtra("values", value_all);
            it_save.putExtra("unit", query_unit);
            it_save.putExtra("parameter", query_name);
            it_save.putExtra("date", choose_date);
            it_flag = true;
        }
        else {
            it_flag = false;
            Log.e("Data", "No Show");
            show_data.setText("");
        }
    }

    private void advanced_update(){
        if(!choose_date.equals("")){
            Log.e("Update", "Update !!!");
            myQuery.mode = 1;
            myQuery.VERBOSE = false;
            CollectionReference ref = db.collection(UsingIndoor+query_name);
            myQuery.Query(ref, time_name, ">=", choose_date, 50, "ASCENDING");
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.BT_BACK:
                if(DEBUG) Log.e("Button", "Indoor Back Click");
                Intent intent_back = new Intent();
                intent_back.setClass(MainAdvanceIndoorQuery.this, MainActivity.class);
                finish();
                break;
            case R.id.BT_PLOT:
                if(it_flag){
                    startActivityForResult(it_save, 123);
                }
                else {
                    if(toast!=null){
                        toast.cancel();
                    }
                    toast = toast.makeText(getApplicationContext(), "No data found!!!Please try again..."
                            , Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.EDIT_DATE:
                if(DEBUG) Log.e("Button", "Edit Test Click");
                // Get Current Date
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                choose_date = String.valueOf(year)+"-"+
                                        Check_Two_Letters(String.valueOf(monthOfYear + 1)) +"-" +
                                        Check_Two_Letters(String.valueOf(dayOfMonth))+
                                        choose_date.substring(10,choose_date.length());
                                EditText_date.setText(choose_date);
                                advanced_update();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

                // Use the current time as the default values for the picker
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);
                // Create a new instance of TimePickerDialog and return it
                new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener(){

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Log.e("Time",("現在時間是" + hourOfDay + ":" + minute));
                        choose_date = choose_date.substring(0,11)+
                                Check_Two_Letters(String.valueOf(hourOfDay)) +":" +
                                Check_Two_Letters(String.valueOf(minute))+":"+
                                "00";
                        EditText_date.setText(choose_date);
                        //advanced_update();
                    }
                }, hour, minute, false).show();

                break;
        }
    }

    private String Check_Two_Letters(String str_in){
        if(str_in.length()<2){
            return ("0"+str_in);
        }
        else{
            return str_in;
        }
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.RBT_TEMP:
                if(DEBUG) Log.e("Radio Button", "Temperature");
                query_name = temp_name;
                advanced_update();
                break;
            case R.id.RBT_HUMIDITY:
                if(DEBUG) Log.e("Radio Button", "Humidity");
                query_name = humidity_name;
                advanced_update();
                break;
            case R.id.RBT_CO:
                if(DEBUG) Log.e("Radio Button", "CO");
                query_name = CO_name;
                advanced_update();
                break;
            case R.id.RBT_CO2:
                if(DEBUG) Log.e("Radio Button", "CO2");
                query_name = CO2_name;
                advanced_update();
                break;
            case R.id.RBT_LPG:
                if(DEBUG) Log.e("Radio Button", "LPG");
                query_name = LPG_name;
                advanced_update();
                break;
            case R.id.RBT_PM:
                if(DEBUG) Log.e("Radio Button", "PM");
                query_name = dust_name;
                advanced_update();
                break;
            case R.id.RBT_PRESSURE:
                if(DEBUG) Log.e("Radio Button", "Pressure");
                query_name = pressure_name;
                advanced_update();
                break;

        }
    }
}
