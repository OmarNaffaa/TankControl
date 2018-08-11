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

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class MoreDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_details);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getDetails();
    }

    String server_url =
            "https://api.thingspeak.com/channels/544573/feeds.json?api_key=NBS23605E6LNZNMS&results=1";

    final int SIZE = 4;

    private void getDetails(){

        // water data text fields
        final TextView waterLvlOne = findViewById(R.id.tankOneLvl);
        final TextView tankOneProg = findViewById(R.id.tankOneProgress);
        final TextView waterLvlTwo = findViewById(R.id.tankTwoLvl);
        final TextView tankTwoProg = findViewById(R.id.tankTwoProgress);
        final TextView tankHeight  = findViewById(R.id.tankHeight);

        // power data text fields
        final TextView currentValue = findViewById(R.id.cVal);
        final TextView voltageValue = findViewById(R.id.vVal);

        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, server_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);

                            String[] mDataSet = new String[SIZE];
                            int HEIGHT = 100; //Adjust height based on size of tank

                            mDataSet[0] = inner.getString("field4") + " "; // Tank 1 Water Level
                            mDataSet[1] = inner.getString("field5") + " "; // Tank 2 Water Level
                            mDataSet[2] = inner.getString("field6") + " "; // current
                            mDataSet[3] = inner.getString("field7") + " "; // voltage

                            // parses water levels for calculations
                            try{

                                double t1 = Double.parseDouble(mDataSet[0].trim());
                                double t2 = Double.parseDouble(mDataSet[1].trim());

                                String t1Prog = ((t1 / HEIGHT) * 100) + "";
                                String t2Prog = ((t2 / HEIGHT) * 100) + "";

                                tankOneProg.setText(t1Prog.substring(0,4) + "% filled");
                                tankTwoProg.setText(t2Prog.substring(0,4) + "% filled");

                            } catch(Exception e){

                                tankOneProg.setText("No Data Found");
                                tankTwoProg.setText("No Data Found");

                            }

                            mDataSet[0] = mDataSet[0].substring(0,5) + " cm";
                            mDataSet[1] = mDataSet[1].substring(0,5) + " cm";
                            mDataSet[2] = mDataSet[2].substring(0,5) + " A";
                            mDataSet[3] = mDataSet[3].substring(0,5) + " V";

                            // Handles null values
                            CharSequence nullValue = "null  ";
                            for(int i = 0; i < SIZE; i++){
                                if(mDataSet[i].contains(nullValue)) {
                                    mDataSet[i] = "No Data Found";
                                }
                            }

                            waterLvlOne.setText(mDataSet[0]);
                            waterLvlTwo.setText(mDataSet[1]);
                            currentValue.setText(mDataSet[2]);
                            voltageValue.setText(mDataSet[3]);
                            tankHeight.setText(HEIGHT + " cm");

                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error){

                Toast.makeText(MoreDetails.this, "Connection Error", Toast.LENGTH_SHORT).show();

            }
        });

        MySingleton.getInstance(MoreDetails.this).addToRequestQueue(objectRequest);

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
                getDetails();
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