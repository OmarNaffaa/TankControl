package com.example.naffaa.tankcontrol;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ControlActivity extends AppCompatActivity implements Lists{

    // URLs that hold the data for the valve and pump buttons
    String pump_state_url;
    String valve_state_url;
    String bothOn;
    String motorOn;
    String valveOn;
    String bothOff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        getSupportActionBar().hide();

        // initialize links used to interface buttons upon activity creation
        pump_state_url = "https://api.thingspeak.com/channels/603121/fields/1.json?api_key=" + mButtonRead.get(0) + "&results=1";
        valve_state_url = "https://api.thingspeak.com/channels/603121/fields/2.json?api_key=" + mButtonRead.get(0) + "&results=1";
        bothOn = "https://api.thingspeak.com/update.json?api_key=" + mButtonWrite.get(0) + "&field1=1&field2=1";
        bothOff = "https://api.thingspeak.com/update.json?api_key=" + mButtonWrite.get(0);                        // OFF = null
        motorOn = "https://api.thingspeak.com/update.json?api_key=" + mButtonWrite.get(0) + "&field1=1";
        valveOn = "https://api.thingspeak.com/update.json?api_key=" + mButtonWrite.get(0) + "&field2=1";

        // initialize the state of the pump and valve
        InitializeValve();
        InitializePump();

        // if either button is clicked, the new status will be written to ThingSpeak
        ToggleButton valveCheck = findViewById(R.id.valveToggle);
        ToggleButton pumpCheck = findViewById(R.id.pumpToggle);

        valveCheck.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UpdateChannel();
            }
        });
        pumpCheck.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UpdateChannel();
            }
        });

    }

    // reads the initial state of the pump on the system and sets the toggle button accordingly
    private void InitializePump(){

        final ToggleButton pump = findViewById(R.id.pumpToggle);

        // create a new JSON object request that will be sent to the queue in the MySingleton class
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, pump_state_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try
                        {
                            // iterate through the general object request to the JSON object that
                            // is holding our values
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);

                            // gets the state of the pump and sets the toggle button state
                            String toggleState = inner.getString("field1");

                            if(toggleState.contains("1")) {   // if the pump is on keep it on initially
                                pump.setChecked(true);

                            } else {                      // otherwise set the pump to off initially
                                pump.setChecked(false);
                            }

                        }
                        catch (JSONException e) // catches json request errors
                        {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                // if there is an error display "Attempting to reconnect..." on the toast at the bottom of the screen
                if(!mButtonRead.isEmpty()) { // if the URL is not disabled but there are still connection issues
                    Toast.makeText(ControlActivity.this, "Attempting to reconnect...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add the JSON object request the the queue (located in the MySingleton class
        MySingleton.getInstance(ControlActivity.this).addToRequestQueue(objectRequest);

    }

    // reads the initial state of the pump on the system and sets the toggle button accordingly
    private void InitializeValve(){

        final ToggleButton valve = findViewById(R.id.valveToggle);

        // create a new JSON object request that will be sent to the queue in the MySingleton class
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, valve_state_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try
                        {
                            // iterate through the general object request to the JSON object that
                            // is holding our values
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);

                            // gets the state of the pump and sets the toggle button state
                            String toggleState = inner.getString("field2");

                            if(toggleState.contains("1")) { // if the pump is on keep it on initially
                                valve.setChecked(true);
                            } else { // otherwise set the pump to off initially
                                valve.setChecked(false);
                            }

                        }
                        catch (JSONException e) // catches json request errors
                        {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                // if there is a connection error display "Attempting to reconnect..." on the toast at the bottom of the screen
                if(!mButtonRead.isEmpty()) { // if the URL is not disabled but there are still connection issues
                    Toast.makeText(ControlActivity.this, "Attempting to reconnect...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add the JSON object request the the queue (located in the MySingleton class
        MySingleton.getInstance(ControlActivity.this).addToRequestQueue(objectRequest);

    }

    // call this method to update the ThingSpeak channel
    private void UpdateChannel(){

        // used to open ThingSpeak in order to refresh the status of the buttons
        WebView updateChannel = findViewById(R.id.update);

        // Buttons on the interface
        ToggleButton mTog = findViewById(R.id.pumpToggle); // Motor control button
        ToggleButton vTog = findViewById(R.id.valveToggle); // Value control button

        if(mTog.isChecked() && vTog.isChecked()){
            updateChannel.loadUrl(bothOn);
        }
        // if the motor button is switched on and the valve button is off, update the channel to ON and OFF (1 and 0)
        if(mTog.isChecked() && !vTog.isChecked()){
            updateChannel.loadUrl(motorOn);
        }
        // if the motor button is switched off and the valve button is on, update the channel to OFF and ON (0 and 1)
        if(!mTog.isChecked() && vTog.isChecked()){
            updateChannel.loadUrl(valveOn);
        }
        // if the motor button is switched off and the valve button is off, update the channel to OFF and OFF (0 and 0)
        if(!mTog.isChecked() && !vTog.isChecked()){
            updateChannel.loadUrl(bothOff);
        }

    }

    // refreshes the data when the activity is showing
    Handler h = new Handler();
    Runnable r;
    int delay = (int) (1000 * 0.5); // 1 second second delay for data pull requests

    @Override
    protected void onResume(){

        h.postDelayed(r = new Runnable() {
            @Override
            public void run() {
                InitializeValve();
                InitializePump();
                h.postDelayed(r, delay);
            }
        }, delay);

        super.onResume();
    }

    @Override
    protected void onPause(){ // if the activity isn't active stop making ThingSpeak requests
        h.removeCallbacks(r);
        super.onPause();
    }
}