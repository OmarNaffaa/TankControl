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

public class PowerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getPower();
    }

    String server_url =
            "https://api.thingspeak.com/channels/544573/feeds.json?api_key=NBS23605E6LNZNMS&results=1";

    private void getPower() {

        final TextView currentTxt = findViewById(R.id.current_val);
        final TextView voltageTxt = findViewById(R.id.voltage_val);

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, server_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);

                            String currentValue = inner.getString("field6") + " ";
                            String voltageValue = inner.getString("field7") + " ";

                            currentValue = currentValue.substring(0,5) + " A";
                            voltageValue = voltageValue.substring(0,5) + " V";

                            CharSequence nullValue = "null ";

                            if(currentValue.contains(nullValue)){
                                currentValue = "No Data Found";
                            }
                            if(voltageValue.contains(nullValue)){
                                voltageValue = "No Data Found";
                            }

                            currentTxt.setText(currentValue);
                            voltageTxt.setText(voltageValue);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                Toast.makeText(PowerActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();

            }
        });

        MySingleton.getInstance(PowerActivity.this).addToRequestQueue(objectRequest);

    }

    // refreshes the data when the activity is active every 5 seconds
    Handler h = new Handler();
    Runnable r;
    int delay = 1000 * 5; // time delay

    @Override
    protected void onResume(){ // when the activity is active refresh every x seconds

        h.postDelayed(r = new Runnable() {
            @Override
            public void run() {
                getPower();
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
