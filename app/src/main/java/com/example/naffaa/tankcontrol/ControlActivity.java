package com.example.naffaa.tankcontrol;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
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
    String bar_state_url;
    String read_url;
    String update_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        getSupportActionBar().hide();

        // Ensures the screen is always in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initialize links used to interface buttons upon activity creation
        pump_state_url = "https://api.thingspeak.com/channels/603121/fields/1.json?api_key=" + mButtonRead.get(0) + "&results=1";
        valve_state_url = "https://api.thingspeak.com/channels/603121/fields/2.json?api_key=" + mButtonRead.get(0) + "&results=1";
        bar_state_url = "https://api.thingspeak.com/channels/603121/fields/3.json?api_key=" + mButtonRead.get(0) + "&results=1";
        read_url = "https://api.thingspeak.com/channels/603121/feeds.json?api_key=" + mButtonRead.get(0) + "&results=1";

        // initialize the state of the pump, valve, and flow meter power
        InitializeValve();
        InitializePump();
        InitializePower();

        // The two togglebuttons and the button event listeners are in the xml file
        // seekbar that will send the selected power ratio to the system
        SeekBar powerBar = findViewById(R.id.powerSeekBar);

        powerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                TextView powerRatio = findViewById(R.id.updateVolt);
                powerRatio.setText(progress * 10 + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // not currently necessary
                // would be used when the user first touches the seekbar to move it
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // not currently necessary
                // would be used when the user stops moving the seekbar
            }
        });

    }

    // reads the initial power ratio from the system and sets the interface accordingly
    String prevPowerValue;
    int powerInit = 0;
    private void InitializePower() {
        // create a new JSON object request that will be sent to the queue in the MySingleton class
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, bar_state_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try
                        {
                            SeekBar powerSeekBar = findViewById(R.id.powerSeekBar);
                            TextView powerView = findViewById(R.id.updateVolt);

                            // iterate through the general object request to the JSON object that
                            // is holding our values
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);
                            String powerState = inner.getString("field3");

                            // changes the value of the seekbar and percentage box
                            // if a different user updated the value
                            if(!powerState.equals(prevPowerValue)){
                                powerView.setText(powerState + "0%");
                                powerSeekBar.setProgress(Integer.parseInt(powerState), true);
                                prevPowerValue = powerState;
                            }

                            // sets the initial value of the seekbar and percentage box
                            if(powerInit == 0){
                                powerView.setText(powerState + "0%");
                                prevPowerValue = inner.getString("field3");
                                powerInit++;
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
    public void UpdateChannel(View v){
        String one, two, three;

        ToggleButton mTog = findViewById(R.id.pumpToggle); // Motor control button
        ToggleButton vTog = findViewById(R.id.valveToggle); // Valve control button
        TextView pwrVal = findViewById(R.id.updateVolt); // power percentage

        if(mTog.isChecked())
            one = "1";
        else
            one = "0";

        if(vTog.isChecked())
            two = "1";
        else
            two = "0";

        // get the first digit of the percentage and upload to ThingSpeak
        // (for microcontroller: divide this digit by 10 to get the ratio)
        SeekBar skBr = findViewById(R.id.powerSeekBar);
        int prog = skBr.getProgress();
        three = prog + "";

        update_url = "https://api.thingspeak.com/update.json?api_key=" + mButtonWrite.get(0) +
                "&field1=" + one + "&field2=" + two + "&field3=" +  three;

        Toast.makeText(ControlActivity.this, update_url, Toast.LENGTH_SHORT).show();

        // used to open ThingSpeak in order to refresh the status of the buttons
        WebView updateChannel = findViewById(R.id.update);
        updateChannel.loadUrl(update_url);
    }

    // refreshes the data when the activity is showing
    Runnable r;
    Handler h = new Handler();
    int delay = (int) (1000 * 1); // 1 second delay for data pull requests

    @Override
    protected void onResume(){

        h.postDelayed(r = new Runnable() {
            @Override
            public void run() {
                InitializeValve();
                InitializePump();
                InitializePower();
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