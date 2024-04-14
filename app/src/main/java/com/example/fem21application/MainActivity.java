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
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    TextView counterTxt, welcomeTxt, ShowTxt;
    Button bluetoothBtn;
    Button ToHomeBtn;
    Button ToPage2Btn;
    ConstraintLayout main, page2;
    FrameLayout container, container2;
    Bluetooth bluetooth = new Bluetooth();

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
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


        // このonReceiveでMainServiceからのIntentを受信する。
        //Try using intent.getExtra only.
        //Bundle bundle = intent.getExtras();
        //int VIEW = bundle.getInt("VIEW");
        //String message = bundle.getString("message");
        //ShowMessage(VIEW, message); //受信した文字列を表示 - this shows the received string characters on the screen of the phone
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // このonReceiveでMainServiceからのIntentを受信する。
                //Try using intent.getExtra only.
                int VIEW = intent.getIntExtra("VIEW", 0);
                String message = intent.getStringExtra("message");
                //Bundle bundle = intent.getExtras();
                //int VIEW = bundle.getInt("VIEW");
                //String message = bundle.getString("message");
                //ShowMessage(VIEW, message); //受信した文字列を表示 - this shows the received string characters on the screen of the phone
                ShowTxt = findViewById(R.id.InputStream);
                ShowTxt.setText(message);
            }
        };
        // "BLUETOOTH" Intentフィルターをセット  Filter to receive only the message with code BLUETOOTH
        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction("BLUETOOTH");
        registerReceiver(mReceiver, mIntentFilter, Context.RECEIVER_NOT_EXPORTED);

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



        bluetoothBtn = findViewById(R.id.bluetoothbtn);

        bluetoothBtn.setOnClickListener(v -> {
            //Toast.makeText(MainActivity.this, "Bluetooth is starting...", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Bluetooth is starting...");
            bluetoothBtn.setEnabled(false);
            bluetooth.BluetoothEnable();
//            setContentView(R.layout.page2);
            bluetooth.BluetoothConnection();
        });

//        ToHomeBtn.setOnClickListener(v -> {
//            Intent changeLayout = new Intent(MainActivity.this, Bluetooth_wee.class);
//            //startActivity(changeLayout);
//            launcher.launch(changeLayout);
//            setContentView(R.layout.activity_main);
//        });

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
    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // User enabled Bluetooth
                    // Proceed with your Bluetooth-related logic
                    Log.d(TAG, "Bluetooth is turning ON");
                } else {
                    // User canceled or didn't enable Bluetooth
                    // Handle accordingly
                    Log.d(TAG, "Bluetooth isn't turning ON");
                }
            });

    public void checkPermission(){
        if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Connecting permission is needed", Toast.LENGTH_SHORT).show();
            Log.i("permission", "CONNECT permission is needed");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
        } else {
            Log.i("permission", "CONNECT permission is not need");
        }

        if (ContextCompat.checkSelfPermission(getBaseContext(), android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "SCAN permission is needed", Toast.LENGTH_SHORT).show();
            Log.i("permission", "SCAN permission is needed");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 100);
        } else{
            Log.i("permission", "SCAN permission is not needed");
        }

    }
}
