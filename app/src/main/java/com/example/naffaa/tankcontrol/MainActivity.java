package com.example.naffaa.tankcontrol;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // URL of the ThingSpeak channel for the sensor data
    String sensor_read_key = "BAY5Y9HPFP6V3C6G";
    String sensor_server_url =
            "https://api.thingspeak.com/channels/544573/feeds.json?api_key=" + sensor_read_key + "&results=1";

    // declare lists to store titles and date retrieved
    ArrayList<String> titles = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // configurations for the app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // ensures the screen is always in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        InitializeTitle();
        GetSensorData();
    }

    final int arraySize = 8;
    final float HEIGHT = 36; // height of tank (Constant, hard coded in)

    // retreives data from a ThingSpeak channel that contains sensor information
    private void GetSensorData(){
        // formatting for numbers set the decimal to up to 2 places
        final DecimalFormat df = new DecimalFormat("0.0");

        // create a new JSON object request that will be sent to the queue in the MySingleton class
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, sensor_server_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            ArrayList<String> mDataValues = new ArrayList<>();

                            // iterate through the general object request to the JSON object that
                            // is holding our values
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);

                            // gets each field from ThingSpeak

                            String[] values = new String[arraySize];
                            float[] floatValues = new float[arraySize];

                            values[0] = inner.getString("field1"); // temperature
                            values[1] = inner.getString("field2"); // conductivity
                            values[2] = inner.getString("field3"); // flow rate
                            values[3] = inner.getString("field5"); // Tank 1 Water Level
                            values[4] = inner.getString("field8"); // Tank 2 Water Level
                            values[5] = inner.getString("field6"); // current
                            values[6] = inner.getString("field7"); // voltage
                            values[7] = inner.getString("field4"); // pressure

                            // attempt to parse the data from string to float
                            float errorValue = 100000;

                            for(int i = 0; i < arraySize; i++){

                                // attempt to parse string from ThingSpeak to a float
                                try{ floatValues[i] = Float.parseFloat(values[i]); }
                                // if a null value is detected, place error value in the array
                                catch(Exception e){ floatValues[i] = errorValue; }

                            }

                            String errorString = "No Data Found";

                            // adds appropriate data to the arraylist

                            if(floatValues[0] != errorValue) mDataValues.add(df.format(floatValues[0]) + " \u00b0C");
                            else mDataValues.add(errorString);

                            if(floatValues[1] != errorValue) mDataValues.add(df.format(floatValues[1]) + " S");
                            else mDataValues.add(errorString);

                            if(floatValues[2] != errorValue) mDataValues.add(df.format(floatValues[2]) + " L / min");
                            else mDataValues.add(errorString);

                            if(floatValues[7] != errorValue) mDataValues.add(df.format(floatValues[7]) + " psi");
                            else mDataValues.add(errorString);


                            if(floatValues[3] != errorValue){
                                String tankOnePcntFilled = df.format(floatValues[3]) + " % filled";
                                String tankOneWaterLvl = df.format((floatValues[3] / 100) * HEIGHT) + " cm";

                                mDataValues.add(tankOneWaterLvl + "\n" + tankOnePcntFilled);
                            }
                            else mDataValues.add(errorString);

                            if(floatValues[4] != errorValue){
                                String tankTwoPcntFilled = df.format(floatValues[4]) + " % filled";
                                String tankTwoWaterLvl = df.format((floatValues[4] / 100) * HEIGHT) + " cm";

                                mDataValues.add(tankTwoWaterLvl + "\n" + tankTwoPcntFilled);
                            }
                            else mDataValues.add(errorString);

                            if(floatValues[3] != errorValue && floatValues[4] != errorValue) mDataValues.add(df.format((floatValues[3] + floatValues[4]) / 2) + " % full");
                            else mDataValues.add(errorString);

                            if(floatValues[5] != errorValue && floatValues[6] != errorValue) mDataValues.add(df.format(floatValues[5] * floatValues[6]) + " W");
                            else mDataValues.add(errorString);

                            if(floatValues[5] != errorValue) mDataValues.add(floatValues[5] + " A");
                            else mDataValues.add(errorString);

                            if(floatValues[6] != errorValue) mDataValues.add(floatValues[6] + " V");
                            else mDataValues.add(errorString);

                            // setting the height of the tank
                            mDataValues.add(4, 36 + " cm");

                            InitializeRecyclerView(mDataValues);

                        }
                        catch (JSONException e) // catches errors
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                // if there is an error display "Attempting to reconnect..." on the toast at the bottom of the screen
                Toast.makeText(MainActivity.this, "Attempting to reconnect...", Toast.LENGTH_SHORT).show();

            }
        });

        // Add the JSON object request the the queue (located in the MySingleton class
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(objectRequest);

    }

    // calls related recyclerview classes to initialize the view on the main activity
    int initialization = 1;

    private void InitializeRecyclerView(ArrayList<String> mDataSet){

        RecyclerView recyclerView = findViewById(R.id.rView);
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(titles, mDataSet, this);

        if(initialization == 1){
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            initialization--;
        } else {
            Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
            recyclerView.setAdapter(recyclerViewAdapter);

            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }
    }

    // used to get to the activity that holds the control options, event listener is in the xml sheet
    public void Controls(View view) {
        Intent intent = new Intent(MainActivity.this, ControlActivity.class);

        // pass sensor URL to controls class
        Bundle bundle = new Bundle();
        bundle.putString("sensor_url_key", sensor_read_key);
        intent.putExtras(bundle);

        startActivity(intent);
    }

    // used to add titles for the title cards to the arrayList
    private void InitializeTitle(){

        titles.add("Temperature");
        titles.add("Conductivity");
        titles.add("Flow Rate");
        titles.add("Pressure");
        titles.add("Height of Tank");
        titles.add("Tank One\nInformation");
        titles.add("Tank Two\nInformation");
        titles.add("Used Capacity");
        titles.add("Power Usage");
        titles.add("Current");
        titles.add("Voltage");

    }

    // refreshes the data every 5 seconds when the activity is showing
    Handler h = new Handler();
    Runnable r;
    int delay = 1000 * 5; // 5 second delay for data pull requests

    @Override
    protected void onResume(){ // when the activity is active refresh every 5 seconds

        h.postDelayed(r = new Runnable() {
            @Override
            public void run() {
                GetSensorData();
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