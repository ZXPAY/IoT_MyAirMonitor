package com.example.zxpay.chaiyilora;

import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity implements Button.OnClickListener{
    int ButtonID[] = {R.id.BT_INDOOR, R.id.BT_OUTDOOR};
    int ImageID[] = {R.id.IMG_DATE_BACK, R.id.IMG_DATE_NEXT, R.id.IMG_INDOOR_ICON, R.id.IMG_OUTDOOR_ICON,
                    R.id.IMG_QUESTION, R.id.IMG_SETTING, R.id.IMG_SHARE, R.id.IMG_TITLE};
    Calendar myCalendar = Calendar.getInstance();
    DateFormat dateFormat;
    String year, month, day;
    TextView showDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Orientation will not change when the cell phone tilt.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Cell phone will keep screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#000000")));
        getSupportActionBar().setTitle(" ");

        showDate = (TextView) findViewById(R.id.TXV_SHOWDATE);
        showDate.setOnClickListener(this);
        for(int id:ButtonID){
            Button bt = (Button) findViewById(id);
            bt.setOnClickListener(this);
        }
        for(int id:ImageID){
            ImageView img = (ImageView) findViewById(id);
            img.setOnClickListener(this);
        }

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        year = dateFormat.format(myCalendar.getTime()).toString().substring(0,4);
        month = myCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
        day = dateFormat.format(myCalendar.getTime()).toString().substring(8,10);
        showDate.setText(month+"/"+day+"/"+year);

    }

    private void setShowDateTXV(){
        year = dateFormat.format(myCalendar.getTime()).toString().substring(0,4);
        month = myCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.ENGLISH);
        day = dateFormat.format(myCalendar.getTime()).toString().substring(8,10);
        showDate.setText(month+"/"+day+"/"+year);
    }

    private void setIndoorTextColor(){
        Button bt_indoor = (Button) findViewById(R.id.BT_INDOOR);
        Button bt_outdoor = (Button) findViewById(R.id.BT_OUTDOOR);
        bt_indoor.setTextColor(getResources().getColor(R.color.colorGreen));
        bt_outdoor.setTextColor(getResources().getColor(R.color.colorW));
    }
    private void setOutdoorTextColor(){
        Button bt_indoor = (Button) findViewById(R.id.BT_INDOOR);
        Button bt_outdoor = (Button) findViewById(R.id.BT_OUTDOOR);
        bt_indoor.setTextColor(getResources().getColor(R.color.colorW));
        bt_outdoor.setTextColor(getResources().getColor(R.color.colorGreen));
    }
    private String Get_Now(){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date today = Calendar.getInstance().getTime();
        return dateFormat.format(today).toString();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        MenuItem menuSearchItem = menu.findItem(R.id.my_search);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menuSearchItem.getActionView();

        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        // 這邊讓icon可以還原到搜尋的icon
        searchView.setIconifiedByDefault(true);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.MENU_SETTING:
                Log.e("Menu", "Team Setting");

                break;
            case R.id.MENU_TEAM_INTRO:
                Log.e("Menu", "Team introduce");

                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        Intent it_go;
        DateFormat dateFormats;
        Date today;
        String dateString;
        switch (v.getId()){
            case R.id.TXV_SHOWDATE:
                final int mYear = myCalendar.get(Calendar.YEAR);
                final int mMonth = myCalendar.get(Calendar.MONTH);
                final int mDay = myCalendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH, monthOfYear);
                                myCalendar.set(Calendar.DATE, dayOfMonth);
                                setShowDateTXV();
                                setClockTime();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

                break;
            case R.id.BT_INDOOR:
                Log.e("BT", "BT_indoor");
                setIndoorTextColor();
                dateFormats = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                today = myCalendar.getTime();
//                Log.e("Day", dateFormats.format(today).toString());
                dateString = dateFormats.format(today).toString();
                it_go = new Intent(this, IndoorActivity.class);
                it_go.putExtra("date", dateString);
                startActivityForResult(it_go, 123);
                break;
            case R.id.BT_OUTDOOR:
                Log.e("BT", "BT_outdoor");
                setOutdoorTextColor();
                dateFormats = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                today = myCalendar.getTime();
//                Log.e("Day", dateFormats.format(today).toString());
                dateString = dateFormats.format(today).toString();
                it_go = new Intent(this, OutdoorActivity.class);
                it_go.putExtra("date", dateString);
                startActivityForResult(it_go, 123);
                break;
            case R.id.IMG_DATE_BACK:
                Log.e("IMG", "IMG_DATE_BACK");
                myCalendar.add(Calendar.DATE,-1);
                setShowDateTXV();
                break;
            case R.id.IMG_DATE_NEXT:
                Log.e("IMG", "IMG_DATE_NEXT");
                myCalendar.add(Calendar.DATE,1);
                setShowDateTXV();
                break;
            case R.id.IMG_INDOOR_ICON:
                Log.e("IMG", "IMG_INDOOR_ICON");
                setIndoorTextColor();
                dateFormats = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                today = myCalendar.getTime();
//                Log.e("Day", dateFormats.format(today).toString());
                dateString = dateFormats.format(today).toString();
                it_go = new Intent(this, IndoorActivity.class);
                it_go.putExtra("date", dateString);
                startActivityForResult(it_go, 123);
                break;
            case R.id.IMG_OUTDOOR_ICON:
                Log.e("IMG", "IMG_OUTDOOR_ICON");
                setOutdoorTextColor();
                dateFormats = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                today = myCalendar.getTime();
//                Log.e("Day", dateFormats.format(today).toString());
                dateString = dateFormats.format(today).toString();
                it_go = new Intent(this, OutdoorActivity.class);
                it_go.putExtra("date", dateString);
                startActivityForResult(it_go, 123);
                break;
            case R.id.IMG_SHARE:
                Log.e("IMG", "IMG_SHARE");

                break;
            case R.id.IMG_SETTING:
                Log.e("IMG", "IMG_SETTING");

                break;
            case R.id.IMG_QUESTION:
                Log.e("IMG", "IMG_QUESTION");

                break;

        }
    }

    private void setClockTime(){
        // Use the current time as the default values for the picker
        final int hour = myCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = myCalendar.get(Calendar.MINUTE);
        // Create a new instance of TimePickerDialog and return it
        new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener(){

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                Log.e("Time",("現在時間是" + hourOfDay + ":" + minute));
                myCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                myCalendar.set(Calendar.MINUTE, minute);
                //advanced_update();
            }
        }, hour, minute, false).show();
    }


    private String Check_Two_Letters(String str_in){
        if(str_in.length()<2){
            return ("0"+str_in);
        }
        else{
            return str_in;
        }
    }
}
