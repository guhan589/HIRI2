package com.example.hiri;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Boot_2_Activity extends AppCompatActivity {
    Button button_blind, button_elder;
    boolean state = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot_2_);
        button_blind = findViewById(R.id.button_blind);
        button_elder = findViewById(R.id.button_elder);

        button_blind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = false;
            }
        });

        button_elder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
