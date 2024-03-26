package com.example.fem21application;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.WindowManager;

import io.embrace.android.embracesdk.Embrace;
import io.embrace.android.embracesdk.EmbraceSamples;

public class MainActivity extends AppCompatActivity {

    //TODO:Define 3 types of variables and initialize (e.g. view, status, flags): How to format?
    //TODO: Weerawit test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Embrace.getInstance().start(this);
        EmbraceSamples.verifyIntegration(); // temporarily add this to verify the integration


        setContentView(R.layout.activity_main); //sets the initial layout to "activity_main.xml"
        //Log layout

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keeps screen on

        //receiver to receive messages from Bluetooth.java
        //FindID
    }

    //TODO: Copy and revise previous code

    //Formatting (previously used in the name valueChecker)

    //on Restart
    //on Destroy
    //on Click
    //on Acitivty result

    //showMessage
    //"RefindId"
    //handler
}