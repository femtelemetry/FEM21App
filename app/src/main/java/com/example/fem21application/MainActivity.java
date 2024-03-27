package com.example.fem21application;

import static java.text.DateFormat.DATE_FIELD;
import static java.text.DateFormat.getDateInstance;
import static java.text.DateFormat.getTimeInstance;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String title = "FEM21App data"; //Title of a directory [Realtime database]
    String folder1 = "folder 1"; //Title of a folder inside the directory [Realtime database]
    String collectionPath = title; //Title of a collection [Cloud FireStore database]
    String documentPath = "test"; //Title of document inside a collection [Cloud FireStore database]
    String TAG = "cloud";
    FirebaseFirestore db = FirebaseFirestore.getInstance(); //Connect to Cloud FireStore.
    FirebaseDatabase database = FirebaseDatabase.getInstance("https://fem21app-f43ef-default-rtdb.asia-southeast1.firebasedatabase.app/"); //connect to the Realtime Database
    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()); // Create a SimpleDateFormat object with the desired date format

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

        //To submit any input text to the databases
        Button submitButton = findViewById(R.id.SubmitButton);
        EditText textbox = findViewById(R.id.TextBox);
        submitButton.setOnClickListener(v -> {

            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()); //Use timestamp as keys

            String text = textbox.getText().toString();

            // Write a message to the Realtime database (RealFireStore) and Cloud database (CloudFireStore)
            RealFireStore(date, time, text);  //type of stored data can be anything simple.

            Map<String, Object> data = new HashMap<>();  //For Cloud Firebase, the data needs to be of HashMap.
            data.put(time, text);
            CloudFireStore(date, data);
        });

        //TextView Textbox = findViewById(R.id.TextBox2);

        //To read data stored in databases
        Button readButton = findViewById(R.id.ReadButton);
        readButton.setOnClickListener(v -> {

            //Read everything in the Realtime database (RealFireStore) and Cloud database (CloudFireStore)
            //RealFireRead(date);
            CloudFireRead();
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


    //Store data to Realtime Database
    private void RealFireStore(String folder,String key, Object data){
        // Write a message to the database
        DatabaseReference myRef = database.getReference(title).child(folder);
        myRef.child(key).setValue(data);
    }

    //TODO: Fix RealFireRead
    //Detect any change in Realtime database
    private void RealFireRead(String folder){
        // Read from the database
        DatabaseReference myRef = database.getReference(title);
        myRef.addValueEventListener(new ValueEventListener() {
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
    }

    private void CloudFireStore(String documentPath, Object data){
/*
        //Add a new document (equivalent to a file) with a generated ID under "Test collection" collection (equivalent to folder)
        db.collection("Test collection")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                        Log.w(TAG, "Error adding document", e);
                });
 */


        //Add a new field (equivalent to data to be stored inside a document) to a specific document in the collection.
        db.collection(collectionPath).document(documentPath)
                .set(data, SetOptions.merge()) //This specifies the need for merging new data to an existing document, if do not want use: .set(data) ,this will overwrite the existing document.
                .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w(TAG, "Error writing document", e));
    }

    private void CloudFireRead() {
        db.collection(collectionPath)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            //Textbox.setText((CharSequence) document.getData());
                        }
                    } else {
                        Log.w(TAG, "Error getting documents.", task.getException());
                    }
                });
    }
}