package com.example.hiri;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class select_weatherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_weather);
        Button button1, button2;
        button1 = findViewById(R.id.button_my_weather);
        button2 = findViewById(R.id.button_total_weather);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//동네날씨
                Intent intent = new Intent(getApplicationContext(),show_myweatherActivity.class);
                startActivity(intent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),show_totalweatherActivity.class);
                startActivity(intent);
            }
        });
    }
}
