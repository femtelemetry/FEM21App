package com.example.fem21application;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    double lowSystemVoltage = 0.0;
    double batterySOC = 0.0;
    double hv_maxtemp234 = 0.0;
    double powerConsumption = 0.0;
    double minvoltage = 0.0;

    double finalLowSystemVoltage;
    int finalBatterySOC;
    double final_hv_maxtemp234;
    double final_powerconsumption;
    double final_minvoltage;

    double[] motor_temp = new double[4];

    double final_motortemp_fl;
    double final_motortemp_fr;
    double final_motortemp_rr;
    double final_motortemp_rl;

    double[] inv_temp = new double[4];

    double final_invtemp_fl;
    double final_invtemp_fr;
    double final_invtemp_rr;
    double final_invtemp_rl;

    String MotorTemps;
    String InvTemps;

    double velocity = 0.0;
    int final_velocity;

    double throttle_percentage = 0.0;
    int final_throttle_percentage;

    int[] ready_to_drive = new int[4];

    int final_RtoD_fl;
    int final_RtoD_fr;
    int final_RtoD_rr;
    int final_RtoD_rl;

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
    static double G_lowSystemVoltage;
    static double G_batterySOC;
    static double G_hv_maxtemp234;
    static double G_powerConsumption;
    static String G_MotorTemps;
    static String G_InvTemps;


    TextView m_lv_voltage;
    TextView vMtr1, vMtr2, vMtr3, vMtr4;
    TextView vVelo;
    ProgressBar bttBar, accelBar, brakeBar;

    TextView vBattCharge, vPowerCons;

    TextView m_hv_maxtemp234;

    TextView m_motor_temp_fl, m_motor_temp_fr, m_motor_temp_rr, m_motor_temp_rl;
    TextView m_igbt_temp_fl, m_igbt_temp_fr, m_igbt_temp_rr, m_igbt_temp_rl;
    TextView m_cp_temp_fl, m_cp_temp_fr, m_cp_temp_rr, m_cp_temp_rl;

    //dataC
    TextView m_velocity;
    ImageView i_rtod_fl, i_rtod_fr, i_rtod_rr, i_rtod_rl;

    ImageView drivsig;

    //dataD

    TextView m_vcminfo;
    TextView m_diagnosticnum_fl, m_diagnosticnum_fr, m_diagnosticnum_rr, m_diagnosticnum_rl;




    private Handler handler;

    TextView ShowTxt, ToDriverTxt;
    Button bluetoothBtn;
    Button runButton;
    Button submitButton;
    Button connectBtn;
    Button pauseButton;
    EditText textbox;
    ScrollView scrollView;
    Firebase firebase = new Firebase();
//    Bluetooth bluetooth = new Bluetooth();
    private Bluetooth bluetooth;
    private final int count = 0;
    private final int data_num = 100;
    private final int time_interval = 100;
    Thread firebaseThread;
    Thread DataCategorizer;
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
        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //keeps screen on
        }
        catch(Exception e){
            Log.e(TAG, "ANTI SLEEP ERR");
        }
        // Initialize the Handler using the main thread's Looper
        handler = new Handler(Looper.getMainLooper());

        //To continuously send signal to database to keep connecting to it.
        try {
            AtomicReference<ScheduledExecutorService> executor = new AtomicReference<>(Executors.newScheduledThreadPool(1));
        }catch (Exception e){
            Log.e(TAG, "DATABASE CONNECTION ATTEMPT ERR");
        }
        FindID(); //TODO: 8/27

//        Firebase firebase = new Firebase();
        Intent FirebaseIntent = new Intent(MainActivity.this, Firebase.class);
        Intent BluetoothIntent = new Intent(MainActivity.this, Bluetooth.class);
        startService(FirebaseIntent);
        startService(BluetoothIntent);

        try {
            textbox = findViewById(R.id.textBox);

            scrollView = findViewById(R.id.scrollView);
            ShowTxt = findViewById(R.id.InputStream);

            connectBtn = findViewById(R.id.connectButton);
            connectBtn.setOnClickListener(v -> {
                bluetooth.BluetoothConnection(this);
                connectBtn.setEnabled(false);
                Log.d(TAG, "CONNECTING WITH BLUETOOTH DEVICE");
            });

            DataCategorizer = new Thread(() -> {
                    while (true) {
                        try {
                            //Can be replaced by actual data
                            String[] dataPart = GlobalMessage.split("/");
                            //Determine the data group
                            String dataType = dataPart[0];
//                    Log.i("DATABASE", "Datatype: " + dataType);
                            switch (dataType) {
                                case "A":
                                    try {
                                        dataA(GlobalMessage);
                                    } catch (Exception e) {
                                        Log.e("DATAERR", "DATA A ERR");
                                    }
//                            Log.i("DATABASE", "Sending to : " + dataType);
                                    break;
                                case "B":
                                    try {
                                        dataB(GlobalMessage);
                                    } catch (Exception e) {
                                        Log.e("DATAERR", "DATA B ERR");
                                    }
//                            Log.i("DATABASE", "Sending to : " + dataType);
                                    break;
                                case "C":
                                    try {
                                        dataC(GlobalMessage);
                                    } catch (Exception e) {
                                        Log.e("DATAERR", "DATA C ERR");
                                    }
//                            Log.i("database", "Sending to : " + dataType);
                                    break;
                                case "D":
                                    try {
                                        dataD(GlobalMessage);
                                    } catch (Exception e) {
                                        Log.e("DATAERR", "DATA D ERR");
                                    }
//                            Log.i("database", "Sending to : " + dataType);
                                    break;
                                case "E":
                                    try {
                                        dataE(GlobalMessage);
                                    } catch (Exception e) {
                                        Log.e("DATAERR", "DATA E ERR");
                                    }
//                            Log.i("database", "Sending to : " + dataType);
                                    break;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "SORT ERROR");
                        }
                        try {
                            Thread.sleep(time_interval / 10);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });
                DataCategorizer.start();
                //To run continuously increasing number
                runButton = findViewById(R.id.runButton);
                runButton.setOnClickListener(v -> {
                    Log.i("DATABASE", "The data is being sent to the database");
                    firebase.countRun();
                    firebaseThread = new Thread(() -> {
                        try {
                            dataUploader();
                            Thread.sleep(time_interval);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    firebaseThread.start();
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

            scrollView = findViewById(R.id.scrollView);
            ShowTxt = findViewById(R.id.InputStream);
        }catch(Exception e){
            Log.e(TAG, "buttons error");
        }
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

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity is onStart()");
        Intent intent = new Intent(this, Bluetooth.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        checkPermission(getApplicationContext());
        checkPermissionSCAN(getApplicationContext());
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

            try {
                ToDriverTxt = findViewById(R.id.ToDriverText);
                ToDriverTxt.setText(message);

                drivsig = findViewById(R.id.driverSignal2);

                if (ToDriverTxt.getText().equals("1")) {
                    drivsig.setImageResource(R.drawable.speed);
                } else if (ToDriverTxt.getText().equals("2")) {
                    drivsig.setImageResource(R.drawable.keep);
                } else if (ToDriverTxt.getText().equals("3")) {
                    drivsig.setImageResource(R.drawable.slow);
                }
            }catch(Exception e){
                Log.e(TAG, "TO driver text err");
            }
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
//            Log.i(TAG, "receive: " + GlobalMessage);
            if (VIEW==404){
                Log.d(bluetooth.TAG, "RESTARTING BLUETOOTH BROADCAST RECEIVED");
                bluetooth.BluetoothConnection(getApplicationContext());
            }

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
    public void dataUploader(){
        firebase.realFireStore("LV", "LV",G_lowSystemVoltage);
        firebase.realFireStore("HV", "BTR",G_batterySOC);
        firebase.realFireStore("HV", "TEMP",G_hv_maxtemp234);
        firebase.realFireStore("PWT", GlobalTime,G_powerConsumption);
        firebase.realFireStore("TEMPS", "MOTOR_TEMP", G_MotorTemps);
        firebase.realFireStore("TEMPS", "INV_TEMP", G_InvTemps);
    }

    //A/<low system voltage>/<high system voltage>/<motor temp[0]>x<motor temp[1]>x<motor temp[2]>x<motor temp[3]>/<inv temp>/A
    @SuppressLint("SetTextI18n")
    public void dataA(String datasetA){
        //Can be replaced by actual data
//        String dataA = datasetA;
//        String dataA = "A/150/300/10x20x30x40/60/89/A";
        //Split the string by "/"
        String[] partA = datasetA.split("/");
        if (partA.length != 6) {
//            Log.e(TAG, "DataA format Error");
        }

        //Extract values
        String startA = partA[0];

        double lowSystemVoltage = 0.0;
        //For Low System Voltage
        try {
            lowSystemVoltage = Double.parseDouble(partA[1]);
        }catch(Exception e){
//            Log.e("DATAErr", "LV Electrical System Voltage failed to parse.");
        }
        //double finalLowSystemVoltage = lowSystemVoltage;

        double batterySOC = 0.0;
        // For Battery State of Charge
        try {
            batterySOC = Double.parseDouble(partA[2]);
        }
        catch(Exception e){
//            Log.e("DATAErr", "HV Electrical System Charge failed to parse.");
        }

        double hv_maxtemp234 = 0.0;
        try {
            hv_maxtemp234 = Double.parseDouble(partA[4]);
        }
        catch(Exception e){
//            Log.e("DATAErr", "HV Electrical System Max Temperature failed to parse.");
        }

        double powerConsumption = 0.0;
        // For Battery State of Charge
        try {
            powerConsumption = Double.parseDouble(partA[6]);
        }
        catch(Exception e){
//            Log.e("DATAErr", "Power Consumption failed to parse.");
        }

        /*
        //For Motor Temperature
        //Split motor temperature by "x" for motor temperature
        String[] motorTemps = partA[3].split("x");
        if (motorTemps.length != 4){
            throw new IllegalArgumentException("Motor Temperature length error");
        }
        int[] motorTemperature = new int[4];
        //Convert string in motorTemps into int type
        for (int i =0; i < motorTemps.length; i++){
            try {
                motorTemperature[i] = Integer.parseInt(motorTemps[i]);
            }
            catch(Exception e){
                Log.e("DATAErr", "MotorTempErr");
            }
        }
        String MotorTemps = motorTemperature[0] + "/" + motorTemperature[1] + "/" + motorTemperature[2] + "/" + motorTemperature[3];
        //For Inverter Temperature
        int inverterTemperature = 0;
        try {
            inverterTemperature = Integer.parseInt(partA[4]);
        }
        catch(Exception e){
            Log.e("DATAErr", "InvTempErr");
        }

        int batteryCharge = 0;
        try {
            batteryCharge = Integer.parseInt(partA[5]);
        }catch(Exception e){
            Log.e("DATAErr", "BttChargeErr");
        }
        */

        FindID();
        G_lowSystemVoltage = lowSystemVoltage;
        G_batterySOC = batterySOC;
        G_hv_maxtemp234 = hv_maxtemp234;
        G_powerConsumption = powerConsumption;

        /*
        firebase.realFireStore("LV", "LV",lowSystemVoltage);
        firebase.realFireStore("HV", "BTR",batterySOC);
        firebase.realFireStore("HV", "TEMP",hv_maxtemp234);
        firebase.realFireStore("PWT", GlobalTime,powerConsumption);
        */

        //firebase.realFireStore("TEMPS", "MOTOR_TEMP", MotorTemps);
        //firebase.realFireStore("TEMPS", "INV_TEMP",inverterTemperature);

        // Use the Handler to update the TextView on the main thread
        //TODO 8/29

        double finalLowSystemVoltage = lowSystemVoltage;

        int finalBatterySOC = (int) Math.round(batterySOC);
        double final_hv_maxtemp234 = hv_maxtemp234;
        double final_minvoltage = minvoltage;
        double final_powerconsumption = powerConsumption;

        handler.post(new Runnable() {
            @Override
            public void run() {
                m_lv_voltage.setText(String.valueOf(finalLowSystemVoltage));
                m_hv_maxtemp234.setText(String.valueOf(final_hv_maxtemp234));
                //vMtr1.setText(String.valueOf(motorTemperature[0]));
                //vMtr2.setText(String.valueOf(motorTemperature[1]));
                //vMtr3.setText(String.valueOf(motorTemperature[2]));
                //vMtr4.setText(String.valueOf(motorTemperature[3]));
                if(finalBatterySOC > 0){
                    bttBar.setProgress(finalBatterySOC); // Set initial progress
                }
                updateProgressBarColor(bttBar);
                vBattCharge.setText(String.valueOf(finalBatterySOC));
                if(final_powerconsumption < 16000) {
                    vPowerCons.setText(String.valueOf(final_powerconsumption));
                }
            }
        });
    }

    //B/<RTD[0]>x<RTD[1]>x<RTD[2]>x<RTD[3]>/<vcm info>/<velocity>/<torque [0]>x<torque [1]>x<torque [2]>x<torque [3]>/B
    public void dataB(String datasetB){
        //Can be replaced by actual data
        //String dataB = datasetB
//        String dataB = "B/10x20x30x40/100/50/200x300x400x500/B";

        //Split the string by "/"
        String[] partB = datasetB.split("/");
        if (partB.length != 6) {
//            Log.e(TAG, "DataB format Error");
        }

        //Extract values
        String startB = partB[0];

        //For MotorTemp
        //Split Mtoro temperature by "x"
        String[] received_motortemp = partB[1].split("x");
        if (received_motortemp.length != 4) {
            //throw new IllegalArgumentException("RTD data Length error");
        }
        double[] motor_temp = new double[4];
        //Convert to the double type
        for (int i =0; i < received_motortemp.length; i++ ) {
            try {
                motor_temp[i] = Double.parseDouble(received_motortemp[i]);
            }
            catch (Exception e){
//                Log.e("DATAErr", "Motor Temperature failed to parse.");
            }
        }

        double final_motortemp_fl = motor_temp[0];
        double final_motortemp_fr = motor_temp[1];
        double final_motortemp_rr = motor_temp[2];
        double final_motortemp_rl = motor_temp[3];

        //For InvTemp
        //Split inverter cold plate temperature by "x"
        String[] received_invtemp = partB[3].split("x");
        if (received_invtemp.length != 4) {
            //throw new IllegalArgumentException("RTD data Length error");
        }
        double[] inv_temp = new double[4];
        //Convert to the double type
        for (int i =0; i < received_invtemp.length; i++ ){
            try{
                inv_temp[i] = Double.parseDouble(received_invtemp[i]);
            }
            catch(Exception e) {
//                Log.e("DATAErr", "Cold Plate Temperature failed to parse.");
            }
        }

        double final_invtemp_fl = inv_temp[0];
        double final_invtemp_fr = inv_temp[1];
        double final_invtemp_rr = inv_temp[2];
        double final_invtemp_rl = inv_temp[3];

        /*
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


         */
        //Output the values (Can be replaced by Broadcast)
//        System.out.println("DataGroup: " + startB);
//        System.out.println("RTD: " + RTD[0] + " ?" + RTD[1] + " ?" + RTD[2] + " ?" + RTD[3] + " ?");
//        System.out.println("VCM Info: " + VCMInfo + " !");
//        System.out.println("Velocity: " + Velocity + "m/s?");
//        System.out.println("Torque: " + Torque[0] + "N.m, " + Torque[1] + "N.m, " + Torque[2] + "N.m, " + Torque[3] + "N.m");

        String MotorTemps = motor_temp[0] + "/" + motor_temp[1] + "/" + motor_temp[2] + "/" + motor_temp[3];
//        firebase.realFireStore("TEMPS", "MOTOR_TEMP", MotorTemps);
        String InvTemps = inv_temp[0] + "/" + inv_temp[1] + "/" + inv_temp[2] + "/" + inv_temp[3];
//        firebase.realFireStore("TEMPS", "INV_TEMP", InvTemps);
        //firebase.realFireStore("VELOCITY", GlobalTime, Velocity);
        //firebase.realFireStore("TORQUE", GlobalTime, Torques);

        G_MotorTemps = MotorTemps;
        G_InvTemps = InvTemps;


        // Use the Handler to update the TextView on the main thread
        //TODO 8/29
        handler.post(new Runnable() {
            @Override
            public void run() {
                //vVelo.setText(String.valueOf(Velocity));
                m_motor_temp_fl.setText(String.valueOf(final_motortemp_fl));
                m_motor_temp_fr.setText(String.valueOf(final_motortemp_fr));
                m_motor_temp_rr.setText(String.valueOf(final_motortemp_rr));
                m_motor_temp_rl.setText(String.valueOf(final_motortemp_rl));
                if(final_invtemp_fl > 0){
                    m_cp_temp_fl.setText(String.valueOf(final_invtemp_fl));
                }
                else{
                    m_cp_temp_fl.setText("-");
                }
                if(final_invtemp_fr > 0){
                    m_cp_temp_fr.setText(String.valueOf(final_invtemp_fr));
                }
                else{
                    m_cp_temp_fr.setText("-");
                }
                if(final_invtemp_rr > 0){
                    m_cp_temp_rr.setText(String.valueOf(final_invtemp_rr));
                }
                else{
                    m_cp_temp_rr.setText("-");
                }
                if(final_invtemp_rl > 0){
                    m_cp_temp_rl.setText(String.valueOf(final_invtemp_rl));
                }
                else{
                    m_cp_temp_rl.setText("-");
                }
            }
        });
    }

    //C/<AMS>/<BSPD>/<IMD>/<TC>/<ABS>/<VDC>/<80kW>/C
    public void dataC(String datasetC){
        //Can be replaced by actual data
        //String dataC = datasetC
//        String dataC = "C/10/20/30/40/50/60/80kW/C";

        //Split by "/"
        String[] partC = datasetC.split("/");
        if (partC.length != 9) {
//            Log.e(TAG, "DataC format Error");
        }

        //Extract Values
        String startC = partC[0];

        double velocity = 0.0;
        //For Low System Voltage
        try {
            velocity = Double.parseDouble(partC[1]);
        }catch(Exception e){
//            Log.e("DATAErr", "Velocity failed to parse.");
        }

        int final_velocity = (int) velocity;

        /*
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

         */
        //Output the values (Can be replaced by Broadcast)
//        System.out.println("DataGroup: " + startC);
//        System.out.println("AMS: " + AMS + " ?");
//        System.out.println("BSPD: " + BSPD + " !");
//        System.out.println("IMD: " + IMD + " #");
//        System.out.println("TC: " + TC + " &");
//        System.out.println("ABS: " + ABS + " %");
//        System.out.println("VDC: " + VDC + " $");
//        System.out.println(": " + fixed80 + " kW");

        //int ran_velocity = (new Random()).nextInt(120);
        //int ran_100 = (new Random()).nextInt(100);
        //boolean ran_boolean = (new Random()).nextBoolean();
        //firebase.realFireStore("ACC", GlobalTime, ran_100);
        //firebase.realFireStore("BRAKE", GlobalTime, ran_100);
        //firebase.realFireStore("BATTERY_LEVEL", GlobalTime, ran_100);
        //firebase.realFireStore("STATUS", "BRAKE_SW",ran_boolean );
        //firebase.realFireStore("STATUS", "HV_STATUS",ran_boolean);
        //firebase.realFireStore("TEMPS", "BTR_TEMP",ran_velocity);

        // Use the Handler to update the TextView on the main thread
        //TODO 8/29
        handler.post(new Runnable() {
            @Override
            public void run() {
                //vVelo.setText(String.valueOf(Velocity));
                //TODO 09/2
                m_velocity.setText(String.valueOf(final_velocity));
            }
        });
    }

    public void dataD(String datasetD){
        //Can be replaced by actual data
        //String dataD = datasetD
//        String dataD = "C/10/20/30/40/50/60/80kW/C";

        //Split by "/"
        String[] partD = datasetD.split("/");
        if (partD.length != 9) {
//            Log.e(TAG, "DataD format Error");
        }

        //Extract Values
        String startC = partD[0];

        double throttle_percentage = 0.0;
        //For Low System Voltage
        try {
            throttle_percentage = Double.parseDouble(partD[1]);
        }catch(Exception e){
//            Log.e("DATAErr", "Throttle percentage failed to parse.");
        }

        int final_throttle_percentage = (int) throttle_percentage;

        /*
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

         */
        //Output the values (Can be replaced by Broadcast)
//        System.out.println("DataGroup: " + startC);
//        System.out.println("AMS: " + AMS + " ?");
//        System.out.println("BSPD: " + BSPD + " !");
//        System.out.println("IMD: " + IMD + " #");
//        System.out.println("TC: " + TC + " &");
//        System.out.println("ABS: " + ABS + " %");
//        System.out.println("VDC: " + VDC + " $");
//        System.out.println(": " + fixed80 + " kW");

        //int ran_velocity = (new Random()).nextInt(120);
        //int ran_100 = (new Random()).nextInt(100);
        //boolean ran_boolean = (new Random()).nextBoolean();
        //firebase.realFireStore("ACC", GlobalTime, ran_100);
        //firebase.realFireStore("BRAKE", GlobalTime, ran_100);
        //firebase.realFireStore("BATTERY_LEVEL", GlobalTime, ran_100);
        //firebase.realFireStore("STATUS", "BRAKE_SW",ran_boolean );
        //firebase.realFireStore("STATUS", "HV_STATUS",ran_boolean);
        //firebase.realFireStore("TEMPS", "BTR_TEMP",ran_velocity);

        // Use the Handler to update the TextView on the main thread
        //TODO 8/29
        handler.post(new Runnable() {
            @Override
            public void run() {
                //vVelo.setText(String.valueOf(Velocity));
                //TODO 09/2
                accelBar.setProgress(final_throttle_percentage);
            }
        });
    }

    public void dataE(String datasetE){
        //Can be replaced by actual data
        //String dataE = datasetE
//        String dataE = "C/10/20/30/40/50/60/80kW/C";

        //Split by "/"
        String[] partE = datasetE.split("/");
        if (partE.length != 9) {
//            Log.e(TAG, "DataE format Error");
        }

        //Extract Values
        String startC = partE[0];

        //For RTD
        //Split RTD value by "x"
        String[] received_RToD  = new String[]{""};
        try{
            received_RToD = partE[2].split("x");
        }catch(Exception e){
            Log.e("DATAErr", "Ready-to-Drive failed to parse.");
        }

        if (received_RToD.length != 4) {
            //throw new IllegalArgumentException("RTD data Length error");
        }
        int[] ready_to_drive = new int[4];
        //Convert to the integer type
        for (int i =0; i < received_RToD.length; i++ ) {
            try {
                ready_to_drive[i] = Integer.parseInt(received_RToD[i]);
            }
            catch(Exception e){
//                Log.e("DATAErr", "Ready-to-Drive failed to parse.");
            }
        }

        int final_RtoD_fl = ready_to_drive[0];
        int final_RtoD_fr = ready_to_drive[1];
        int final_RtoD_rr = ready_to_drive[2];
        int final_RtoD_rl = ready_to_drive[3];

        if(final_RtoD_fl == 1 && final_RtoD_fr == 1 && final_RtoD_rr == 1 && final_RtoD_rl == 1){
            //rtod_flag = true;
        }
        /*
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

         */
        //Output the values (Can be replaced by Broadcast)
//        System.out.println("DataGroup: " + startC);
//        System.out.println("AMS: " + AMS + " ?");
//        System.out.println("BSPD: " + BSPD + " !");
//        System.out.println("IMD: " + IMD + " #");
//        System.out.println("TC: " + TC + " &");
//        System.out.println("ABS: " + ABS + " %");
//        System.out.println("VDC: " + VDC + " $");
//        System.out.println(": " + fixed80 + " kW");

        //int ran_velocity = (new Random()).nextInt(120);
        //int ran_100 = (new Random()).nextInt(100);
        //boolean ran_boolean = (new Random()).nextBoolean();
        //firebase.realFireStore("ACC", GlobalTime, ran_100);
        //firebase.realFireStore("BRAKE", GlobalTime, ran_100);
        //firebase.realFireStore("BATTERY_LEVEL", GlobalTime, ran_100);
        //firebase.realFireStore("STATUS", "BRAKE_SW",ran_boolean );
        //firebase.realFireStore("STATUS", "HV_STATUS",ran_boolean);
        //firebase.realFireStore("TEMPS", "BTR_TEMP",ran_velocity);

        // Use the Handler to update the TextView on the main thread
        //TODO 8/29
        handler.post(new Runnable() {
            @Override
            public void run() {
                //vVelo.setText(String.valueOf(Velocity));
                //TODO 09/2
                if(final_RtoD_fl == 1){
                    i_rtod_fl.setBackgroundColor(getColor(R.color.good_green));
                }
                else{
                    i_rtod_fl.setBackgroundColor(getColor(R.color.dead_grey));
                }
                if(final_RtoD_fr == 1){
                    i_rtod_fr.setBackgroundColor(getColor(R.color.good_green));
                }
                else{
                    i_rtod_fr.setBackgroundColor(getColor(R.color.dead_grey));
                }
                if(final_RtoD_rr == 1){
                    i_rtod_rr.setBackgroundColor(getColor(R.color.good_green));
                }
                else{
                    i_rtod_rr.setBackgroundColor(getColor(R.color.dead_grey));
                }
                if(final_RtoD_rl == 1){
                    i_rtod_rl.setBackgroundColor(getColor(R.color.good_green));
                }
                else{
                    i_rtod_rl.setBackgroundColor(getColor(R.color.dead_grey));
                }
            }
        });
    }

    private void FindID(){
        m_lv_voltage = findViewById(R.id.lvData);
        vMtr1 = findViewById(R.id.mtrFLtext);
        vMtr2 = findViewById(R.id.mtrFRtext);
        vMtr3 = findViewById(R.id.mtrRLtext);
        vMtr4 = findViewById(R.id.mtrRRtext);
        vVelo = findViewById(R.id.veloText);
        bttBar = findViewById(R.id.bttBar);
        accelBar = findViewById(R.id.accelProgressBar);
        //vBattCharge = findViewById(R.id.bttText); // REMOVED 9/12 for acceleration
        vBattCharge = findViewById(R.id.bttText2);
        vPowerCons = findViewById(R.id.powerconsumptiontext);
        m_hv_maxtemp234 = findViewById(R.id.hvData);
        m_motor_temp_fl = findViewById(R.id.mtrFLtext);
        m_motor_temp_fr = findViewById(R.id.mtrFRtext);
        m_motor_temp_rr = findViewById(R.id.mtrRRtext);
        m_motor_temp_rl = findViewById(R.id.mtrRLtext);
        /*
        m_igbt_temp_fl = findViewById(R.id.iGBTtext);
        m_igbt_temp_fr = findViewById(R.id.invFRtext);
        m_igbt_temp_rr = findViewById(R.id.invRRtext);
        m_igbt_temp_rl = findViewById(R.id.invRLtext);*/
        m_cp_temp_fl = findViewById(R.id.invFLtext);
        m_cp_temp_fr = findViewById(R.id.invFRtext);
        m_cp_temp_rr = findViewById(R.id.invRRtext);
        m_cp_temp_rl = findViewById(R.id.invRLtext);

        m_velocity = findViewById(R.id.veloText);
        i_rtod_fl = findViewById(R.id.rtodFL);
        i_rtod_fr = findViewById(R.id.rtodFR);
        i_rtod_rr = findViewById(R.id.rtodRR);
        i_rtod_rl = findViewById(R.id.rtodRL);
        drivsig = findViewById(R.id.driverSignal2);
    }


}
