package com.example.naffaa.tankcontrol;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.system.ErrnoException;
import android.view.View;
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

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // configurations for the app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // ensures the screen is always in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Buttons on the interface
        ToggleButton toggleMotor = findViewById(R.id.motorToggle); // Motor control button
        ToggleButton toggleValve = findViewById(R.id.valveToggle); // Value control button
        Button detailButton = findViewById(R.id.detailButton);

        getData();
    }

    // Receives the data from ThingSpeak and displays it on the appropriate textbox

    // URL of the ThingSpeak channel that the data is being sent to
    String server_url =
            "https://api.thingspeak.com/channels/544573/feeds.json?api_key=NBS23605E6LNZNMS&results=1";

    // sets the size of the array based on the amount of data that is being retrieved
    final int arraySize = 8;

    private void getData(){

        // Data textboxes on the interface
        final TextView tempTxt = findViewById(R.id.temp_val);           // temperature value
        final TextView condTxt = findViewById(R.id.cond_val);           // conductivity value
        final TextView flowTxt = findViewById(R.id.flow_val);           // flow rate value
        final TextView pressureTxt = findViewById(R.id.pressure_val);   // pressure value
        final TextView capUsed = findViewById(R.id.water_level_val);    // water level value
        final TextView powerTxt = findViewById(R.id.power_val);         // power value

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

                            // gets each field from ThingSpeak (unitless data is used for calculations)
                            String[] values = new String[arraySize];

                            values[0] = inner.getString("field1") + " \u00b0c";  // temperature
                            values[1] = inner.getString("field2") + " S";        // conductivity
                            values[2] = inner.getString("field3") + " cm^3 / s"; // flow rate
                            values[3] = inner.getString("field4");               // Tank 1 Water Level
                            values[4] = inner.getString("field5");               // Tank 2 Water Level
                            values[5] = inner.getString("field6");               // current
                            values[6] = inner.getString("field7");               // voltage
                            values[7] = inner.getString("field8") + " kPa";      // pressure

                            // If no data is found, the value displayed will be "No Data Found"
                            String noData = "No Data Found";

                            for(int i = 0; i < arraySize; i++){
                                if(values[i].contains("null")) {
                                    values[i] = noData;
                                }
                            }

                            // calculates power based on current and voltage from ThingSpeak
                            String pwr = noData;
                            if(values[5] != noData && values[6] != noData)  // if there is a voltage and a current
                            {
                                pwr = CalculatePower(values[5], values[6]); // calculate the power
                            }

                            // calculates remaining capacity of the system
                            String spaceLeft = noData;
                            if(values[3] != noData && values[4] != noData) // if there is data for both tank water levels
                            {
                                spaceLeft = CalculateSpaceRemaining(values[3], values[4]); // calculate the space remaining
                            }

                            // set the field to the appropriate textbox
                            tempTxt.setText(values[0]);
                            condTxt.setText(values[1]);
                            flowTxt.setText(values[2]);
                            pressureTxt.setText(values[7]);
                            capUsed.setText(spaceLeft);
                            powerTxt.setText(pwr);

                        }
                        catch (JSONException e) // catches errors
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                // if there is an error display "Connection Error" on the toast at the bottom of the screen
                Toast.makeText(MainActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();

            }
        });

        // Add the JSON object request the the queue (located in the MySingleton class
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(objectRequest);

    }

    // Calculates the space remaining in the two tanks
    private String CalculateSpaceRemaining(String t1, String t2){

        double amntOfSpace;
        double tankHeight = 100; // adjust based on actual tank height

        double tankOne = Double.parseDouble(t1);
        double tankTwo = Double.parseDouble(t2);

        double tankOnePcnt = (tankOne / tankHeight) * 100;
        double tankTwoPcnt = (tankTwo / tankHeight) * 100;

        amntOfSpace = (tankOnePcnt + tankTwoPcnt) / 2;

        return Math.round(amntOfSpace) + "% full";

    }

    // Calculates the power of the system
    private String CalculatePower(String c, String v){

        double pwr;

        double a = Double.parseDouble(c);
        double b = Double.parseDouble(v);
        pwr = (a * b);

        return Math.round(pwr) + " W";

    }

    // refreshes the data every 5 seconds when the activity is showing
    Handler h = new Handler();
    Runnable r;
    int delay = 1000 * 5; // 15 second delay

    @Override
    protected void onResume(){ // when the activity is active refresh every 5 seconds

        h.postDelayed(r = new Runnable() {
            @Override
            public void run() {
                getData();
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
        startActivity(intent);
    }
}