package com.example.naffaa.tankcontrol;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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

        // Data textboxes on the interface
        TextView temp = findViewById(R.id.temp_val);           // temperature value
        TextView conduct = findViewById(R.id.cond_val);        // conductivity value
        TextView flow = findViewById(R.id.flow_val);           // flow rate value
        TextView press = findViewById(R.id.pressure_val);      // pressure value
        TextView level = findViewById(R.id.water_level_val);   // water level value
        TextView power = findViewById(R.id.power_val);         // power value

        // Buttons on the interface
        ToggleButton toggleMotor = findViewById(R.id.motorToggle);
        ToggleButton toggleValve = findViewById(R.id.valveToggle);
        Button powerButton = findViewById(R.id.powerButton);

        getData(temp, conduct, flow, press, level, power);
    }

    // Receives the data from ThingSpeak and displays it on the appropriate
    // textbox
    String server_url =
            "https://api.thingspeak.com/channels/525549/feeds.json?api_key=7I4UJ8MNLR8I0LWS&results=1";

    final int arraySize = 7;

    private void getData(final TextView tempTxt, final TextView condTxt, final TextView flowTxt,
                         final TextView pressureTxt, final TextView waterTxt, final TextView powerTxt) {

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

                            String zero = inner.getString("field1"); // temperature
                            String one = inner.getString("field2"); // conductivity
                            //String three = inner.getString("field3"); // flow rate
                            //String four = inner.getString("field4"); // pressure
                            //String five = inner.getString("field5"); // water level
                            //String six = inner.getString("field6"); // power

                            values[0] = zero;
                            values[1] = one;

                            // Handles null values
                            for(int i = 0; i < arraySize; i++){
                                if(values[i] == "null") {
                                    values[i] = "No Value";
                                }
                            }

                            // set the field to the appropriate textbox
                            tempTxt.setText(values[1]);

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

    // event listener for this method is controlled by the powerButton
    // in the XML sheet
    public void PowerDetails(View v){
        Intent intent = new Intent(MainActivity.this, PowerActivity.class);
        startActivity(intent);
    }

}
