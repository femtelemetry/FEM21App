package com.example.fem21application;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    String title = "FEM21App data";
    String folder1 = "folder 1";
    int key = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO:Define 3 types of variables and initialize (e.g. view, status, flags): How to format?
        //TODO: Weerawit test

        //For Firebase
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this); //Google Analytics (Firebase) for logging specific events

        setContentView(R.layout.activity_main); //sets the initial layout to "activity_main.xml"
        //Log layout

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keeps screen on

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://fem21app-default-rtdb.asia-southeast1.firebasedatabase.app/");
        //database.setPersistenceEnabled(true);

        DatabaseReference myRef = database.getReference(title).child(folder1);

        Button submitButton = findViewById(R.id.SubmitButton);
        EditText textbox = findViewById(R.id.TextBox);
        submitButton.setOnClickListener(v -> {
            String text = textbox.getText().toString();
            // Write a message to the database
            myRef.child("key" + key).setValue(text);
            key++;
        });


        // Read from the database
        myRef.child(folder1).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d("database", "Value is: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w("database", "Failed to read value.", error.toException());
            }
        });

        Button crashButton = findViewById(R.id.CrashButton);
        crashButton.setOnClickListener(view -> {
            throw new RuntimeException("Test Crash"); // Force a crash
        });


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