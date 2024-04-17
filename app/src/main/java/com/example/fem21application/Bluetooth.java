package com.example.fem21application;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends Service {
    private static final int REQUEST_ENABLE_BT = 1;

    public static BluetoothAdapter bluetoothAdapter = null;
    public BluetoothDevice bluetoothDevice;
    public BluetoothSocket bluetoothSocket;


    String text1 = "Hello World";
    private String TAG = "BLUETOOTH_SERVICE";
    private String macAddress = "";
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String deviceName = "ESP32";
    private boolean IsRunning = true;
    private boolean stopThread;
    public boolean isGranted = false;
    Thread BLThread;
    private ConnectingThread mConnectingThread;
    private ConnectedThread mConnectedThread;

    public Bluetooth(){}
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BLUETOOTH_SERVICE IS CREATED");
        stopThread = false;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BLUETOOTH_SERVICE IS STARTED");
//
//        Intent sendIntent = new Intent().setAction("BLUETOOTH");
//        sendIntent.putExtra("message", "BLUETOOTH_SERVICE IS STARTED");
//        LocalBroadcastManager.getInstance(this).sendBroadcast(sendIntent);
        SendBroadcast("BLUETOOTH_SERVICE IS STARTED");

        MainActivity Main = new MainActivity();
        BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager != null) {
            bluetoothAdapter = bluetoothManager.getAdapter();
            // Check if the device supports Bluetooth
            if (bluetoothAdapter == null) {
                Log.e(TAG, "BLUETOOTH NOT SUPPORTED BY DEVICE, STOPPING SERVICE");
                stopSelf();
            } else {
                Log.i(TAG, "BluetoothAdapter IS CREATED");
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    Main.checkPermission(this);
                    startActivity(enableBtIntent);
                    Log.d(TAG, "Bluetooth is turning ON...");
                } else {
                    Log.d(TAG, "Bluetooth is already ON");
                }
                if (bluetoothAdapter == null) {
                    Log.d(TAG, "BluetoothAdapter become null");
                }
            }
        } else {
            Log.e(TAG, "BluetoothManager is NULL");
        }
        return super.onStartCommand(intent, flags, startId);
//        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopThread = true;
        if (mConnectedThread != null){
            mConnectedThread.closeStreams();
        }
        if (mConnectingThread != null){
            mConnectingThread.closeSocket();
        }

        Log.d(TAG, "BLUETOOTH_SERVICE is onDestroy()");

    }

    public void BluetoothEnable(BluetoothManager bluetoothManager) {
        try {
            //1. Get the Bluetooth Adapter: The BluetoothAdapter is required for any and all Bluetooth activity.
            Log.d(TAG, "BluetoothEnable() is starting...");
//            BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
//            final BluetoothManager bluetoothManager = (BluetoothManager) this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
//        BluetoothManager bluetoothManager = this.getSystemService(Context.BLUETOOTH_SERVICE)
//            Log.d(TAG, "Bluetooth Manager is working fine...");
            if (bluetoothManager != null) {
                bluetoothAdapter = bluetoothManager.getAdapter();

                // Check if the device supports Bluetooth
                if (bluetoothAdapter == null) {
                    Log.e(TAG, "BLUETOOTH NOT SUPPORTED BY DEVICE, STOPPING SERVICE");
                    //Toast.makeText(this, "Device doesn't support Bluetooth", Toast.LENGTH_SHORT).show();
                    stopSelf();
                } else {
                    Log.i(TAG, "Device supports Bluetooth");
                    if (!bluetoothAdapter.isEnabled()) {
//                        bluetoothAdapter.enable();
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(enableBtIntent);
                        Log.d(TAG, "Bluetooth is turning ON...");
//                        MainActivity Main = new MainActivity();
//                        Main.launcher.launch(enableBtIntent);
//                      Main.checkPermission();
//                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                            Log.i("permission", "CONNECT permission is needed");
//                            ActivityCompat.requestPermissions(Main, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
//                        } else {
//                            Log.i("permission", "CONNECT permission is not need");
//                        }
//                        Intent intent = new Intent("com.example.PERMISSION_REQUEST");
//                        intent.putExtra("permission", android.Manifest.permission.BLUETOOTH_CONNECT);
//                        sendBroadcast(intent);
                        if (!bluetoothAdapter.isEnabled()){
                            Log.e(TAG, "Bluetooth is not yet turned ON");
                        } else {
                            Log.d(TAG, "Bluetooth is turned ON");
                        }

                    } else {
                        Log.d(TAG, "Bluetooth is already ON");
                    }
                }
            } else {
                Log.e(TAG, "BluetoothManager is not available");
                // Toast.makeText(this, "BLManager not working", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException stopping Android O background scanner");
        } catch (RuntimeException e) {
            Log.e(TAG, "Unexpected runtime exception stopping Android O background scanner", e);
        }
    }

    public void BluetoothConnection(Context context) {
        //Proceed with querying devices
        MainActivity Main = new MainActivity();
        Log.i(TAG, "BluetoothConnection() IS STARTING");
        Main.checkPermission(context);
//        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
//        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter!=null){
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            Log.d(TAG, "getting paired devices:" + pairedDevices);
            for (BluetoothDevice device : pairedDevices) {
                Log.i(TAG, "Name->" + device.getName() + "    " + "MAC->" + device.getAddress());
                if (device.getName().equals(deviceName)) {
                    macAddress = device.getAddress();
                    Log.d(TAG, deviceName + "is found");
                    Log.d(TAG, "MAC - > " + macAddress);
                } else {
                    Log.i(TAG, "Cannot find:" + deviceName + " yet");
                }
            }
        } else{
            Log.e(TAG, "bluetoothAdapter is null");
        }
        Log.d(TAG, "Mac:" + macAddress);
        if (BluetoothAdapter.checkBluetoothAddress(macAddress)) {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(macAddress); // You can use this BluetoothDevice instance to establish a connection or perform other Bluetooth operation.

            Log.i(TAG, "Bluetooth device is found");
            // Proceed with BluetoothDevice usage

//            BLThread = new connectThread(bluetoothDevice, context);
//            BLThread.start();

            mConnectingThread = new ConnectingThread(bluetoothDevice, context);
            mConnectingThread.start();
            Log.i(TAG, "Bluetooth Thread is starting");
        } else {
            // Handle invalid Bluetooth address
            Log.e(TAG, "Invalid Bluetooth address");
//            Toast.makeText(this, "Invalid Bluetooth Address", Toast.LENGTH_SHORT).show();
        }
    }
    private class ConnectingThread extends Thread {
        private BluetoothSocket bluetoothSocket;
        private final BluetoothDevice bluetoothDevice;
        private final Context ThreadContext;
        private final Intent sendIntent = new Intent("BLUETOOTH");
        private MainActivity Main = new MainActivity();

        public ConnectingThread(BluetoothDevice bluetoothDevice, Context context) {
            this.bluetoothDevice = bluetoothDevice;
            ThreadContext = context;
        }

        public void run() {
                Main.checkPermission(ThreadContext);
                BluetoothSocket tmp = null;
                try {
                    tmp = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    Log.d(TAG, "SOCKET CREATED:" + tmp.toString());
                } catch (IOException e) {
                    Log.e(TAG, "SOCKET CREATION FAILED, STOPPING SERVICE");
                    stopSelf();
                }
                bluetoothSocket = tmp;

                bluetoothAdapter.cancelDiscovery(); // Cancelling discovery as it may slow down connection
                Log.d("Thread", "Try connecting to " + deviceName);

                //Proceed with any task
//                BluetoothCommunication(bluetoothSocket, ThreadContext);
                try {
                    SendBroadcast("CONNECTING TO: " + deviceName);
                    bluetoothSocket.connect();
                    mConnectedThread = new ConnectedThread(bluetoothSocket, ThreadContext);
                    mConnectedThread.start();
                } catch (IOException e) {
                    SendBroadcast("FAILED CONNECTING TO:" + deviceName);
                    try {
                        bluetoothSocket.close();
                        Log.d("Thread", "SOCKET CONNECTION FAILED, STOPPING SERVICE" + e.toString());
                        stopSelf();
                    } catch (IOException e2) {
                        Log.e("Thread", "SOCKET CLOSING FAILED, STOPPING SERVICE:" + e2.toString());
                        stopSelf();
                    }
                } catch (IllegalStateException e) {
                Log.e("Thread", "CONNECTED THREAD START FAILED, STOPPING SERVICE" + e.toString());
                stopSelf();
            }
        }
        public void closeSocket() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.d("Thread", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }
    }

    // New Class for Connected Thread == BluetoothConnection();
    private class ConnectedThread extends Thread {
        private final InputStream inputStream;
        private final OutputStream outputStream;


        //creation of the connect thread
        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = bluetoothSocket.getInputStream();
                tmpOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.d("Thread", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                stopSelf();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        public void run() {
            Log.i("Thread", "START RECEIVING");
            byte[] buffer = new byte[1024];
            // Keep looping to listen for received messages
            while (!stopThread) {
                try {
                    if (inputStream.available() > 0){
                        int length = inputStream.read(buffer);
                        String message = new String(buffer, 0, length);
                        Log.i("Thread", "receive:" + message);
                        SendBroadcast(message);
                    }
                }
                catch (IOException e) {
                    Log.e("Thread", e.toString());
                    Log.e("Thread", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                    SendBroadcast("UNABLE TO READ/WRITE, STOPPING SERVICE");
                    stopSelf();
                    break;
                }
            }
        }

        public void closeStreams() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                //insert code to deal with this
                Log.d("Thread", "STREAM CLOSING FAILED, STOPPING SERVICE");
                stopSelf();
            }
        }
    }
    class connectThread extends Thread {
        BluetoothDevice bluetoothDevice;
        Context ThreadContext;
        private final Intent sendIntent = new Intent("BLUETOOTH");
        MainActivity Main = new MainActivity();

        connectThread(BluetoothDevice device, Context context) {
            this.bluetoothDevice = device;
            ThreadContext = context;
        }

        public void run() {
            try {

                Main.checkPermission(ThreadContext);
//                //Creating a socket to the bluetoothDevice (create a pathway to the device)
//                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                    Log.i("permission", "CONNECT permission is needed");
//                    ActivityCompat.requestPermissions((Activity) mContext, new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
//                } else {
//                    Log.i("permission", "CONNECT permission is not need");
//                }

                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                bluetoothAdapter.cancelDiscovery();
                Log.d("Thread", "Try connecting to " + deviceName);

                sendIntent.putExtra("message", "Connecting to " + deviceName);
                LocalBroadcastManager.getInstance(ThreadContext).sendBroadcast(sendIntent);
//                ContinueSend(ThreadContext);
                //Start connecting to the bluetoothDevice
                bluetoothSocket.connect();
                //Toast.makeText(MainActivity.this,"Connecting to " + deviceName , Toast.LENGTH_SHORT).show();
                Log.d("Thread", "Connected to " + deviceName);
                sendIntent.putExtra("message", "Connected to " + deviceName);
                LocalBroadcastManager.getInstance(ThreadContext).sendBroadcast(sendIntent);

                //Proceed with any task
                BluetoothCommunication(bluetoothSocket, ThreadContext);

                //outputStream = bluetoothSocket.getOutputStream();
                //inputStream = bluetoothSocket.getInputStream();

                //Toast.makeText(MainActivity.this, "Completely connected to " + deviceName, Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                Log.e("Thread", "THREAD CREATING ERROR: " + e);
                cancel();
            }
        }
        public void cancel() {
            try {
                bluetoothSocket.close();
                MainActivity.btConnected = false;
            } catch (IOException e) {
                Log.e(TAG,"Could not close the client socket");
                //sendBroadcast(MainActivity.VIEW_STATUS, "Could not close the client socket");
            }
        }

    }
    public void ContinueSend(){

        new Thread(() -> {
            for (int i=0; i<=10; i++) {
                int rand = new Random().nextInt(100);
                Intent intent = new Intent().setAction("random");
                intent.putExtra("message", rand);

                try {
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                    Log.i("Broadcast", "send:" + rand);
                } catch (RuntimeException e) {
                    Log.e("Broadcast", "send: nothing");
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    public void BluetoothCommunication(BluetoothSocket bluetoothSocket, Context context) throws IOException {
        OutputStream outputStream = bluetoothSocket.getOutputStream();
        InputStream inputStream = bluetoothSocket.getInputStream();
        if (outputStream==null || inputStream == null){
            Log.e("Thread", "Stream error");
            return;  // Exit the method if streams are null
        }
        Log.i("Thread", "START RECEIVING");
        byte[] buffer = new byte[1024];
        new Thread(()->{
            while(IsRunning){
                try {
                    if (inputStream.available() > 0){
                        int length = inputStream.read(buffer);
                        String message = new String(buffer, 0, length);
//                        Log.i("Thread", "receive:" + message);
                        Intent SendIntent = new Intent().setAction("BLUETOOTH");
                        SendIntent.putExtra("message", message);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(SendIntent);
                    }
                } catch (RuntimeException | IOException e){
                    Log.e("Thread", "receive: ERROR");
                    break;
                }
            }
        }).start();
    }

    private void SendBroadcast(String message) {
        // IntentをブロードキャストすることでMainActivityへデータを送信
        Intent intent = new Intent();
        intent.setAction("BLUETOOTH");  //Set code as BLUETOOTH for the receiver to know where the information is from
        //intent.putExtra("VIEW", VIEW);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//        Log.i("Broadcast", "send:" + message);
    }

    public void checkPermission(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(MainActivity.this, "Connecting permission is needed", Toast.LENGTH_SHORT).show();
            Log.i("permission", "CONNECT permission is needed");

//            ActivityCompat.requestPermissions( MainActivity.this , new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 100);
        } else {
            Log.i("permission", "CONNECT permission is not need");
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
//            Toast.makeText(MainActivity.this, "SCAN permission is needed", Toast.LENGTH_SHORT).show();
            Log.i("permission", "SCAN permission is needed");
//            ActivityCompat.requestPermissions( (Activity) MainActivity.this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, 100);
        } else{
            Log.i("permission", "SCAN permission is not needed");
        }

    }
    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                // User enabled Bluetooth
                // Proceed with your Bluetooth-related logic
                Log.i(TAG, "Bluetooth is enabled");
            } else {
                // User canceled or didn't enable Bluetooth
                // Handle accordingly
                Log.i(TAG, "Bluetooth isn't  enabled");
            }
        }
    }

     */

}