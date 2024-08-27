package com.example.fem21application;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

    //Action(Torques)
    static final int VIEW_TORQ = 12;
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

    //Action(AMS)
    static final int VIEW_AMS = 17;

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

    boolean isRun = false;
    static boolean isSleep;
    public static boolean LVFlag;
    boolean stopThread = false;
    static boolean pauseThread = false;
    static String GlobalMessage;
    static String GlobalTime;

    TextView mLV;


    TextView ShowTxt, ToDriverTxt;
    Button bluetoothBtn, runButton, RandomButton, submitButton, connectBtn, pauseButton;
    EditText textbox;
    ScrollView scrollView;
    Firebase firebase = new Firebase();
//    Bluetooth bluetooth = new Bluetooth();
    private Bluetooth bluetooth;
    private final int count = 0;
    private final int data_num = 100;
    private final int time_interval = 100;
    Thread firebaseThread;
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Bluetooth.LocalBinder binder = (Bluetooth.LocalBinder) service;
            bluetooth = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "MainActivity is onCreate()");

        //TODO:Define 3 types of variables and initialize (e.g. view, status, flags): How to format?
        //TODO: Weerawit test

        //For Firebase analytics
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this); //Google Analytics (Firebase) for logging specific events

        setContentView(R.layout.main_disp_temp); //sets the initial layout to "activity_main.xml"
//        Firebase firebase = new Firebase();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keeps screen on

        //To continuously send signal to database to keep connecting to it.
        AtomicReference<ScheduledExecutorService> executor = new AtomicReference<>(Executors.newScheduledThreadPool(1));

        FindID(); //TODO: 8/27


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
            firebase.realFireStore("VCMINFO", time, text);  //type of stored data can be anything simple.
            firebase.realFireStore("ERROR", time, text);  //type of stored data can be anything simple.
//            Map<String, Object> data = new HashMap<>();  //For Cloud Firebase, the data needs to be of HashMap.
//            data.put(time, text);
//            CloudFireStore(date, data);
            Toast.makeText(this, "The message is sent to the database...", Toast.LENGTH_SHORT).show();
        });

        //To run number infinitely
        RandomButton = findViewById(R.id.randomButton);
        RandomButton.setOnClickListener(v -> {
//            Firebase firebase = new Firebase();
            Toast.makeText(this, "The number is running now...", Toast.LENGTH_SHORT).show();
            firebase.countRun();
            Log.i("database", "The data is being sent to the database");
            new Thread(() -> {
                for (int i = 0; i <= 80; i++) {
                    long nanoTime = System.nanoTime();
                    long micros = (nanoTime / 100000); // Extract microseconds from nanoseconds
                    String time = new SimpleDateFormat("HH:mm:ss:" + micros, Locale.getDefault()).format(new Date()); //Use timestamp as keys
                    //Map<String, Object> data = new HashMap<>();  //For Cloud Firebase, the data needs to be of HashMap.
                    //data.put(time, i);
                    //CloudFireStore(date, data);
//                Random random = new Random();
//                Generate a random integer between 0 and 120
                    int ran_LV = (new Random()).nextInt(30);
                    int ran_velocity = (new Random()).nextInt(120);
                    int ran_HV = (new Random()).nextInt(600);
                    int ran_torque = (new Random()).nextInt(200);
                    int ran_100 = (new Random()).nextInt(100);
                    boolean ran_boolean = (new Random()).nextBoolean();
                    String set = ran_torque + "/" + ran_100 + "/" + ran_velocity + "/" + ran_LV;
                    firebase.realFireStore("VELOCITY", time, ran_velocity);
                    firebase.realFireStore("LV", time,ran_LV );
                    firebase.realFireStore("HV", time, ran_HV);
                    firebase.realFireStore("TORQUE", time, set );
//                    firebase.realFireStore("TORQUE1", time, ran_torque);
//                    firebase.realFireStore("TORQUE2", time, ran_100);
//                    firebase.realFireStore("TORQUE3", time, ran_velocity);
//                    firebase.realFireStore("TORQUE4", time, ran_LV);
                    firebase.realFireStore("ACC", time, ran_100);
                    firebase.realFireStore("BRAKE", time, ran_100);
                    firebase.realFireStore("BATTERY_LEVEL", time, ran_100);
                    firebase.realFireStore("STATUS", "BRAKE_SW",ran_boolean );
                    firebase.realFireStore("STATUS", "HV_STATUS",ran_boolean);
                    firebase.realFireStore("TEMPS", "BTR_TEMP",ran_velocity);
                    firebase.realFireStore("TEMPS", "MOTOR_TEMP",set);
                    firebase.realFireStore("TEMPS", "INV_TEMP",ran_LV);

//                Log.i("database", time + ":" + i);
                    try {
                        Thread.sleep(time_interval);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        });

        //To run continuously increasing number
        runButton = findViewById(R.id.runButton);
        runButton.setOnClickListener(v -> {
            Log.i("database", "The data is being sent to the database");
            firebase.countRun();
            firebaseThread = new Thread(() -> {
                while (true) {
//                    long nanoTime = System.nanoTime();
//                    long micros = (nanoTime / 100000); // Extract microseconds from nanoseconds
//                    String time = new SimpleDateFormat("HH:mm:ss:" + micros, Locale.getDefault()).format(new Date()); //Use timestamp as keys
//                    Log.i("database", GlobalMessage);
                    //Can be replaced by actual data
                    String[] dataPart = GlobalMessage.split("/");
                    //Determine the data group
                    String dataType = dataPart[0];
//                    Log.i("database", "Datatype: " + dataType);
                    switch (dataType) {
                        case "A":
                            dataA(GlobalMessage);
//                            Log.i("database", "Sending to : " + dataType);
                            break;
                        case "B":
                            dataB(GlobalMessage);
//                            Log.i("database", "Sending to : " + dataType);
                            break;
                        case "C":
                            dataC(GlobalMessage);
//                            Log.i("database", "Sending to : " + dataType);
                            break;
                    }
                    try {
                        Thread.sleep(time_interval);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
            firebaseThread.start();

//            new Thread(() -> {
//                for (int i = 0; i <= data_num; i++) {
//                    long nanoTime = System.nanoTime();
//                    long micros = (nanoTime / 100000); // Extract microseconds from nanoseconds
//                    String time = new SimpleDateFormat("HH:mm:ss:" + micros, Locale.getDefault()).format(new Date()); //Use timestamp as keys
//                    boolean ran_boolean = (new Random()).nextBoolean();
//                    String set = i + "/" + Math.ceil(0.85*i) + "/" + Math.ceil(1.5*i) + "/" + Math.ceil(0.7*i);
//                    firebase.realFireStore("VELOCITY", time, i+3);
//                    firebase.realFireStore("LV", time,i -5);
//                    firebase.realFireStore("HV", time, i+2);
//                    firebase.realFireStore("TORQUE", time, set);
////                    firebase.realFireStore("TORQUE1", time,i-10 );
////                    firebase.realFireStore("TORQUE2", time,i +5);
////                    firebase.realFireStore("TORQUE3", time,i -2);
////                    firebase.realFireStore("TORQUE4", time,i );
//                    firebase.realFireStore("ACC", time, i);
//                    firebase.realFireStore("BRAKE", time, i-4);
//                    firebase.realFireStore("BATTERY_LEVEL", time, i-12);
//                    firebase.realFireStore("STATUS", "BRAKE_SW",ran_boolean );
//                    firebase.realFireStore("STATUS", "HV_STATUS",ran_boolean);
//                    firebase.realFireStore("TEMPS", "BTR_TEMP",i+3);
//                    firebase.realFireStore("TEMPS", "MOTOR_TEMP",set);
//                    firebase.realFireStore("TEMPS", "INV_TEMP",i+6);
//
//                    try {
//                        Thread.sleep(time_interval);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//            }).start();
        });
        pauseButton = findViewById(R.id.pauseButton);
        pauseButton.setOnClickListener( v -> {
            if (pauseThread){
                pauseThread = false;
                bluetooth.resumeConnectedThread();
//                Log.i(bluetooth.TAG , "Resuming thread ");
                pauseButton.setText("PAUSE");
            } else {
                pauseThread = true;
                bluetooth.pauseConnectedThread();

//                Log.i(bluetooth.TAG, "pausing thread ");
                GlobalMessage = "";
                pauseButton.setText("RESUME");
            }
        });

//        Bluetooth bluetoothService = new Bluetooth();

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
//            firebase.countRun();
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

        //TODO: ProgressBar Code
        //ProgressBar progressBar = findViewById(R.id.bttBar);
        //progressBar.setProgress(60); // Set initial progress

        //updateProgressBarColor(progressBar);
    }

    private void updateProgressBarColor(ProgressBar progressBar) {
        int progress = progressBar.getProgress();

        int startColor;
        int endColor;
        float fraction;

        if (progress >= 50) {
            startColor = 0xFFFFFF00; // Yellow
            endColor = 0xFF00FF00;   // Green
            fraction = (progress - 50) / 50f; // Fraction between 50% and 100%
        } else {
            startColor = 0xFFFF0000; // Red
            endColor = 0xFFFFFF00;   // Yellow
            fraction = progress / 50f; // Fraction between 0% and 50%
        }

        int color = interpolateColor(startColor, endColor, fraction);

        LayerDrawable drawable = (LayerDrawable) progressBar.getProgressDrawable();
        ClipDrawable progressDrawable = (ClipDrawable) drawable.findDrawableByLayerId(android.R.id.progress);

        // Create a new ShapeDrawable with the chosen color
        ShapeDrawable shape = new ShapeDrawable(new RectShape());
        shape.getPaint().setColor(color);

        // Set the new ShapeDrawable as the progress drawable
        progressDrawable.setDrawable(shape);
    }

    private int interpolateColor(int startColor, int endColor, float fraction) {
        int startRed = (startColor >> 16) & 0xFF;
        int startGreen = (startColor >> 8) & 0xFF;
        int startBlue = startColor & 0xFF;

        int endRed = (endColor >> 16) & 0xFF;
        int endGreen = (endColor >> 8) & 0xFF;
        int endBlue = endColor & 0xFF;

        int red = (int) (startRed + (endRed - startRed) * fraction);
        int green = (int) (startGreen + (endGreen - startGreen) * fraction);
        int blue = (int) (startBlue + (endBlue - startBlue) * fraction);

        return 0xFF000000 | (red << 16) | (green << 8) | blue;
    }

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
        Intent intent = new Intent(this, Bluetooth.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        checkPermission(this);
        checkPermissionSCAN(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity is onResume()");
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("BLUETOOTH"));

        // Register the global receiver for Bluetooth broadcasts
        //getApplicationContext().registerReceiver(bReceiver, new IntentFilter("BLUETOOTH"), Context.RECEIVER_NOT_EXPORTED);

        // Register local receivers for other intents
        LocalBroadcastManager.getInstance(this).registerReceiver(permissionReceiver, new IntentFilter("PERMISSION_REQUEST"));
        LocalBroadcastManager.getInstance(this).registerReceiver(fReceiver, new IntentFilter("FIREBASE"));
        LocalBroadcastManager.getInstance(this).registerReceiver(rReceiver, new IntentFilter("random"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity is onPause()");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(rReceiver);
        //getApplicationContext().unregisterReceiver(bReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
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


    //Create broadcast receivers
    BroadcastReceiver fReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("Broadcast", "Firebase receive: "+ intent.getStringExtra("message"));
            String message = intent.getStringExtra("message");
            int VIEW = intent.getIntExtra("VIEW", 0);

            ToDriverTxt = findViewById(R.id.ToDriverText);
            ToDriverTxt.setText(message);
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
    BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // このonReceiveでMainServiceからのIntentを受信する。

            long nanoTime = System.nanoTime();
            long micros = (nanoTime / 1000000); // Extract microseconds from nanoseconds
            String time = new SimpleDateFormat("HH:mm:ss:" + micros, Locale.getDefault()).format(new Date()); //Use timestamp as keys
            String message = intent.getStringExtra("message");
            int VIEW = intent.getIntExtra("VIEW", 0);
            //ShowMessage(VIEW, message); //受信した文字列を表示 - this shows the received string characters on the screen of the phone
//            Firebase firebase = new Firebase();
            //Log.i(TAG, "receive: " + message);
            assert message != null;
            GlobalMessage = message.trim();
            GlobalTime = time;
            ShowTxt.append(time + ":" + GlobalMessage + "\n");
            //Log.i(TAG, "receive: " + GlobalMessage);

//            if (VIEW == 1) {
//                firebase.realFireStore("LV", time, message);
//            } else if (VIEW == 2) {
//                firebase.realFireStore("HV", time, message);
//            } else if (VIEW == 3) {
//                firebase.realFireStore("TEMPS", "MOTOR_TEMP", message);
//            } else if (VIEW == 4) {
//                firebase.realFireStore("TEMPS", "INV_TEMP",message);
//            } else if (VIEW == 7) {
//                firebase.realFireStore("VELOCITY", time, message);
//            } else if (VIEW == VIEW_TORQ) {
//                String set = message[0] + "/" + message[1] + "/" + message[2] + "/" + message[3];
//                firebase.realFireStore("TORQUE", time, message);
//            }

//            firebase.realFireStore("VELOCITY", time, message);
//            bluetooth.Write_file(message, "FEM21.txt", 1);
//            bluetooth.Write_file(message, "FEM21.csv", 1);

//            ShowTxt.setMovementMethod(new ScrollingMovementMethod());

            // Scroll the ScrollView to the bottom
            scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            if (VIEW==99){
                connectBtn.setEnabled(true);
            }

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
    public void checkPermission(Context context){
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("permission", "CONNECT permission is not yet granted");
            ActivityCompat.requestPermissions( MainActivity.this , new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
        } else {
            Log.i("permission", "CONNECT permission is granted already");
        }
    }
    public void checkPermissionSCAN(Context context){
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Log.e("permission", "SCAN permission is not yet granted");
            ActivityCompat.requestPermissions( MainActivity.this , new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 100);
        } else {
            Log.i("permission", "SCAN permission is granted already");
        }
    }

    //A/<low system voltage>/<high system voltage>/<motor temp[0]>x<motor temp[1]>x<motor temp[2]>x<motor temp[3]>/<inv temp>/A
    @SuppressLint("SetTextI18n")
    public void dataA(String datasetA){
        //Can be replaced by actual data
//        String dataA = datasetA;
//        String dataA = "A/150/300/10x20x30x40/60/A";
        //Split the string by "/"
        String[] partA = datasetA.split("/");
        if (partA.length != 6) {
            throw new IllegalArgumentException("DataA format Error");
        }

        //Extract values
        String startA = partA[0];

        //For Low System Voltage
        int lowSystemVoltage = Integer.parseInt(partA[1]);

        // For High System Voltage
        int highSystemVoltage = Integer.parseInt(partA[2]);

        //For Motor Temperature
        //Split motor temperature by "x" for motor temperature
        String[] motorTemps = partA[3].split("x");
        if (motorTemps.length != 4){
            throw new IllegalArgumentException("Motor Temperature length error");
        }
        int[] motorTemperature = new int[4];
        //Convert string in motorTemps into int type
        for (int i =0; i < motorTemps.length; i++){
            motorTemperature[i] = Integer.parseInt(motorTemps[i]);
        }
        String MotorTemps = motorTemperature[0] + "/" + motorTemperature[1] + "/" + motorTemperature[2] + "/" + motorTemperature[3];
        //For Inverter Temperature
        int inverterTemperature = Integer.parseInt(partA[4]);

        //Output the values (Can be replaced by Broadcast)
//        System.out.println("DataGroup: " + startA);

//        System.out.println("Low System Voltage: " + lowSystemVoltage + " V");
//        System.out.println("High System Voltage: " + highSystemVoltage + " V");
//        System.out.println("Motor Temperatures: " + motorTemperature[0] + "°C, " + motorTemperature[1] + "°C, " + motorTemperature[2] + "°C, " + motorTemperature[3] + "°C");
//        System.out.println("Inverter Temperature: " + inverterTemperature + "°C");
        FindID();
//        Log.i("database", "DataGroup: " + startA);
        firebase.realFireStore("LV", GlobalTime,lowSystemVoltage);
        mLV.append(""+ lowSystemVoltage); //TODO: 8/27
        firebase.realFireStore("HV", GlobalTime,highSystemVoltage);
        firebase.realFireStore("TEMPS", "MOTOR_TEMP", MotorTemps);
        firebase.realFireStore("TEMPS", "INV_TEMP",inverterTemperature);
    }

    //B/<RTD[0]>x<RTD[1]>x<RTD[2]>x<RTD[3]>/<vcm info>/<velocity>/<torque [0]>x<torque [1]>x<torque [2]>x<torque [3]>/B
    public void dataB(String datasetB){
        //Can be replaced by actual data
        //String dataB = datasetB
//        String dataB = "B/10x20x30x40/100/50/200x300x400x500/B";

        //Split the string by "/"
        String[] partB = datasetB.split("/");
        if (partB.length != 6) {
            throw new IllegalArgumentException("DataB format Error");
        }

        //Extract values
        String startB = partB[0];

        //For RTD
        //Split RTD value by "x"
        String[] originalRTD = partB[1].split("x");
        if (originalRTD.length != 4) {
            throw new IllegalArgumentException("RTD data Length error");
        }
        int[] RTD = new int[4];
        //Convert to the integer type
        for (int i =0; i < originalRTD.length; i++ ){
            RTD[i] = Integer.parseInt(originalRTD[i]);
        }

        //For VCM
        String VCMInfo = partB[2];

        //For velocity
        int Velocity = Integer.parseInt(partB[3]);

        //For Torque
        //Split Torque by "x"
        String[] originalTorque = partB[4].split("x");
        if (originalTorque.length != 4) {
            throw new IllegalArgumentException("Torque data length error");
        }
        int[] Torque = new int[4];
        //Convert to Integer
        for (int i =0; i < originalTorque.length; i++){
            Torque[i] = Integer.parseInt(originalTorque[i]);
        }
        String Torques = Torque[0] + "/" + Torque[1] + "/" + Torque[2] + "/" + Torque[3];

        //Output the values (Can be replaced by Broadcast)
//        System.out.println("DataGroup: " + startB);
//        System.out.println("RTD: " + RTD[0] + " ?" + RTD[1] + " ?" + RTD[2] + " ?" + RTD[3] + " ?");
//        System.out.println("VCM Info: " + VCMInfo + " !");
//        System.out.println("Velocity: " + Velocity + "m/s?");
//        System.out.println("Torque: " + Torque[0] + "N.m, " + Torque[1] + "N.m, " + Torque[2] + "N.m, " + Torque[3] + "N.m");

        firebase.realFireStore("VELOCITY", GlobalTime, Velocity);
        firebase.realFireStore("TORQUE", GlobalTime, Torques);
    }

    //C/<AMS>/<BSPD>/<IMD>/<TC>/<ABS>/<VDC>/<80kW>/C
    public void dataC(String datasetC){
        //Can be replaced by actual data
        //String dataC = datasetC
//        String dataC = "C/10/20/30/40/50/60/80kW/C";

        //Split by "/"
        String[] partC = datasetC.split("/");
        if (partC.length != 9) {
            throw new IllegalArgumentException("DataC format Error");
        }

        //Extract Values
        String startC = partC[0];
        //For AMS
        int AMS = Integer.parseInt(partC[1]);
        //For BSPD
        int BSPD = Integer.parseInt(partC[2]);
        //For IMD
        int IMD = Integer.parseInt(partC[3]);
        //For TC
        int TC = Integer.parseInt(partC[4]);
        //For ABS
        int ABS = Integer.parseInt(partC[5]);
        //For VDC
        int VDC = Integer.parseInt(partC[6]);
        //For a fixed 80kW
//        int fixed80 = Integer.parseInt(partC[7]); TODO: fix this

        //Output the values (Can be replaced by Broadcast)
//        System.out.println("DataGroup: " + startC);
//        System.out.println("AMS: " + AMS + " ?");
//        System.out.println("BSPD: " + BSPD + " !");
//        System.out.println("IMD: " + IMD + " #");
//        System.out.println("TC: " + TC + " &");
//        System.out.println("ABS: " + ABS + " %");
//        System.out.println("VDC: " + VDC + " $");
//        System.out.println(": " + fixed80 + " kW");

        int ran_velocity = (new Random()).nextInt(120);
        int ran_100 = (new Random()).nextInt(100);
        boolean ran_boolean = (new Random()).nextBoolean();
        firebase.realFireStore("ACC", GlobalTime, ran_100);
        firebase.realFireStore("BRAKE", GlobalTime, ran_100);
        firebase.realFireStore("BATTERY_LEVEL", GlobalTime, ran_100);
        firebase.realFireStore("STATUS", "BRAKE_SW",ran_boolean );
        firebase.realFireStore("STATUS", "HV_STATUS",ran_boolean);
        firebase.realFireStore("TEMPS", "BTR_TEMP",ran_velocity);

    }

    private void FindID(){
        mLV = findViewById(R.id.lvData);
    }

}
