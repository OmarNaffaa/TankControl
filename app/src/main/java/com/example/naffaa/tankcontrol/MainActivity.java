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

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Buttons on the interface
        ToggleButton toggleMotor = findViewById(R.id.motorToggle);
        ToggleButton toggleValve = findViewById(R.id.valveToggle);
        Button powerButton = findViewById(R.id.powerButton);

        getData();
    }

    // Receives the data from ThingSpeak and displays it on the appropriate
    // textbox
    String server_url =
            "https://api.thingspeak.com/channels/525549/feeds.json?api_key=7I4UJ8MNLR8I0LWS&results=1";
    final int arraySize = 7;

    private void getData(){

        // Data textboxes on the interface
        final TextView tempTxt = findViewById(R.id.temp_val);           // temperature value
        final TextView condTxt = findViewById(R.id.cond_val);           // conductivity value
        final TextView flowTxt = findViewById(R.id.flow_val);           // flow rate value
        final TextView pressureTxt = findViewById(R.id.pressure_val);   // pressure value
        final TextView waterTxt = findViewById(R.id.water_level_val);   // water level value
        final TextView powerTxt = findViewById(R.id.power_val);         // power value

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, server_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);

                            // gets each field from ThingSpeak
                            String[] values = new String[arraySize];

                            values[0] = inner.getString("field1") + " \u00b0C"; // temperature
                            values[1] = inner.getString("field2") + " S";       // conductivity
                            values[2] = inner.getString("field3") + " m^3 / s"; // flow rate
                            values[3] = inner.getString("field4") + " Pa";      // pressure
                            values[4] = inner.getString("field5") + " cm";      // water level
                            values[5] = inner.getString("field6") + " A";       // current
                            values[6] = inner.getString("field7") + " V";       // voltage

                            // Handles null values
                            CharSequence nullValue = "null";
                            for(int i = 0; i < arraySize; i++){
                                if(values[i].contains(nullValue)) {
                                    values[i] = "No Data Found";
                                }
                            }

                            // calculates power based on current and voltage from ThingSpeak
                            String pwr = "";
                            try{
                                int a = Integer.parseInt(values[5]);
                                int b = Integer.parseInt(values[6]);
                                pwr = Integer.toString(a * b);
                            } catch(Exception e){
                                pwr = "No Data Found";
                            }

                            // set the field to the appropriate textbox
                            tempTxt.setText(values[0]);
                            condTxt.setText(values[1]);
                            flowTxt.setText(values[2]);
                            pressureTxt.setText(values[3]);
                            waterTxt.setText(values[4]);
                            powerTxt.setText(pwr);

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                Toast.makeText(MainActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

            }
        });

        MySingleton.getInstance(MainActivity.this).addToRequestQueue(objectRequest);

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
    public void PowerDetails(View v){
        Intent intent = new Intent(MainActivity.this, PowerActivity.class);
        startActivity(intent);
    }

}