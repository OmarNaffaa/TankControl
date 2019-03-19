package com.tankControl.naffaa.tankcontrol;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class SystemSelect extends AppCompatActivity implements Lists{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_select);
        getSupportActionBar().hide();

        // ensures the screen is always in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // stops the keyboard from appearing on activity startup
        // and keeps the UI static when the keyboard appears
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // declare button objects
        Button add = findViewById(R.id.addKey);
        Button dlt = findViewById(R.id.subKey);
        Button slt = findViewById(R.id.selectKey);
        Button detail = findViewById(R.id.viewDetailsKey);

        // set button listeners that perform the appropriate action once the button is pressed
        dlt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DeleteSystems();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddSystems();
            }
        });
        slt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectSystem();
            }
        });
        detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewSystemDetails();
            }
        });

        // Set initial state of the recyclerview
        RefreshSystems();
    }

    // Called when the delete button is pressed, removes a system based on an entered system name
    private void DeleteSystems(){
        // Create a pop up dialog (Alert-Dialog) to warn the user and verify that they
        // would like to delete the system that they typed in
        AlertDialog.Builder deleteSystem = new AlertDialog.Builder(this);
        final TextView message = new TextView(this);
        final TextView info = findViewById(R.id.enterKey);

        deleteSystem.setTitle("Warning");
        message.setTextColor(getResources().getColor(R.color.cardText));
        message.setPadding(75, 50, 50, 50);
        message.setTextSize(20);
        message.setText("This will permanently remove the system, which would require you to add it again.\nHit \"OK\" to proceed, otherwise click \"Cancel\"");
        deleteSystem.setView(message);

        // If on the pop up dialog the user clicks the "OK" option the system information will be deleted
        // and the viewer will be refreshed
        deleteSystem.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int index = mNames.indexOf(info.getText().toString());

                mNames.remove(index);
                mChannels.remove(index);
                mBChannels.remove(index);
                mSystems.remove(index);
                mButtonRead.remove(index);
                mButtonWrite.remove(index);
                RefreshSystems();
            }
        });
        // If on the pop up dialog the user clicks the "Cancel" option the window will close
        // without doing anything to the system information
        deleteSystem.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        String temp = info.getText().toString();
        int check = 0;

        // Search for the name of the system that the user wants to delete and if a system has been
        // found increment the check variables as indication
        for(int i = 0; i < mNames.size(); i++){

            if(temp.equals(mNames.get(i))) {
                check++;
                deleteSystem.show();
            }

        }

        // If no system has been found by the for loop above,
        // notify the user that they should check the name they entered
        // Otherwise, save the lists again now that the system has been deleted
        if(check == 0)
            SystemNotFound();
        else {
            // Save added systems locally
            SaveData("System Names", mNames);
            SaveData("Channel ID", mChannels);
            SaveData("Button Channels", mBChannels);
            SaveData("Data Key", mSystems);
            SaveData("Button Read", mButtonRead);
            SaveData("Button Write", mButtonWrite);
        }

    }

    // Called when the add button is pressed, adds a system based on entered system information
    private void AddSystems(){
        final AlertDialog.Builder error = new AlertDialog.Builder(this);
        final EditText title = findViewById(R.id.enterKey);

        Boolean validate = true;

        // check to see if the title is already used, and if so exit the method
        for(int i = 0; i < mNames.size(); i++){

            if(title.getText().toString().equals(mNames.get(i))){
                validate = false;
            }

        }

        // If the name has not been used before, Call GetChannelID
        if(validate){
            GetChannelID(); // (note: will call GetButtonRead after data key is successfully added)

            // if a key wasn't added, remove all other keys that might have been added in the process

            while(mNames.size() != mChannels.size()){
                mChannels.remove(mChannels.size() - 1);
            }

            while(mNames.size() != mBChannels.size()){
                mBChannels.remove(mBChannels.size() - 1);
            }

            while(mNames.size() != mSystems.size()){
                mSystems.remove(mSystems.size() - 1);
            }

            while(mNames.size() != mButtonRead.size()){
                mButtonRead.remove(mButtonRead.size() - 1);
            }

            while(mNames.size() != mButtonWrite.size()){
                mButtonWrite.remove(mButtonWrite.size() - 1);
            }

        } else { // if a name has been used, notify the user

            error.setTitle("Error, this system name has already been used.");

            error.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

            error.show();
        }
    }

    // Methods called in AddSystems method to get keys, put into method form so they
    // could be called repeatedly if input is invalid
    private void GetChannelID(){
        final AlertDialog.Builder getChannel = new AlertDialog.Builder(this);
        final EditText dialogInputCID = new EditText(this);

        getChannel.setTitle("Enter a 6-Character Channel ID"); // set the title of the dialog box
        dialogInputCID.setInputType(InputType.TYPE_CLASS_TEXT); // set input type for dialog
        getChannel.setView(dialogInputCID);

        // Perform operations within the "onClick" method if the user clicks the "OK" option
        getChannel.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // If an invalid key is entered, notify the user and allow them to try again
                if(dialogInputCID.getText().toString().length() != 6 || dialogInputCID.getText().toString().trim().equals("")) {
                    GetChannelID();
                    ShowDialog();

                    // Otherwise, add the entered key and move to the next method to get the button read key
                } else {
                    mChannels.add(dialogInputCID.getText().toString());
                    GetButtonChannelID();
                }
            }
        });
        // Cancel the pop-up window if the user clicks the "Cancel" option
        getChannel.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        getChannel.show();
    }

    // Get the channel ID for the system buttons
    private void GetButtonChannelID(){
        final AlertDialog.Builder getBChannel = new AlertDialog.Builder(this);
        final EditText dialogInputBCID = new EditText(this);

        getBChannel.setTitle("Enter a 6-Character Button Channel ID"); // set the title of the dialog box
        dialogInputBCID.setInputType(InputType.TYPE_CLASS_TEXT); // set input type for dialog
        getBChannel.setView(dialogInputBCID);

        // Perform operations within the "onClick" method if the user clicks the "OK" option
        getBChannel.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // If an invalid key is entered, notify the user and allow them to try again
                if(dialogInputBCID.getText().toString().length() != 6 || dialogInputBCID.getText().toString().trim().equals("")) {
                    GetButtonChannelID();
                    ShowDialog();

                    // Otherwise, add the entered key and move to the next method to get the button read key
                } else {
                    mBChannels.add(dialogInputBCID.getText().toString());
                    GetDataKey();
                }
            }
        });
        // Cancel the pop-up window if the user clicks the "Cancel" option
        getBChannel.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        getBChannel.show();
    }

    // After GetChannelID method is successful, this method is called to get the channel ID
    private void GetDataKey(){
        final AlertDialog.Builder getData = new AlertDialog.Builder(this);
        final EditText dialogInput = new EditText(this);

        getData.setTitle("Enter a 16-Character System Data Key"); // set the title of the dialog box
        dialogInput.setInputType(InputType.TYPE_CLASS_TEXT); // set input type for dialog
        getData.setView(dialogInput);

        // Perform operations within the "onClick" method if the user clicks the "OK" option
        getData.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // If an invalid key is entered, notify the user and allow them to try again
                if(dialogInput.getText().toString().length() != 16 || dialogInput.getText().toString().trim().equals("")) {
                    GetDataKey();
                    ShowDialog();

                // Otherwise, add the entered key and move to the next method to get the button read key
                } else {
                    mSystems.add(dialogInput.getText().toString());
                    GetButtonRead();
                }
            }
        });
        // Cancel the pop-up window if the user clicks the "Cancel" option
        getData.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        getData.show();
    }

    // After the GetDataKey method is successful, this method is called to get the button read key
    private void GetButtonRead(){
        // Same logic used as the GetDataKey method, except once a valid button read key
        // has been entered the GetButtonWrite method will be called to get the button write key

        final AlertDialog.Builder getButtonRead = new AlertDialog.Builder(this);
        final EditText dialogInput2 = new EditText(this);

        getButtonRead.setTitle("Enter a 16-Character Button Read Key");
        dialogInput2.setInputType(InputType.TYPE_CLASS_TEXT); // set input type for dialog
        getButtonRead.setView(dialogInput2);

        // set up the positive and negative buttons for the button read key dialog
        getButtonRead.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(dialogInput2.getText().toString().length() != 16 || dialogInput2.getText().toString().trim().equals("")) {
                    GetButtonRead();
                    ShowDialog();
                } else {
                    mButtonRead.add(dialogInput2.getText().toString());
                    GetButtonWrite();
                }
            }
        });
        getButtonRead.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        getButtonRead.show();
    }

    // After GetButtonRead is successful, this method is caleld to get the button write key
    private void GetButtonWrite(){
        final EditText title = findViewById(R.id.enterKey); // used to add the title after all keys are added

        final AlertDialog.Builder getButtonWrite = new AlertDialog.Builder(this);
        final EditText dialogInput3 = new EditText(this);

        getButtonWrite.setTitle("Enter a 16-Character Button Write Key");
        dialogInput3.setInputType(InputType.TYPE_CLASS_TEXT); // set input type for dialog

        getButtonWrite.setView(dialogInput3);

        // If the user clicks "OK" on the pop-up window perform the operations within the onClick method
        getButtonWrite.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // If the entered key is invalid, recall this method and notify the user to try again
                if(dialogInput3.getText().toString().length() != 16 || dialogInput3.getText().toString().trim().equals("")) {
                    GetButtonWrite();
                    ShowDialog();

                // Otherwise, add the button write key and save all the system information since the user succsesfully added
                // all the system information that is needed to complete a system profile. Afterwards, refresh the viewer
                } else {
                    mButtonWrite.add(dialogInput3.getText().toString());

                    // add the system title if all keys were successfully added
                    mNames.add(title.getText().toString());

                    // Save added systems locally
                    SaveData("System Names", mNames);
                    SaveData("Channel ID", mChannels);
                    SaveData("Button Channels", mBChannels);
                    SaveData("Data Key", mSystems);
                    SaveData("Button Read", mButtonRead);
                    SaveData("Button Write", mButtonWrite);

                    // refresh the feed after making changes with this method
                    RefreshSystems();
                }
            }
        });
        // Cancel the pop-up window if the user clicks on the "Cancel" option
        getButtonWrite.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        getButtonWrite.show();
    }

    // Called when the select button is pressed, selects a system based on an entered system name
    private void SelectSystem(){

        final EditText system = findViewById(R.id.enterKey);
        String temp = system.getText().toString();
        String tempName, tempChannel, tempBChannel, tempDataKey, tempButtonRead, tempButtonWrite;

        Boolean found = false; // used to check whether or not to display a message

        // Iterate through the list of systems, and if a system has been found with the name that
        // the user has entered move the system information to the top of the list of systems
        // in order select it
        for(int i = 0; i < mNames.size(); i++){

            if(temp.equals(mNames.get(i))){
                // store values to be moved to front of array in temp variables
                tempName = mNames.get(i);
                tempChannel = mChannels.get(i);
                tempBChannel = mBChannels.get(i);
                tempDataKey = mSystems.get(i);
                tempButtonRead = mButtonRead.get(i);
                tempButtonWrite = mButtonWrite.get(i);

                // remove the values from its existing position to the front of the arraylist
                // in order to set the system keys in other methods
                mNames.remove(i);
                mChannels.remove(i);
                mBChannels.remove(i);
                mSystems.remove(i);
                mButtonRead.remove(i);
                mButtonWrite.remove(i);

                mNames.add(0, tempName);
                mChannels.add(0, tempChannel);
                mBChannels.add(0, tempBChannel);
                mSystems.add(0, tempDataKey);
                mButtonRead.add(0, tempButtonRead);
                mButtonWrite.add(0, tempButtonWrite);

                found = true;
                SystemSelected(tempName); // notifys the user that the system has been set
                RefreshSystems(); // refresh the recycler view with the current system moved to the top
            }

        }

        // if the system the user wants to select has not been found, display a notifcation message
        if(found == false) { SystemNotFound(); }
    }

    // Will show the keys that are added under a system name
    private void ViewSystemDetails(){
        int indexOfSystem = -1;

        final EditText sysName = findViewById(R.id.enterKey);

        // Set the index to view system details based on where the name is
        for(int i = 0; i < mNames.size(); i++){
            if(sysName.getText().toString().equals(mNames.get(i))){
                indexOfSystem = i;
            }
        }

        if(indexOfSystem == -1) { // exit method if system name is not found
            SystemNotFound();     // display an error message to the user
            return;
        }

        // *** Alert Dialog Setup to view system information *** \\
        // (most configurations are for appearance)
        AlertDialog.Builder systemInfo = new AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // add TextViews containing the title and the 3 keys
        systemInfo.setTitle(mNames.get(indexOfSystem)); // set the title

        final TextView channelKey = new TextView(this); // set the data key
        channelKey.setTextSize(20);
        channelKey.setTextColor(getResources().getColor(R.color.cardText));
        channelKey.setPadding(75, 50, 50, 50);
        channelKey.setText("Channel ID:\n     " + mChannels.get(indexOfSystem));
        layout.addView(channelKey);

        final TextView bChannelKey = new TextView(this); // set the data key
        bChannelKey.setTextSize(20);
        bChannelKey.setTextColor(getResources().getColor(R.color.cardText));
        bChannelKey.setPadding(75, 50, 50, 50);
        bChannelKey.setText("Channel ID:\n     " + mBChannels.get(indexOfSystem));
        layout.addView(bChannelKey);

        final TextView dataKey = new TextView(this); // set the data key
        dataKey.setTextSize(20);
        dataKey.setTextColor(getResources().getColor(R.color.cardText));
        dataKey.setPadding(75, 50, 50, 50);
        dataKey.setText("Data Key:\n     " + mSystems.get(indexOfSystem));
        layout.addView(dataKey);

        final TextView buttonRead = new TextView(this); // set the button read key
        buttonRead.setTextSize(20);
        buttonRead.setTextColor(getResources().getColor(R.color.cardText));
        buttonRead.setPadding(75, 50, 50, 50);
        buttonRead.setText("Button Read Key:\n     " + mButtonRead.get(indexOfSystem));
        layout.addView(buttonRead);

        final TextView buttonWrite = new TextView(this); // set the button write key
        buttonWrite.setTextSize(20);
        buttonWrite.setTextColor(getResources().getColor(R.color.cardText));
        buttonWrite.setPadding(75, 50, 50, 50);
        buttonWrite.setText("Button Write Key:\n     " + mButtonWrite.get(indexOfSystem));
        layout.addView(buttonWrite);

        systemInfo.setView(layout); // set the view of the alert dialog to all of the keys

        // Exits the pop-up window once the user clicks the "OK" option
        systemInfo.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        systemInfo.show();
    }

    // Displays a message to the user indicating that a system was select (used in SelectSystem method)
    private void SystemSelected(String name){

        AlertDialog.Builder selected = new AlertDialog.Builder(this);
        selected.setTitle('\"' + name + '\"' + " was selected");

        selected.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        selected.show();

    }

    // Displays a message that the system could not be added
    private void SystemNotFound(){

        AlertDialog.Builder notFound = new AlertDialog.Builder(this);
        final TextView message = new TextView(this);
        message.setTextColor(getResources().getColor(R.color.cardText));
        message.setPadding(75, 50, 50, 50);
        message.setTextSize(20);

        notFound.setTitle("System Not Found");
        message.setText("Recheck that the name is correct and try again.");
        notFound.setView(message);

        notFound.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        notFound.show();
    }

    // Displays a message to the user that the system could not be added
    private void ShowDialog(){

        AlertDialog.Builder systemNotAdded = new AlertDialog.Builder(this);
        final TextView message = new TextView(this);
        message.setTextColor(getResources().getColor(R.color.cardText));
        message.setPadding(75, 50, 50, 50);
        message.setTextSize(20);

        systemNotAdded.setTitle("System Not Added");
        message.setText("Recheck the key or channel ID and try again.");
        systemNotAdded.setView(message);

        systemNotAdded.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        systemNotAdded.show();
    }

    // Refreshes the recycler view feed within this method
    private void RefreshSystems() {

        RecyclerView recyclerView = findViewById(R.id.recycleView);
        RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(mNames,this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    // Used to save system information locally to avoid having to re-add data repeatedly
    // (copied from main activities to make method usable in system select method)
    private void SaveData(String filename, ArrayList<String> systemList){

        String tempString = "";

        // Put the contents of the arraylist into one string separated by commas to be saved
        for(int i = 0; i < systemList.size(); i++){
            tempString += systemList.get(i) + ',';
        }

        // Try to save the string locally, otherwise log a message that the save failed
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(tempString);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
}