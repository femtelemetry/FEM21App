package com.example.fem21application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
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
    String TAG = "MainActivity";

    int signalFlag = 1;


    //Action(ステータス表示).
    static final int VIEW_STATUS = 0; //integers generally just set for the sake of
/*
    //Action(LV).
    static final int VIEW_LV = 1;

    //Action(HV)
    static final int VIEW_HV = 2;

    //Action(MOTOR)
    static final int VIEW_MT = 3;

    //Action(INV)
    static final int VIEW_INV = 4;

    //Action(RTD)
    static final int VIEW_RTD = 5;

    //信号受信時エラーは六番目
    static final int VIEW_ERR = 6;
    //Action(VELOCITY)
    static final int VIEW_VELO = 7;

    //Action(ERROR)
    static final int VIEW_ERRFR = 61;
    static final int VIEW_ERRFL = 62;
    static final int VIEW_ERRRR = 63;
    static final int VIEW_ERRRL = 64;

    //Action(NOWBTT)
    static final int VIEW_NOWBTT = 10;

    //Action(VCMINFO)
    static final int VIEW_VCMINFO = 11;

    //Action(TSV)
    static final int VIEW_TSV = 12;

    //Action(MAXCELLV)
    static final int VIEW_MAXCELLV = 13;

    //Action(MINCELLV)
    static final int VIEW_MINCELLV = 14;

    //Action(ZYUUDEN)
    static final int VIEW_ZYUUDEN = 15;

    //Action(MAXCELLT)
    static final int VIEW_MAXCELLT = 16;

    //Action(ERRORAMS)
    static final int VIEW_ERRORAMS = 17;

    //Action(ERRORCOUNT)
    static final int VIEW_ERRORCOUNT = 18;

    //Action(STATUSAMS)
    static final int VIEW_STATUSAMS = 19;

    static final int VIEW_RED = 20;
    static final int VIEW_YELLOW = 21;
    static final int VIEW_LIGHTGREEN = 22;
    static final int VIEW_GREEN = 23;

    //Action(LayoutChange:RTD)
    static final int LAYOUT_RTD = 51;

    //Action(LayoutChange:HVON)
    static final int LAYOUT_HVON = 52;

    //Action(LayoutChange:LVON)
    static final int LAYOUT_DEFAULT = 53;

    //Action(LayoutChange:ERROR)
    static final int LAYOUT_ERR = 54;

    //Action(LayoutChange:BORON)
    static final int LAYOUT_BOR = 55;

    //Action(LayoutVisible:HITEMP)
    static final int LAYOUT_HITEMP = 56;

    static final int LAYOUT_DRIVE = 57;

    static final int LAYOUT_LVON = 58;
    */

    //Action(bluetooth)
    static final int VIEW_BLUETOOTH = 100;

    //Action(デバック用取得文字列)
    static final int VIEW_INPUT = 101;


    static final int CHECK_RTOD = 102;

    //Showmessageする文字列の受け渡し用
    static String msg;

    //HTTPSサービス確認用フラグ
    static boolean isSerHTTPS = false;

    //BLUETOOTHサービス確認用フラグ
    static boolean btServiceOn = false;

    static boolean btConnected = false;

    boolean isRun;
    static boolean isSleep;
    public static boolean LVFlag;

    TextView ShowTxt;
    Button bluetoothBtn, readButton, crashButton, RunButton, submitButton, signallingBtn, connectBtn, runBtn;
    EditText textbox;
    ScrollView scrollView;
    Firebase firebase = new Firebase();
    private int count = 0;
    IntentFilter BTIntentFilter, RDIntentFilter, mIntentFilter, fIntentFilter;
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity is onCreate()");

        //TODO:Define 3 types of variables and initialize (e.g. view, status, flags): How to format?
        //TODO: Weerawit test

        //For Firebase analytics
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this); //Google Analytics (Firebase) for logging specific events

        setContentView(R.layout.activity_main); //sets the initial layout to "activity_main.xml"
//        Firebase firebase = new Firebase();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keeps screen on

        //To continuously send signal to database to keep connecting to it.
        AtomicReference<ScheduledExecutorService> executor = new AtomicReference<>(Executors.newScheduledThreadPool(1));
        signallingBtn = findViewById(R.id.signallingButton);
        signallingBtn.setOnClickListener(v -> {
            Firebase firebase = new Firebase();
            if (signalFlag == 1) {
                signalFlag = 0;
                signallingBtn.setText("Deactivate Signalling");
                final int[] num = {0};
                executor.get().scheduleAtFixedRate(() -> {      // Schedule the task to run every 10 seconds
                    num[0]++;
                    firebase.databaseSignaling(num[0]);
                    System.out.println("sending signal: " + num[0]);
                }, 0, 10, TimeUnit.SECONDS);
            } else {
                signallingBtn.setText("Activate Signalling");
                signalFlag = 1;
                executor.get().shutdown();
                executor.set(Executors.newScheduledThreadPool(1));
            }

        });
//        Firebase firebase = new Firebase();
        Intent intent = new Intent(MainActivity.this, Firebase.class);
        startService(intent);
        //To submit any input text to the databases
        submitButton = findViewById(R.id.submitButton);
        textbox = findViewById(R.id.textBox);
        submitButton.setOnClickListener(v -> {
//            Firebase firebase = new Firebase();
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()); //Use timestamp as keys
            String text = textbox.getText().toString();
            // Write a message to the Realtime database (RealFireStore) and Cloud database (CloudFireStore)
            firebase.countRun();
            firebase.realFireStore("MOTOR", time, text);  //type of stored data can be anything simple.
//            Map<String, Object> data = new HashMap<>();  //For Cloud Firebase, the data needs to be of HashMap.
//            data.put(time, text);
//            CloudFireStore(date, data);
            Toast.makeText(this, "The message is sent to the database...", Toast.LENGTH_SHORT).show();
        });

        //To run number infinitely
        RunButton = findViewById(R.id.testButton);
        RunButton.setOnClickListener(v -> {
//            Firebase firebase = new Firebase();
            Toast.makeText(this, "The number is running now...", Toast.LENGTH_SHORT).show();
            firebase.countRun();
            Log.i("database", "The data is being sent to the database");
            new Thread(() -> {
                for (int i = 0; i <= 5; i++) {
                    long nanoTime = System.nanoTime();
                    long micros = (nanoTime / 100000000); // Extract microseconds from nanoseconds
                    String time = new SimpleDateFormat("HH:mm:ss:" + micros, Locale.getDefault()).format(new Date()); //Use timestamp as keys
                    //Map<String, Object> data = new HashMap<>();  //For Cloud Firebase, the data needs to be of HashMap.
                    //data.put(time, i);
                    //CloudFireStore(date, data);
//                Random random = new Random();
//                Generate a random integer between 0 and 120
                    int randomNumber = (new Random()).nextInt(120);
                    firebase.realFireStore("MOTOR", time, i);
//                Log.i("database", time + ":" + i);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        });

        //To read data stored in databases
        readButton = findViewById(R.id.readButton);
        readButton.setOnClickListener(v -> {
//            Firebase firebase = new Firebase();
            //Read everything in the Realtime database (RealFireStore) and Cloud database (CloudFireStore)
            firebase.realFireRead();
            firebase.cloudFireRead();
        });

        crashButton = findViewById(R.id.crashButton);
        crashButton.setOnClickListener(view -> {
            throw new RuntimeException("Test Crash"); // Force a crash event
        });
        Bluetooth bluetooth = new Bluetooth();

        scrollView = findViewById(R.id.scrollView);
        ShowTxt = findViewById(R.id.InputStream);
        bluetoothBtn = findViewById(R.id.bluetoothButton);
        bluetoothBtn.setOnClickListener(v -> {
            //Toast.makeText(MainActivity.this, "Bluetooth is starting...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bluetooth is starting...");
//            ActivityCompat.requestPermissions( this , new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
            bluetoothBtn.setEnabled(false);
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, 100);
            startService(new Intent(MainActivity.this, Bluetooth.class));

//           bluetooth.BluetoothEnable((BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE));
//            setContentView(R.layout.page2);

        });

        connectBtn = findViewById(R.id.connectButton);
        connectBtn.setOnClickListener(v -> {
            bluetooth.BluetoothConnection(this);
//            bluetooth.controlThread("START");
            firebase.countRun();
//            connectBtn.setEnabled(false);
        });
        /*
        runBtn = findViewById(R.id.runButton);
        runBtn.setOnClickListener(v -> {
            if (count == 1){
                runBtn.setText("PAUSE");
                bluetooth.controlThread("RESUME");
                firebase.countRun();
                count = 0;
            } else if (count == 0) {
                runBtn.setText("RESUME");
                bluetooth.controlThread("PAUSE");
                count = 1;

            }
        });

         */
    }

    BroadcastReceiver fReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Broadcast", "Firebase receive: "+ intent.getStringExtra("message"));
        }
    };
    BroadcastReceiver rReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("Broadcast", "receive: " + intent.getIntExtra("message", 0));
//            ShowTxt = findViewById(R.id.InputStream);
//            ShowTxt.setText(number);
        }
    };
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // このonReceiveでMainServiceからのIntentを受信する。
            long nanoTime = System.nanoTime();
            long micros = (nanoTime / 1000000); // Extract microseconds from nanoseconds
            String time = new SimpleDateFormat("HH:mm:ss:" + micros, Locale.getDefault()).format(new Date()); //Use timestamp as keys
            int VIEW = intent.getIntExtra("VIEW", 0);
            String message = intent.getStringExtra("message");
            //ShowMessage(VIEW, message); //受信した文字列を表示 - this shows the received string characters on the screen of the phone
//            Firebase firebase = new Firebase();
            ShowTxt.append(time + ":" + message + "\n");
            Log.i(TAG, "receive: " + message);
            firebase.realFireStore("MOTOR", time, message);

//            ShowTxt.setMovementMethod(new ScrollingMovementMethod());

            // Scroll the ScrollView to the bottom
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

        }
    };
    BroadcastReceiver permissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Objects.equals(intent.getAction(), "com.example.PERMISSION_REQUEST")) {
                String permission = intent.getStringExtra("permission"); //permission should be android.Manifest.permission.BLUETOOTH_CONNECT
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, 100);
                Log.i("permission", permission + "is being requested");
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, perform the desired action
                Log.e("Permission", "BLUETOOTH_CONNECT permission is granted");
            } else {
                // Permission denied, handle accordingly
                Log.e("Permission", "BLUETOOTH_CONNECT permission is denied");
//                ActivityCompat.requestPermissions( this, new String[]{android.Manifest.permission.}, 100);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity is onStart()");
        checkPermission(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity is onResume()");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("BLUETOOTH"));
        LocalBroadcastManager.getInstance(this).registerReceiver(permissionReceiver, new IntentFilter("PERMISSION_REQUEST"));
        LocalBroadcastManager.getInstance(this).registerReceiver(fReceiver, new IntentFilter("firebase"));
        LocalBroadcastManager.getInstance(this).registerReceiver(rReceiver, new IntentFilter("random"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity is onPause()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(permissionReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(fReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity is onDestroy()");
//        unregisterReceiver(mReceiver);
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(rReceiver);
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

    public void checkPermission(Context context){
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("permission", "CONNECT permission is not yet granted");
            ActivityCompat.requestPermissions( MainActivity.this , new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
        } else {
            Log.i("permission", "CONNECT permission is granted already");
        }
    }
}
