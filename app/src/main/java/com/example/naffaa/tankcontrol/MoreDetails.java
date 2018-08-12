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

import java.text.DecimalFormat;

public class MoreDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_details);
        getSupportActionBar().hide();

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
                            double HEIGHT = 100; //Adjust height based on size of tank

                            mDataSet[0] = inner.getString("field4"); // Tank 1 Water Level
                            mDataSet[1] = inner.getString("field5"); // Tank 2 Water Level
                            mDataSet[2] = inner.getString("field6"); // current
                            mDataSet[3] = inner.getString("field7"); // voltage

                            // parses water levels for calculations
                            if(mDataSet[0] != "null"){ tankOneProg.setText(CalculateTankOne(mDataSet[0], HEIGHT)); }
                            else{ tankOneProg.setText("No Data Found"); }

                            if(mDataSet[1] != "null"){ tankTwoProg.setText(CalculateTankTwo(mDataSet[1], HEIGHT)); }
                            else{ tankTwoProg.setText("No Data Found"); }

                            DecimalFormat decimalFormat = new DecimalFormat("0.00");

                            // Handles null values and formatting
                            double[] parsedVal = new double[SIZE];
                            String[] formattedDataSet = new String[SIZE];

                            for(int i = 0; i < SIZE; i++){
                                if(mDataSet[i].contains("null")) {
                                    formattedDataSet[i] = "No Data Found";
                                } else {
                                    parsedVal[i] = Double.parseDouble(mDataSet[i]);
                                    formattedDataSet[i] = decimalFormat.format(parsedVal[i]);

                                    if(i < 2) { formattedDataSet[i] += " cm"; } // adds units conditionally
                                    if(i == 2) {formattedDataSet[i] += " A"; }
                                    if(i == 3) {formattedDataSet[i] += " V"; }
                                }
                            }

                            waterLvlOne.setText(formattedDataSet[0]);
                            waterLvlTwo.setText(formattedDataSet[1]);
                            currentValue.setText(formattedDataSet[2]);
                            voltageValue.setText(formattedDataSet[3]);
                            tankHeight.setText(decimalFormat.format(HEIGHT) + " cm");

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

    private String CalculateTankOne(String tankOneInfo, double HEIGHT){

        try{

            double t1 = Double.parseDouble(tankOneInfo);
            double t1Prog = ((t1 / HEIGHT) * 100);

            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            return decimalFormat.format(t1Prog) + " % filled";

        } catch(Exception e){

            return "No Data Found";

        }

    }

    private String CalculateTankTwo(String tankTwoInfo, double HEIGHT){

        try{

            double t2 = Double.parseDouble(tankTwoInfo);
            double t2Prog = ((t2 / HEIGHT) * 100);

            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            return decimalFormat.format(t2Prog) + " % filled";

        } catch(Exception e){

            return "No Data Found";

        }

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