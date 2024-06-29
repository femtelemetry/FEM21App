package com.example.fem21application;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Firebase extends Service {

    private final String title = "FEM21App data"; //Title of a directory [Realtime database]
    private final String collectionPath = title; //Title of a collection [Cloud FireStore database]
    private final String TAG = "FIREBASE_SERVICE";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance(); //Connect to Cloud FireStore.
    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://fem21app-f43ef-default-rtdb.asia-southeast1.firebasedatabase.app"); //connect to the Realtime Database
    private final DatabaseReference ref = database.getReference(title);
    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()); // Create a SimpleDateFormat object with the desired date format
    int COUNT = 0;
    public Firebase(){}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "FIREBASE_SERVICE IS CREATED");
        ref.child("STATUS").setValue("START");


        Log.i(TAG, "Thread ToDriver Started");
        DatabaseReference myRef = database.getReference(title + '/' + date);
        myRef.child("ToDriver").setValue("No messages yet");
        myRef.child("ToDriver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again whenever data at this location is updated.
                String message = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Message: " + message);
                SendBroadcast(0, message);      //TODO:change VIEW number to the correct one later.
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ref.child("STATUS").setValue("STOP");
    }

    //Store data to Realtime Database
    public void realFireStore(String folder, String key, Object data){
        DatabaseReference REF = database.getReference(title + "/" + date);
        //To read the value COUNT stored in each date first, in order to name the RUN:x
        REF.child("COUNT").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer count = dataSnapshot.getValue(Integer.class);

                if (count != null) {
//                    System.out.println("sending "+ data);
                    REF.child("/RUN:" + COUNT + "/" + folder + "/" + key).setValue(data);
                } else {
                    System.out.println("Cannot obtain COUNT, Data will be sent to RUN:1");
                    REF.child("COUNT").setValue(1);
                    REF.child("/RUN:1/" + folder + "/" + key).setValue(data);
                    COUNT = 1;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Error: " + databaseError.getMessage());
            }
        });
    }
    public void countRun(){

        DatabaseReference REF = database.getReference(title + "/" + date);

        REF.child("COUNT").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Integer count = dataSnapshot.getValue(Integer.class);

                if (count != null) {
                    count = count + 1;
                    COUNT = count;
                    REF.child("COUNT").setValue(count);

                } else {
                    COUNT = 1;
                    REF.child("COUNT").setValue(1);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Error: " + databaseError.getMessage());
            }
        });
        COUNT++;
    }
    //TODO: Fix RealFireRead
    //Detect any change in Realtime database
    public void realFireRead(){
        // Read from the database
        DatabaseReference myRef = database.getReference(title + "/" + date);
        myRef.getKey();
        Log.d(TAG, "key: " + myRef.getKey());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }

        });
    }

    public void cloudFireStore(String documentPath, Object data){
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

    public void cloudFireRead() {
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
    //To keep the connection stable or some?
    public void databaseSignaling(int duration){
        DatabaseReference signalRef = database.getReference(title);
        signalRef.child("signal").setValue(duration);
    }

    private void SendBroadcast(int VIEW, String message) {
        // IntentをブロードキャストすることでMainActivityへデータを送信
        Intent intent = new Intent();
        intent.setAction("FIREBASE");  //Set code as BLUETOOTH for the receiver to know where the information is from
        intent.putExtra("VIEW", VIEW);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}