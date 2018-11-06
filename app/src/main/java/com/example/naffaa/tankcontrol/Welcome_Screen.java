package com.example.naffaa.tankcontrol;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/* This class handles the welcome screen when the app is opened.
   Everything in this class is for the appearance of the app when
   it is opened up, which means nothing in here affects the functionality
   of the program.
 */
public class Welcome_Screen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_welcome__screen);

        getSupportActionBar().hide();

        Button passListener = findViewById(R.id.enterPass); // button object on welcome screen
        final EditText password = findViewById(R.id.password); // textbox for entering a password
        final TextView attemptDisplay = findViewById(R.id.information); // shows the user how many attempts are left

        final String[] validPIN = {"1234"};

        passListener.setOnClickListener(new View.OnClickListener() { // when the button is pushed
            int attemptsUsed = 0; // gives the user 5 tries to enter a password
            public void onClick(View v) {

                String PIN = password.getText().toString(); // parse the number the user entered

                for(int i = 0; i < validPIN.length; i++){ // iterates through list of valid PINs to validate

                    if(PIN.equals("1234")){ // if the entered PIN is valid, enter the app
                        Intent intent = new Intent(Welcome_Screen.this, MainActivity.class);
                        startActivity(intent);
                        Welcome_Screen.this.finish();

                    // if the data is invalid, decrement the amount of tries and tell the user
                    } if(!PIN.equals("1234")) {
                        password.setText(""); // clear the attempted password
                        attemptDisplay.setTextColor(Color.YELLOW);
                        attemptsUsed++; // increment the amount of tries the user did
                        attemptDisplay.setText("Incorrect, you tried " + attemptsUsed + " times");
                    }

                }

            }
        });
    }
}

