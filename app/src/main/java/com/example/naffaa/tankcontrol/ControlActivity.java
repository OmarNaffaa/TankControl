package com.example.naffaa.tankcontrol;

import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
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
import org.w3c.dom.Text;

import java.util.ArrayList;

public class ControlActivity extends AppCompatActivity {

    // URLs that hold the data for the valve and pump buttons
    String pump_state_url =
            "https://api.thingspeak.com/channels/603121/fields/3.json?api_key=RREYB0QH84HAKNIZ&results=1";
    String valve_state_url =
            "https://api.thingspeak.com/channels/603121/fields/4.json?api_key=RREYB0QH84HAKNIZ&results=1";

    // specified links used to update the status of both buttons on ThingSpeak (used in UpdateChannel method)
    String bothOn  = "https://api.thingspeak.com/update.json?api_key=M3MIFBPFS6YFA3GZ&field1=1&field2=1";
    String motorOn = "https://api.thingspeak.com/update.json?api_key=M3MIFBPFS6YFA3GZ&field1=1&field2=0";
    String valveOn = "https://api.thingspeak.com/update.json?api_key=M3MIFBPFS6YFA3GZ&field1=0&field2=1";
    String bothOff = "https://api.thingspeak.com/update.json?api_key=M3MIFBPFS6YFA3GZ&field1=0&field2=0";

    // arrays used to hold the titles and system keys
    ArrayList<String> mNames = new ArrayList<>();
    ArrayList<String> mSystems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        getSupportActionBar().hide();

        // stops the keyboard from appearing on activity startup
        // and keeps the UI static when the keyboard appears
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        // ensures the screen is always in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initialize the state of the pump and valve
        InitializePump();
        InitializeValve();

        // if either button is clicked, the new status will be written to
        // ThingSpeak
        ToggleButton valveCheck = findViewById(R.id.valveToggle);
        ToggleButton pumpCheck = findViewById(R.id.pumpToggle);

        pumpCheck.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UpdateChannel();
            }
        });
        valveCheck.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                UpdateChannel();
            }
        });

        mNames.add("default system");
        mSystems.add("default key");
        RefreshSystems();

        // call the "AddSystems" method when the "ADD" button is pressed
        // which will add a system key and title to the lists and refresh the view
        Button add = findViewById(R.id.addKey);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AddSystems();

            }
        });
    }

    private void AddSystems(){

        TextView info = findViewById(R.id.enterKey);

        String temp = info.getText().toString();
        int comma = temp.indexOf(',');

        if(temp.contains(",")) {
            mNames.add(temp.substring(0, comma)); // add the title if there is a name
            mSystems.add(temp.substring(comma + 1)); // add the system key
        } else {
            mNames.add(" "); // add a placeholder for the title
            mSystems.add(temp); // add the system key
        }

        RefreshSystems();
    }

    private void RefreshSystems() {

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(mNames, mSystems, this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

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

    // refreshes the data every 5 seconds when the activity is showing
    Handler h = new Handler();
    Runnable r;
    int delay = 1000 * 5; // 5 second delay for data pull requests

    @Override
    protected void onResume(){ // when the activity is active refresh every 5 seconds

        h.postDelayed(r = new Runnable() {
            @Override
            public void run() {   // continually check the state of the pump and valve
                InitializePump(); // to keep multiple devices in sync
                InitializeValve();
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