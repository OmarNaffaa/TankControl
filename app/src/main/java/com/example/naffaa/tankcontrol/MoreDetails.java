package com.example.naffaa.tankcontrol;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import static java.lang.Math.round;

public class MoreDetails extends AppCompatActivity {

    // URL of the ThingSpeak channel (filled in onCreate method)
    String server_url;

    // sets the size of the array based on the amount of data that is being retrieved
    final int SIZE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_details);
        getSupportActionBar().hide();

        // ensures the screen is always in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get the url that was passed from the MainActivity
        Bundle bundle = getIntent().getExtras();
        server_url = bundle.getString("url");

        getDetails();
    }

    private void getDetails(){

        // formatting for numbers set the decimal to up to 2 places
        final DecimalFormat df = new DecimalFormat("0.0");

        // water data text fields
        final TextView waterLvlOne = findViewById(R.id.tankOneLvl);
        final TextView tankOneProg = findViewById(R.id.tankOneProgress);
        final TextView waterLvlTwo = findViewById(R.id.tankTwoLvl);
        final TextView tankTwoProg = findViewById(R.id.tankTwoProgress);
        final TextView tankHeight  = findViewById(R.id.tankHeight);

        // power data text fields
        final TextView currentValue = findViewById(R.id.cVal);
        final TextView voltageValue = findViewById(R.id.vVal);

        // create a new JSON object request that will be sent to the queue in the MySingleton class
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, server_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            // iterate through the general object request to the JSON object that
                            // is holding our values
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);

                            // array storing the data taken from ThingSpeak
                            String[] mDataSet = new String[SIZE];
                            float[] numDataSet = new float[SIZE];

                            float HEIGHT = 36; //Adjust height based on size of tank

                            mDataSet[0] = inner.getString("field5") + " "; // Tank 1 Water Level
                            mDataSet[1] = inner.getString("field8") + " "; // Tank 2 Water Level
                            mDataSet[2] = inner.getString("field6") + " "; // current
                            mDataSet[3] = inner.getString("field7") + " "; // voltage

                            // attempt to parse the data from string to float
                            float errorValue = 100000;

                            for(int i = 0; i < SIZE; i++){

                                // attempt to parse string from ThingSpeak to a float
                                try{ numDataSet[i] = Float.parseFloat(mDataSet[i]); }
                                // if a null value is detected, place error value in the array
                                catch(Exception e){ numDataSet[i] = errorValue; }

                            }

                            String errorString = "No Data Found";
                            // set the value for the percentage of the tank filled if data is found
                            if(numDataSet[0] != errorValue) {
                                tankOneProg.setText(df.format(numDataSet[0]) + " % filled");
                                waterLvlOne.setText(df.format((numDataSet[0] / 100) * HEIGHT) + " cm");
                            }
                            else {
                                tankOneProg.setText(errorString);
                                waterLvlOne.setText(errorString);
                            }

                            if(numDataSet[1] != errorValue){
                                tankTwoProg.setText(df.format(numDataSet[1]) + " % filled");
                                waterLvlTwo.setText(df.format((numDataSet[1] / 100) * HEIGHT) + " cm");
                            }
                            else {
                                tankTwoProg.setText(errorString);
                                waterLvlTwo.setText(errorString);
                            }

                            // set the value of the current and the voltage
                            if(numDataSet[2] != errorValue) currentValue.setText(df.format(numDataSet[2]) + " A");
                            else currentValue.setText(errorString);

                            if(numDataSet[3] != errorValue) voltageValue.setText(df.format(numDataSet[3]) + " V");
                            else voltageValue.setText(errorString);

                            // set the value for the height of the tank
                            tankHeight.setText(df.format(HEIGHT) + " cm");

                        }
                        catch (JSONException e) // catches exceptions related to making the JSON object request
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                // if an error is caught, display "Connection Error" on the toast at the bottom of the screen
                Toast.makeText(MoreDetails.this, "Connection Error", Toast.LENGTH_SHORT).show();

            }
        });

        // adds the JSON object requests to the queue located in the MySingleton class
        MySingleton.getInstance(MoreDetails.this).addToRequestQueue(objectRequest);

    }

    // refreshes the data when the activity is active every 5 seconds
    Handler h = new Handler();
    Runnable r;
    int delay = 1000 * 1; // time delay

    @Override
    protected void onResume(){ // when the activity is active refresh every x seconds

        h.postDelayed(r = new Runnable() {
            @Override
            public void run() {
                //getDetails();
                h.postDelayed(r, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause(){ // if the activity isn't active stop calling getData
        h.removeCallbacks(r);
        super.onPause();
    }

    }