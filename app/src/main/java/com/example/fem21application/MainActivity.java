package com.example.fem21application;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {
    String title = "FEM21App data"; //Title of a directory [Realtime database]
    String folder1 = "folder 1"; //Title of a folder inside the directory [Realtime database]
    String collectionPath = title; //Title of a collection [Cloud FireStore database]
    String documentPath = "test"; //Title of document inside a collection [Cloud FireStore database]
    String TAG = "cloud";
    Firebase firebase = new Firebase();
    int signalFlag = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO:Define 3 types of variables and initialize (e.g. view, status, flags): How to format?
        //TODO: Weerawit test

        //For Firebase analytics
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this); //Google Analytics (Firebase) for logging specific events

        setContentView(R.layout.activity_main); //sets the initial layout to "activity_main.xml"
        //Log layout

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keeps screen on

        //To continuously send signal to database to keep connecting to it.
        AtomicReference<ScheduledExecutorService> executor = new AtomicReference<>(Executors.newScheduledThreadPool(1));
        Button signallingBtn = findViewById(R.id.signallingButton);
        signallingBtn.setOnClickListener(v -> {

            if(signalFlag == 1){
                signalFlag = 0;
                signallingBtn.setText("Deactivate Signalling");
                final int[] num = {0};
                // Schedule the task to run every 30 seconds

                executor.get().scheduleAtFixedRate(() -> {
                    // Your method to be executed periodically
                    num[0]++;
                    firebase.DatabaseSignaling(num[0]);
                    System.out.println("sending signal: " + num[0]);
                }, 0, 10, TimeUnit.SECONDS);
            } else{
                signallingBtn.setText("Activate Signalling");
                signalFlag = 1;
                executor.get().shutdown();
                executor.set(Executors.newScheduledThreadPool(1));
            }

        });


        //To submit any input text to the databases
        Button submitButton = findViewById(R.id.SubmitButton);
        EditText textbox = findViewById(R.id.TextBox);
        submitButton.setOnClickListener(v -> {

            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()); //Use timestamp as keys

            String text = textbox.getText().toString();

            // Write a message to the Realtime database (RealFireStore) and Cloud database (CloudFireStore)
            firebase.CountRun();
            firebase.RealFireStore("MOTOR", time, text);  //type of stored data can be anything simple.

//            Map<String, Object> data = new HashMap<>();  //For Cloud Firebase, the data needs to be of HashMap.
//            data.put(time, text);
//            CloudFireStore(date, data);
            Toast.makeText(this, "The message is sent to the database...", Toast.LENGTH_SHORT).show();

        });

        //To run number infinitely
        Button RunButton = findViewById(R.id.RunButton);
        RunButton.setOnClickListener( v -> {
            Toast.makeText(this, "The number is running now...", Toast.LENGTH_SHORT).show();
            firebase.CountRun();
            Log.i("database", "The data is being sent to the database");
            new Thread(() -> {
                for (int i = 0; i<=5; i++){
                    long nanoTime = System.nanoTime();
                    long micros = (nanoTime / 100000000); // Extract microseconds from nanoseconds

                    String time = new SimpleDateFormat("HH:mm:ss:" + micros, Locale.getDefault()).format(new Date()); //Use timestamp as keys

                    //Map<String, Object> data = new HashMap<>();  //For Cloud Firebase, the data needs to be of HashMap.
                    //data.put(time, i);
                    //CloudFireStore(date, data);
//                Random random = new Random();
//                Generate a random integer between 0 and 120
                    int randomNumber = (new Random()).nextInt(120);
                    firebase.RealFireStore("MOTOR", time, i);

//                Log.i("database", time + ":" + i);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

        });

        //TextView Textbox = findViewById(R.id.TextBox2);

        //To read data stored in databases
        Button readButton = findViewById(R.id.ReadButton);
        readButton.setOnClickListener(v -> {

            //Read everything in the Realtime database (RealFireStore) and Cloud database (CloudFireStore)
            //RealFireRead(date);
            firebase.CloudFireRead();
        });

        Button crashButton = findViewById(R.id.CrashButton);
        crashButton.setOnClickListener(view -> {
            throw new RuntimeException("Test Crash"); // Force a crash event
        });


        //receiver to receive messages from Bluetooth.java
        //FindID
    }

    //TODO: Copy and revise previous code

    //Formatting (previously used in the name valueChecker)

    //on Restart


    //on Destroy
    //on Click
    //on Activity result

    //showMessage
    //"Refind Id"
    //handler

}