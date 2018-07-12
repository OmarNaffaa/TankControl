package com.example.naffaa.tankcontrol;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ToggleButton toggleMotor = findViewById(R.id.motorToggle);
        ToggleButton toggleValve = findViewById(R.id.valveToggle);
        Button powerButton = findViewById(R.id.powerButton);
    }

    // event listener for this method is controlled by the powerButton
    // in the XML sheet
    public void PowerDetails(View v){
        Intent intent = new Intent(MainActivity.this, PowerActivity.class);
        startActivity(intent);
    }

}
