package com.example.zxpay.chaiyilora;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivityDetails extends AppCompatActivity {

    Button btn_back;
    Bundle bundle;
    float[] details_array;
    LinearLayout linearLayout;
    TextView tv;
    String s = "";
    int warning= 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_details);
        // Orientation will not change when the cell phone tilt.
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Cell phone will keep screen on.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        linearLayout = findViewById(R.id.linearLayout);

        try{
            bundle = getIntent().getExtras();
            details_array = bundle.getFloatArray("details");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        s = s + "室內二氧化碳濃度";
        if(details_array[2] < 10000) s = s + "在正常範圍內\n";
        else
        {
            s = s + "已超出正常範圍\n";
            warning = warning+1;
        }
        s = s + "\n室內一氧化碳濃度";
        if(details_array[3] < 800) s = s + "在正常範圍內\n";
        else
        {
            s = s + "已超出正常範圍\n";
            warning = warning+1;
        }
        s = s + "\n室內瓦斯濃度";
        if(details_array[4] < 600) s = s + "在正常範圍內\n";
        else
        {
            s = s + "已超出正常範圍\n";
            warning = warning+1;
        }

        if (warning > 0) s = s + "\n\n*請將門窗打開通風,並檢查相關電器,必要時請撥打119或報警處理!!";

        tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(20);
        linearLayout.addView(tv);

    }
}
