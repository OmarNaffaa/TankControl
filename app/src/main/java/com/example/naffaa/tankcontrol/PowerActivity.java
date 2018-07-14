package com.example.naffaa.tankcontrol;

import android.content.pm.ActivityInfo;
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

        TextView current = findViewById(R.id.current_val);
        TextView voltage = findViewById(R.id.voltage_val);

        getPower(current, voltage);
    }

    String server_url =
            "https://api.thingspeak.com/channels/525549/feeds.json?api_key=7I4UJ8MNLR8I0LWS&results=1";

    private void getPower(final TextView currentTxt, final TextView voltageTxt) {

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, server_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);

                            String currentValue = inner.getString("field6") + " A";
                            String voltageValue = inner.getString("field7") + " V";

                            CharSequence nullValue = "null";

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

                Toast.makeText(PowerActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();

            }
        });

        MySingleton.getInstance(PowerActivity.this).addToRequestQueue(objectRequest);

    }
}
