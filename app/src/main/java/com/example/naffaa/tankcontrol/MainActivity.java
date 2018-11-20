package com.example.naffaa.tankcontrol;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
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

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    // URL of the ThingSpeak channel for the sensor data, pump state, and value state
    String sensor_server_url =
            "https://api.thingspeak.com/channels/544573/feeds.json?api_key=BAY5Y9HPFP6V3C6G&results=1";
    String pump_state_url =
            "https://api.thingspeak.com/channels/603121/fields/3.json?api_key=RREYB0QH84HAKNIZ&results=1";
    String valve_state_url =
            "https://api.thingspeak.com/channels/603121/fields/4.json?api_key=RREYB0QH84HAKNIZ&results=1";

    // specified links used to update the status of both buttons on ThingSpeak (used in UpdateChannel method)
    String bothOn  = "https://api.thingspeak.com/update.json?api_key=M3MIFBPFS6YFA3GZ&field1=1&field2=1";
    String motorOn = "https://api.thingspeak.com/update.json?api_key=M3MIFBPFS6YFA3GZ&field1=1&field2=0";
    String valveOn = "https://api.thingspeak.com/update.json?api_key=M3MIFBPFS6YFA3GZ&field1=0&field2=1";
    String bothOff = "https://api.thingspeak.com/update.json?api_key=M3MIFBPFS6YFA3GZ&field1=0&field2=0";

    // sets the size of the array based on the amount of data that is being retrieved
    final int arraySize = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // configurations for the app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // ensures the screen is always in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // get the pin that was passed from the MainActivity
        Bundle bundle = getIntent().getExtras();
        String pin = bundle.getString("pin");

        DisableButtons(pin); // deny button access to users with lower access pin

        InitializeValve();
        InitializePump();
        GetSensorData(); // gets the data from ThingSpeak
        UpdateChannel(); // updates the data from ThingSpeak
    }

    // disables the button connection to ThingSpeak
    private void DisableButtons(String PIN){

        ToggleButton vTOG = findViewById(R.id.valveToggle);
        ToggleButton pTOG = findViewById(R.id.pumpToggle);

        if (PIN.length() % 2 == 0) { // if an 4 digit pin is entered remove access to the buttons
            pump_state_url = "";
            valve_state_url = "";
            bothOn = "";           valveOn = "";
            bothOff = "";          motorOn = "";

            vTOG.setVisibility(View.GONE); // remove button objects from layout
            pTOG.setVisibility(View.GONE);

            // disable URL links that upload button states to ThingSpeak
            bothOff = "";          bothOn = "";
            valveOn = "";          motorOn = "";
        }

    }

    // retreives data from a ThingSpeak channel that contains sensor information
    private void GetSensorData(){
        // formatting for numbers set the decimal to up to 2 places
        final DecimalFormat df = new DecimalFormat("0.0");

        // Data textboxes on the interface
        final TextView tempTxt = findViewById(R.id.temp_val);           // temperature value
        final TextView condTxt = findViewById(R.id.cond_val);           // conductivity value
        final TextView flowTxt = findViewById(R.id.flow_val);           // flow rate value
        final TextView pressureTxt = findViewById(R.id.pressure_val);   // pressure value
        final TextView capUsed = findViewById(R.id.water_level_val);    // water level value
        final TextView powerTxt = findViewById(R.id.power_val);         // power value

        // create a new JSON object request that will be sent to the queue in the MySingleton class
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, sensor_server_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            // iterate through the general object request to the JSON object that
                            // is holding our values
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);

                            // gets each field from ThingSpeak (unitless data is used for calculations)
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
                            // sets views with appropriate data
                            if(floatValues[0] != errorValue) tempTxt.setText(df.format(floatValues[0]) + " \u00b0C");
                            else tempTxt.setText(errorString);

                            if(floatValues[1] != errorValue) condTxt.setText(df.format(floatValues[1]) + " S");
                            else condTxt.setText(errorString);

                            if(floatValues[2] != errorValue) flowTxt.setText(df.format(floatValues[2]) + " L / min");
                            else flowTxt.setText(errorString);

                            if(floatValues[3] != errorValue && floatValues[4] != errorValue) capUsed.setText(df.format((floatValues[3] + floatValues[4]) / 2) + " % full");
                            else capUsed.setText(errorString);

                            if(floatValues[5] != errorValue && floatValues[6] != errorValue) powerTxt.setText(df.format(floatValues[5] * floatValues[6]) + " W");
                            else powerTxt.setText(errorString);

                            if(floatValues[7] != errorValue) pressureTxt.setText(df.format(floatValues[7]) + " psi");
                            else pressureTxt.setText(errorString);

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
                            String toggleState = inner.getString("field3");

                            if(Integer.parseInt(toggleState) == 1) // if the pump is on keep it on initially
                                pump.setChecked(true);
                            else                                   // otherwise set the pump to off initially
                                pump.setChecked(false);

                        }
                        catch (JSONException e) // catches json request errors
                        {
                            e.printStackTrace();
                        }
                        catch (NumberFormatException ex) // catches integer parse error
                        {
                            pump.setChecked(false); // if no number is detected leave the motor on the off state
                        }

                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                // if there is an error display "Attempting to reconnect..." on the toast at the bottom of the screen
                if(pump_state_url != "") { // if the URL is disabled
                    Toast.makeText(MainActivity.this, "Attempting to reconnect...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add the JSON object request the the queue (located in the MySingleton class
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(objectRequest);

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
                            String toggleState = inner.getString("field4");

                            if(Integer.parseInt(toggleState) == 1) // if the pump is on keep it on initially
                                valve.setChecked(true);
                            else                                   // otherwise set the pump to off initially
                                valve.setChecked(false);

                        }
                        catch (JSONException e) // catches json request errors
                        {
                            e.printStackTrace();
                        }
                        catch (NumberFormatException ex) // catches integer parse error
                        {
                            valve.setChecked(false); // if no number is detected leave the motor on the off state
                        }

                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                // if there is a connection error display "Attempting to reconnect..." on the toast at the bottom of the screen
                if(valve_state_url != "") {
                    Toast.makeText(MainActivity.this, "Attempting to reconnect...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Add the JSON object request the the queue (located in the MySingleton class
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(objectRequest);

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
                UpdateChannel();
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

    // event listener for this method is controlled by the powerButton
    // in the XML sheet
    public void Details(View v) {
        Intent intent = new Intent(MainActivity.this, MoreDetails.class);

        Bundle bundle = new Bundle(); // create the bundle
        bundle.putString("url", sensor_server_url); // add ThingSpeak URL
        intent.putExtras(bundle); // pass the variable to the main activity

        startActivity(intent);
    }

}