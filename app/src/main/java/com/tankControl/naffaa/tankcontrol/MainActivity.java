package com.tankControl.naffaa.tankcontrol;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Lists{

    ArrayList<String> titles = new ArrayList<>(); // holds title card information in MainActivity

    // URL variables of the ThingSpeak channel for the sensor data
    String sensor_read_key;
    String sensor_server_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configurations for the app
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();

        // Ensures the screen is always in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Gets local data (if there is any) whenever the activity is created
        GetData("System Names", mNames);
        GetData("Data Key", mSystems);
        GetData("Button Read", mButtonRead);
        GetData("Button Write", mButtonWrite);

        // Sets a system for viewing (picks a default system if none exist)
        SetSystem();

        // Adds hard-coded titles for the cards within the recycler view
        InitializeTitle();

        // Pulls first set of data from ThingSpeak
        GetSensorData();
    }

    final int arraySize = 8; // set to 8 because ThingSpeak has maximum 8 fields
    final float HEIGHT = 36; // height of tank (Constant, hard coded in)

    // Retreives data from a ThingSpeak channel that contains sensor information
    private void GetSensorData(){
        // formatting for numbers set the decimal to up to 2 places
        final DecimalFormat df = new DecimalFormat("0.0");

        // create a new JSON object request that will be sent to the queue in the MySingleton class
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, sensor_server_url, (JSONObject) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try
                        {
                            ArrayList<String> mDataValues = new ArrayList<>();

                            // iterate through the general object request to the JSON object that
                            // is holding our values
                            JSONArray outer = response.getJSONArray("feeds");
                            JSONObject inner = outer.getJSONObject(0);

                            // gets each field from ThingSpeak

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

                            // adds appropriate data to the arraylist

                            if(floatValues[0] != errorValue) mDataValues.add(df.format(floatValues[0]) + " \u00b0C");
                            else mDataValues.add(errorString);

                            if(floatValues[1] != errorValue) mDataValues.add(df.format(floatValues[1]) + " S");
                            else mDataValues.add(errorString);

                            if(floatValues[2] != errorValue) mDataValues.add(df.format(floatValues[2]) + " L / min");
                            else mDataValues.add(errorString);

                            if(floatValues[7] != errorValue) mDataValues.add(df.format(floatValues[7]) + " psi");
                            else mDataValues.add(errorString);


                            if(floatValues[3] != errorValue){
                                String tankOnePcntFilled = df.format(floatValues[3]) + " % filled";
                                String tankOneWaterLvl = df.format((floatValues[3] / 100) * HEIGHT) + " cm";

                                mDataValues.add(tankOneWaterLvl + "\n" + tankOnePcntFilled);
                            }
                            else mDataValues.add(errorString);

                            if(floatValues[4] != errorValue){
                                String tankTwoPcntFilled = df.format(floatValues[4]) + " % filled";
                                String tankTwoWaterLvl = df.format((floatValues[4] / 100) * HEIGHT) + " cm";

                                mDataValues.add(tankTwoWaterLvl + "\n" + tankTwoPcntFilled);
                            }
                            else mDataValues.add(errorString);

                            if(floatValues[3] != errorValue && floatValues[4] != errorValue) mDataValues.add(df.format((floatValues[3] + floatValues[4]) / 2) + " % full");
                            else mDataValues.add(errorString);

                            if(floatValues[5] != errorValue && floatValues[6] != errorValue) mDataValues.add(df.format(floatValues[5] * floatValues[6]) + " W");
                            else mDataValues.add(errorString);

                            if(floatValues[5] != errorValue) mDataValues.add(floatValues[5] + " A");
                            else mDataValues.add(errorString);

                            if(floatValues[6] != errorValue) mDataValues.add(floatValues[6] + " V");
                            else mDataValues.add(errorString);

                            // setting the height of the tank
                            mDataValues.add(4, 36 + " cm");

                            InitializeRecyclerView(mDataValues);

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
                // and clear prior data entires from the view
                ArrayList<String> noResultsFound = new ArrayList<>();
                InitializeRecyclerView(noResultsFound);

                Toast.makeText(MainActivity.this, "Attempting to reconnect...", Toast.LENGTH_SHORT).show();

            }
        });

        // Add the JSON object request the the queue (located in the MySingleton class
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(objectRequest);

    }

    // Calls related recyclerview classes to initialize the view on the main activity
    int initialization = 1;
    private void InitializeRecyclerView(ArrayList<String> mDataSet){

        RecyclerView recyclerView = findViewById(R.id.rView);
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(titles, mDataSet, this);

        // the if-else statement is used to determine if the recycler view has already been initialized once,
        // and if it has it calls "onSaveInstanceState" to maintain the place the user is looking at instead of sending
        // them to the top of the viewer
        if(initialization == 1){
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            initialization--;
        } else {
            Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
            recyclerView.setAdapter(recyclerViewAdapter);
            recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
        }
    }

    // Used to add titles for the title cards to the arrayList
    // could add more titles to the list for the recycler view, but it may not appear
    // unless there is corresponding data
    private void InitializeTitle(){

        titles.add("Temperature");
        titles.add("Conductivity");
        titles.add("Flow Rate");
        titles.add("Pressure");
        titles.add("Height of Tank");
        titles.add("Tank One\nInformation");
        titles.add("Tank Two\nInformation");
        titles.add("Used Capacity");
        titles.add("Power Usage");
        titles.add("Current");
        titles.add("Voltage");

    }

    // Sets the system based on what the user selects, which will appear at the top of the list of
    // systems (the zeroth value in the mSystems ArrayList)
    private void SetSystem(){
        if(mNames.isEmpty()){
            mNames.add("Default System");
            mSystems.add("BAY5Y9HPFP6V3C6G");
            mButtonRead.add("RREYB0QH84HAKNIZ");
            mButtonWrite.add("M3MIFBPFS6YFA3GZ");
        }

        sensor_read_key = mSystems.get(0); // get the default key
        sensor_server_url = "https://api.thingspeak.com/channels/544573/feeds.json?api_key=" + sensor_read_key + "&results=1"; // set the URL based on the key
    }

    // Used to get data stored locally within the app
    private void GetData(String filename, ArrayList<String> systemList) {
        String tempString = "";
        int indexOfComma = 0;

        try {
            InputStream inputStream = this.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                tempString = stringBuilder.toString();

                // parse the string and re-store it into the appropriate arraylist
                while(tempString.length() != 0){
                    indexOfComma = tempString.indexOf(",");
                    systemList.add(tempString.substring(0, indexOfComma));

                    // remove the part of the array that was added
                    tempString = tempString.substring(indexOfComma + 1);
                }
            }
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

    }

    // Used to save system information locally to avoid having to re add data repeatedly
    private void SaveData(String filename, ArrayList<String> systemList){
        String tempString = "";

        // put the contents of the arraylist into one string separated by commas to be saved
        for(int i = 0; i < systemList.size(); i++){
            tempString += systemList.get(i) + ',';
        }

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(tempString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    // Used to get to the activity that holds the system select options, event listener is in the xml sheet
    public void SystemSelect(View view) {
        Intent intent = new Intent(MainActivity.this, SystemSelect.class);
        startActivity(intent);
    }

    // Used to get to the activity that holds the control options, event listener is in the xml sheet
    public void Controls(View view) {
        Intent intent = new Intent(MainActivity.this, ControlActivity.class);
        startActivity(intent);
    }

    // Refreshes the data every 5 seconds when the activity is showing
    Handler h = new Handler();
    Runnable r;
    int delay = (int) (1000 * 1); // 500 ms second delay for data pull requests

    // Override standard android function for when the activity is within the view of the user
    @Override
    protected void onResume(){ // when the activity is active refresh every 5 seconds

        h.postDelayed(r = new Runnable() {
            @Override
            public void run() {
                GetSensorData();

                SaveData("System Names", mNames);
                SaveData("Data Key", mSystems);
                SaveData("Button Read", mButtonRead);
                SaveData("Button Write", mButtonWrite);

                SetSystem();
                h.postDelayed(r, delay);
            }
        }, delay);

        super.onResume();
    }

    // Override standard android function for when the activity is out of the view of the user
    @Override
    protected void onPause(){ // if the activity isn't active stop calling getData
        h.removeCallbacks(r);
        super.onPause();
    }

    // Override standard android function for when the user presses the back key within the activity
    @Override
    public void onBackPressed() {
        // When the back key is pressed on the main activity it exits the app, and on startup
        // two of the array values are added which causes glitches. Clearing the lists when back
        // is pressed ensures the double appearance of the lists does not happen since only the
        // data that was saved before quitting would be added to the lists when the app is reopened
        mNames.clear();
        mSystems.clear();
        mButtonRead.clear();
        mButtonWrite.clear();

        super.onBackPressed();
    }
}