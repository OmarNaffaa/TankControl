package com.tankControl.naffaa.tankcontrol;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
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
    String pump_state_url, bar_state_url;
    String update_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        getSupportActionBar().hide();

        // Ensures the screen is always in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initialize links used to interface buttons upon activity creation
        pump_state_url = "https://api.thingspeak.com/channels/" + mChannels.get(0) + "/feeds.json?api_key=" + mSystems.get(0) + "&results=1";
        bar_state_url = "https://api.thingspeak.com/channels/" + mChannels.get(0) + "/fields/8.json?api_key=" + mSystems.get(0) + "&results=1";

        // initialize the state of the pump, valve, and flow meter power
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
                    @RequiresApi(api = Build.VERSION_CODES.N) // no effect, added annotation to remove warning statement
                    @Override
                    public void onResponse(JSONObject response) {

                        try
                        {
                            SeekBar powerSeekBar = findViewById(R.id.powerSeekBar);
                            TextView powerView = findViewById(R.id.updateVolt);
                            TextView tSpeakPower = findViewById(R.id.sysPowerLvl); // value stored on ThingSpeak

                            int prog = powerSeekBar.getProgress();

                            // iterate through the general object request to the JSON object that
                            // is holding our values
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);
                            String powerState = inner.getString("field8");

                            // if a null string is received, set it to 0 instead
                            if(powerState.contains("null") || powerState.isEmpty()) {
                                powerState = "";
                                tSpeakPower.setText("No value found");
                            }
                            else
                                tSpeakPower.setText("Current ThingSpeak Value: " + Integer.parseInt(powerState)*10 + "%");

                            // changes the value of the seekbar and percentage box
                            // if a different user updated the value
                            if(!powerState.equals(prevPowerValue)){
                                powerView.setText(Integer.parseInt(powerState)*10 + "%");
                                powerSeekBar.setProgress(Integer.parseInt(powerState), true);
                                prevPowerValue = powerState;
                            }

                            // sets the initial value of the seekbar and percentage box
                            if(powerInit == 0){
                                powerView.setText(powerState + "0%");
                                prevPowerValue = powerState;
                                powerInit++;
                            }
                        }
                        catch (JSONException e) // catches json request errors
                        {
                            e.printStackTrace();
                        }
                        catch (Exception ex) // handles general exceptions
                        {
                            // could display a message for general exception, not currently used
                            //Toast.makeText(ControlActivity.this, "null value found", Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                // if there is a connection error display "Attempting to reconnect..." on the toast at the bottom of the screen
                if(!mSystems.isEmpty()) { // if the URL is not disabled but there are still connection issues
                    Toast.makeText(ControlActivity.this, "Attempting to reconnect...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add the JSON object request the the queue (located in the MySingleton class
        MySingleton.getInstance(ControlActivity.this).addToRequestQueue(objectRequest);
    }

    // reads the initial state of the pump on the system and sets the toggle button accordingly
    private void InitializePump(){

        final TextView pState = findViewById(R.id.pumpStateView);

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
                            String waterHeight = inner.getString("field5");
                            String systemState = inner.getString("field6"); // pump read field

                            mDataPoints[4] = waterHeight; // update the array from ControlActivity since Main is paused

                            double wHeight = Double.parseDouble(waterHeight);

                            if(systemState.contains("1")) { // if the pump is on keep it on
                                pState.setText("The pump is currently ON");

                            } else {
                                pState.setText("The pump is currently OFF");

                            }

                            if(wHeight >= 75.0) // send a 0 once the tank is full
                                PumpOff();

                        }
                        catch (JSONException e) // catches json request errors
                        {
                            e.printStackTrace();
                        }
                        catch (Exception ex) // handles general exceptions
                        {
                            Toast.makeText(ControlActivity.this, "null value found", Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                // if there is an error display "Attempting to reconnect..." on the toast at the bottom of the screen
                if(!mSystems.isEmpty()) { // if the URL is not disabled but there are still connection issues
                    Toast.makeText(ControlActivity.this, "Attempting to reconnect...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add the JSON object request the the queue (located in the MySingleton class
        MySingleton.getInstance(ControlActivity.this).addToRequestQueue(objectRequest);

    }

    private void ResetChannel(){
        double wHeight = Double.parseDouble(mDataPoints[4]);

        if(wHeight >= 75.0) // send a 0 once the tank is full
            PumpOff();
    }

    public void PumpOn(View v){

        // get the first digit of the percentage and upload to ThingSpeak
        // (for microcontroller: divide this digit by 10 to get the ratio)
        String two;
        SeekBar skBr = findViewById(R.id.powerSeekBar);
        int prog = skBr.getProgress();
        two = prog + "";

        // update the system control values, must get all previous points or else
        // the updating of the control values will clear the sensor values
        update_url = "https://api.thingspeak.com/update.json?api_key=" + mButtonWrite.get(0) +
                 "&field1=1";

        // used to open ThingSpeak in order to refresh the status of the buttons
        WebView updateChannel = findViewById(R.id.update);
        updateChannel.loadUrl(update_url);
    }

    public void PumpOff(View v){

        // get the first digit of the percentage and upload to ThingSpeak
        // (for microcontroller: divide this digit by 10 to get the ratio)
        String two;
        SeekBar skBr = findViewById(R.id.powerSeekBar);
        int prog = skBr.getProgress();
        two = prog + "";

        // update the system control values, must get all previous points or else
        // the updating of the control values will clear the sensor values
        update_url = "https://api.thingspeak.com/update.json?api_key=" + mButtonWrite.get(0) +
                "&field1=0";

        // used to open ThingSpeak in order to refresh the status of the buttons
        WebView updateChannel = findViewById(R.id.update);
        updateChannel.loadUrl(update_url);
    }

    // Overloaded PumpOff method
    public void PumpOff(){

        // get the first digit of the percentage and upload to ThingSpeak
        // (for microcontroller: divide this digit by 10 to get the ratio)
        String two;
        SeekBar skBr = findViewById(R.id.powerSeekBar);
        int prog = skBr.getProgress();
        two = prog + "";

        // update the system control values, must get all previous points or else
        // the updating of the control values will clear the sensor values
        update_url = "https://api.thingspeak.com/update.json?api_key=" + mButtonWrite.get(0) +
                "&field1=0";

        // used to open ThingSpeak in order to refresh the status of the buttons
        WebView updateChannel = findViewById(R.id.update);
        updateChannel.loadUrl(update_url);
    }

    // refreshes the data when the activity is showing
    Handler h = new Handler();
    Runnable r;

    Handler rsHandler = new Handler(); // separate handler for reseting the pump state
    Runnable rsChannel;

    int delay = (int) (1000 * 1); // n second delay for pump update requests - multiplied by 1000 to convert from ms to sec

    @Override
    protected void onResume(){

        h.postDelayed(r = new Runnable() {
            @Override
            public void run() {
                InitializePump();
                InitializePower();
                h.postDelayed(r, delay);
            }
        }, delay);

        rsHandler.postDelayed(rsChannel = new Runnable() {
            @Override
            public void run() {
                ResetChannel();
                rsHandler.postDelayed(rsChannel, delay);
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