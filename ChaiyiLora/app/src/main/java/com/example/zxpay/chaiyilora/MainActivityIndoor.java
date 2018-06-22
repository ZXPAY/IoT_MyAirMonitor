package com.example.zxpay.chaiyilora;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivityIndoor extends AppCompatActivity implements Button.OnClickListener{

    int ButtonID[] = {R.id.BT_QUERY, R.id.BT_BACK};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_indoor);

        for(int id:ButtonID){
            Button btn = (Button) findViewById(id);
            btn.setOnClickListener(this);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.BT_QUERY:
                Log.e("Query", "Indoor Query Data.");

                break;
            case R.id.BT_BACK:
                Intent intent_back = new Intent();
                intent_back.setClass(MainActivityIndoor.this, MainActivity.class);
                startActivity(intent_back);
                break;
        }
    }
}
