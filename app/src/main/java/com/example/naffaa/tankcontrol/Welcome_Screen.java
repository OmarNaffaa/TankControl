package com.example.naffaa.tankcontrol;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Welcome_Screen extends AppCompatActivity {

    private static int SCREEN_TIMEOUT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_welcome__screen);

        getSupportActionBar().hide();
        OpeningLauncher openingLauncher = new OpeningLauncher();
        openingLauncher.start();
    }

    private class OpeningLauncher extends Thread{
        public void run(){
            try{
                sleep(SCREEN_TIMEOUT * 1000);
            } catch(InterruptedException e){
                e.printStackTrace();
            }


            Intent intent = new Intent(Welcome_Screen.this, MainActivity.class);
            startActivity(intent);
            Welcome_Screen.this.finish();
        }

    }
}

