package com.example.naffaa.tankcontrol;

import java.util.ArrayList;

public interface Lists {

    ArrayList<String> mNames = new ArrayList<>(); // holds the system names in SystemSelect Activity
    ArrayList<String> mSystems = new ArrayList<>(); // holds the system keys in SystemSelect Activity
    ArrayList<String> mButtonRead = new ArrayList<>(); // holds the read keys for the buttons in SystemSelect Activity
    ArrayList<String> mButtonWrite = new ArrayList<>(); // holds the write keys for the buttons in SystemSelect Activity

}