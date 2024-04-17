package com.example.fem21application;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class Bluetooth extends Service {
    public static BluetoothAdapter bluetoothAdapter = null;
    public BluetoothDevice bluetoothDevice;
    private final String TAG = "BLUETOOTH_SERVICE";
    private String macAddress = "";
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String deviceName = "ESP32";
    public boolean stopThread;
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

    public void BluetoothConnection(Context context) {
        //Proceed with querying devices
        MainActivity Main = new MainActivity();
        Log.i(TAG, "BluetoothConnection() IS STARTING");
        Main.checkPermission(context);
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
        private final MainActivity Main = new MainActivity();

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
                    mConnectedThread = new ConnectedThread(bluetoothSocket);
//                    Log.d("Thread", "mConnectedThread IS CREATED ");
                    mConnectedThread.start();
                } catch (IOException e) {
                    SendBroadcast("FAILED CONNECTING TO:" + deviceName);
                    try {
                        bluetoothSocket.close();
                        Log.e("Thread", "SOCKET CONNECTION FAILED, STOPPING SERVICE");
                        Log.e("Thread", e.toString());
                        stopSelf();
                    } catch (IOException e2) {
                        Log.e("Thread", "SOCKET CLOSING FAILED, STOPPING SERVICE:");
                        Log.e("Thread",  e2.toString());
                        stopSelf();
                    }
                } catch (IllegalStateException e) {
                Log.e("Thread", "CONNECTED THREAD START FAILED, STOPPING SERVICE");
                Log.e("Thread", e.toString());
                stopSelf();
            }
        }
        public void closeSocket() {
            try {
                //Don't leave Bluetooth sockets open when leaving activity
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e("Thread", "SOCKET CLOSING FAILED, STOPPING SERVICE");
                Log.e("Thread", e.toString());
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
                    int length = inputStream.read(buffer);
                    String message = new String(buffer, 0, length);
                    if (!message.trim().isEmpty()) {  //To skip any empty message
//                            Log.i("Thread", "receive:" + message);
                        SendBroadcast(message);
                    }
                } catch (IOException e) {
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

    private void SendBroadcast(String message) {
        // IntentをブロードキャストすることでMainActivityへデータを送信
        Intent intent = new Intent();
        intent.setAction("BLUETOOTH");  //Set code as BLUETOOTH for the receiver to know where the information is from
        //intent.putExtra("VIEW", VIEW);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}