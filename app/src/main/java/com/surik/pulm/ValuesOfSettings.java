package com.surik.pulm;

import android.app.Application;
import android.content.SharedPreferences;


import java.io.File;

/**
 *
 *  Containing values of Settings
 *
 */
public class ValuesOfSettings extends Application {

    private static ValuesOfSettings instance;

    public static final String HEADSENSE_DEFAULT_FOLDER = "/mnt/sdcard/SurikPulm/";
    public static final String TRIAL_ID = "000";

    private  SharedPreferences mSharedPreferences;
    private  SharedPreferences.Editor mEditor;

    private boolean continuesMode;
    private int recording_interval;
    private int soundLength;
    private int graph_type;
    private String baseDirectory;
    private boolean demo;

    @Override
    public void onCreate() {
        super.onCreate();
        setInstance(this);
        mSharedPreferences = getSharedPreferences("HeadSense Settings Data", MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        initValues();
    }

    public void initValues(){
        continuesMode = mSharedPreferences.getBoolean("continuesMode", true);
        recording_interval = mSharedPreferences.getInt("timeBetweenRecords", 2);
        soundLength = mSharedPreferences.getInt("soundLength", 10);
        baseDirectory = mSharedPreferences.getString("baseDirectory", HEADSENSE_DEFAULT_FOLDER);
        demo = mSharedPreferences.getBoolean("demo", false);

    }

    public void initSharedPreferences(){
        mEditor.putBoolean("continuesMode", continuesMode);
        mEditor.putInt("timeBetweenRecords", recording_interval);
        mEditor.putInt("soundLength", soundLength);
        mEditor.putInt("graph_type", graph_type);
        mEditor.putString("baseDirectory", baseDirectory);
        ///////////////////////////////////////////////////////////////////
        mEditor.putBoolean("demo", demo);
        ///////////////////////////////////////////////////////////////////
        mEditor.commit();
    }

    public boolean isEmpty(){
        return mSharedPreferences.getAll().isEmpty();
    }

    public void initDefaultValues() {
        continuesMode = true;
        recording_interval = 2;
        soundLength = 10;
        baseDirectory = HEADSENSE_DEFAULT_FOLDER;
        demo = false;
    }

    public static ValuesOfSettings getInstance(){
        return instance;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }


    public int getRecording_interval() {
        return recording_interval;
    }

    public void setRecording_interval(int recording_interval) {
        this.recording_interval = recording_interval;
    }

    public int getSoundLength() {
        return soundLength;
    }

    public void setSoundLength(int soundLength) {
        this.soundLength = soundLength;
    }

    public int getGraph_type() {
        return graph_type;
    }

    public void setGraph_type(int graph_type) {
        this.graph_type = graph_type;
    }

    public boolean isContinuesMode() {
        return continuesMode;
    }

    public boolean isDemo() {
        return demo;
    }

    public void setDemo(boolean demo) {
        this.demo = demo;
    }

    private void setInstance(ValuesOfSettings i) {
        instance = i;
    }
}
