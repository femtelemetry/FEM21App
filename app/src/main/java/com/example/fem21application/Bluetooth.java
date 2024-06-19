package com.example.fem21application;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        SendBroadcast(0,"BLUETOOTH_SERVICE IS STARTED");

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
                    SendBroadcast(0,"CONNECTING TO: " + deviceName);
                    bluetoothSocket.connect();
                    mConnectedThread = new ConnectedThread(bluetoothSocket);
//                    Log.d("Thread", "mConnectedThread IS CREATED ");
                    mConnectedThread.start();
                } catch (IOException e) {
                    SendBroadcast(0,"FAILED CONNECTING TO:" + deviceName);
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
                            Log.i("Thread", "receive:" + message);
//                        categorizeMessage(message);
//                        SendBroadcast(MainActivity.VIEW_INPUT, m);
                    }
                } catch (IOException e) {
                    Log.e("Thread", e.toString());
                    Log.e("Thread", "UNABLE TO READ/WRITE, STOPPING SERVICE");
                    SendBroadcast(0,"UNABLE TO READ/WRITE, STOPPING SERVICE");
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

    private void SendBroadcast(int VIEW, String message) {
        // IntentをブロードキャストすることでMainActivityへデータを送信
        Intent intent = new Intent();
        intent.setAction("BLUETOOTH");  //Set code as BLUETOOTH for the receiver to know where the information is from
        intent.putExtra("VIEW", VIEW);
        intent.putExtra("message", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

//    private void categorizeMessage(String message){
//        int values_count = 0;
//        Write_file(message.trim(), "data_log.csv", 0);
//        if (message.trim().startsWith("A") && message.trim().endsWith("A")) {
//            try{
//                appendLog(message.trim());
//                SendBroadcast(MainActivity.VIEW_INPUT, message.trim()); //デバック用
//
//                String[] values;
//                values = message.trim().split("/", 0);
//                values_count = values.length;
//
//
//                //LV解析
//                try{
//                    if (!values[MainActivity.VIEW_LV].contains("-")) {
//                        //values[MainActivity.VIEW_LV] = "ON";
//
//                        // 小数第一位以下を表示しないように文字列切り取り
//                        if (Float.parseFloat(values[MainActivity.VIEW_LV]) < 30.0) {
//                            if (values[MainActivity.VIEW_LV].length() >= 3) {
//                                values[MainActivity.VIEW_LV] = values[MainActivity.VIEW_LV].substring(0, 3);
//                            }
//                        }
//
//                        else {
//                            if (values[MainActivity.VIEW_LV].length() >= 4) {
//                                values[MainActivity.VIEW_LV] = values[MainActivity.VIEW_LV].substring(0, 4);
//                            }
//                        }
//                        SendBroadcast(MainActivity.VIEW_LV, values[MainActivity.VIEW_LV]);
//                        AddCloud("LV", values[MainActivity.VIEW_LV]); //クラウド送信情報として登録
//
//
//                    }
//                    else {
//                        values[MainActivity.VIEW_LV] = "-----";
//                    }
//                    SendBroadcast(MainActivity.VIEW_LV, values[MainActivity.VIEW_LV]); //MainActivityに送信し文字列表示
//                }
//                catch(Exception e){
//                    Log.w(TAG, "LV Error");
//                }
//
//                //HV解析
//                try{
//                    if (!values[MainActivity.VIEW_HV].contains("-")) {
//                        SendBroadcast(MainActivity.VIEW_HV, values[MainActivity.VIEW_HV]);
//                        AddCloud("HV", values[MainActivity.VIEW_HV]);
//                        MainActivity.HVFlag = true; //HVONフラグをON
//                    }
//                    else {
//                        MainActivity.HVFlag = false; //HVONフラグをOFF
//                        values[MainActivity.VIEW_HV] = "-----";
//                    }
//                    SendBroadcast(MainActivity.VIEW_HV, values[MainActivity.VIEW_HV]);
//                    MainActivity.HVFlag = !values[MainActivity.VIEW_HV].contains("-");
//                }
//                catch(Exception e){
//                    Log.w(TAG, "HV Error");
//                }
//
//                int maxMT = 0;
//
//                //MOTOR温度解析
//                try{
//                    String[] MTs;
//                    float[] MTInt = new float[4];
//                    final int indexNum=4;
//                    MTs = values[MainActivity.VIEW_MT].trim().split("x", 0); //FR,FL,RR,RLの情報に分解
//
//                    for (int n = 0; n < indexNum; n++) {
//                        MTInt[n] = 0;
//                        //Log.i(TAG, "mt index=" + n);
//                        Log.i(TAG, "mt" + n + "=" + MTs[n]);
//
//                        if(MTs[n].contains("-")){
//                            MTInt[n] = 0;
//                        }
//                        else{
//                            MTInt[n] = Float.valueOf(MTs[n]);
//                        }
//
//                        // 画面には四輪の内の最大値のみ表示する
//                        if (MTInt[n] >= maxMT) {
//                            maxMT = Math.round(MTInt[n]);
//                        }
//
//                        // クラウドには四輪とも送信
//                        switch (n) {
//                            case 0:
//                                AddCloud("MOTOR1", MTs[0]);
//                                break;
//                            case 1:
//                                AddCloud("MOTOR2", MTs[1]);
//                                break;
//                            case 2:
//                                AddCloud("MOTOR3", MTs[2]);
//                                break;
//                            case 3:
//                                AddCloud("MOTOR4", MTs[3]);
//                                break;
//                        }
//                    }
//
//                    if (maxMT == 0) {
//                        SendBroadcast(MainActivity.VIEW_MT, "-----");
//                    }
//                    else {
//                        SendBroadcast(MainActivity.VIEW_MT, Integer.toString(maxMT));
//                    }
//                }
//                catch(Exception e){
//                    Log.w(TAG, "A - MOTOR Error");
//                    e.printStackTrace();
//                }
//
//                //INV温度解析
//                try{
//                    if (!values[MainActivity.VIEW_INV].contains("-")) {
//                        values[MainActivity.VIEW_INV] = values[MainActivity.VIEW_INV].split("\\.", 0)[0]; //整数にする
//                        AddCloud("INV", values[MainActivity.VIEW_INV]);
//                    }
//                    else {
//                        values[MainActivity.VIEW_INV] = "-----";
//                    }
//                    SendBroadcast(MainActivity.VIEW_INV, values[MainActivity.VIEW_INV]);
//                }
//                catch(Exception e){
//                    Log.w(TAG,"INV Error");
//                }
//
//
//
//                // クラウドのデータストアへの登録
//                objV.saveInBackground(new DoneCallback() {
//                    @Override
//                    public void done(NCMBException e) {
//                        if (e != null) {
//                            //保存に失敗した場合の処理
//                            e.printStackTrace();
//                        } else {
//                            //保存に成功した場合の処理
//
//                        }
//                    }
//                });
//
//                if(maxMT >= 115){ //高温判定
//                    if(!(MainActivity.isHITEMP)) { //高温表示
//                        SendBroadcast(MainActivity.LAYOUT_HITEMP, "YES");
//                        MainActivity.isHITEMP = true;
//                    }
//                }
//                else if(!values[MainActivity.VIEW_INV].contains("-")){
//                    if(Integer.parseInt(values[MainActivity.VIEW_INV]) >= 40){
//                        if(!(MainActivity.isHITEMP)) { //高温表示
//                            SendBroadcast(MainActivity.LAYOUT_HITEMP, "YES");
//                            MainActivity.isHITEMP = true;
//                        }
//                    }
//                    else{
//                        if(MainActivity.isHITEMP){ //高温非表示
//                            SendBroadcast(MainActivity.LAYOUT_HITEMP, "NO");
//                            MainActivity.isHITEMP = false;
//                        }
//                    }
//                }
//                else{
//                    if(MainActivity.isHITEMP){ //高温非表示
//                        SendBroadcast(MainActivity.LAYOUT_HITEMP, "NO");
//                        MainActivity.isHITEMP = false;
//                    }
//                }
//            }
//            catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//    }

    public void Write_file(String message, String filename, int n) {
        //String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        //String baseDir = "Try to put any path here";
        //String filePath = baseDir + File.separator + filename;
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
        try {
            // Check if the parent directory exists; if not, create it
            File parentDirectory = file.getParentFile();
            assert parentDirectory != null;
            if (!parentDirectory.exists() && !parentDirectory.mkdirs()) {
                // Handle the case where directory creation fails
                throw new IOException("Failed to create parent directory: " + parentDirectory);
            }
            // Create or overwrite the file
            FileWriter writer = new FileWriter(file,true);

            //for adding time stamp to each data input
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timestampedData = LocalDateTime.now().format(formatter) + ":";
            writer.write(timestampedData);

            // Write data to the file
            writer.write(message + "\n");
            /*
            for (int i = 0; i < n-1; i++){
                writer.write(arr[i] + "/");
            }
//            writer.write(message[0] + "\n");

             */

            // Close the FileWriter
            writer.close();
            System.out.println("File created or updated successfully.");
//            Toast.makeText(this, "Data created or updated successfully!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e(TAG, "ERROR: " + e);
            // Handle the exception
        }
    }

    private String readFromFile(String filename) {
        try {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
            //FileInputStream fileInputStream = openFileInput("my_data.txt");
            FileInputStream fileInputStream = new FileInputStream(file);
            StringBuilder stringBuilder = new StringBuilder();
            int c;
            while ((c = fileInputStream.read()) != -1) {
                stringBuilder.append((char) c);
            }
            fileInputStream.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            Log.e(TAG, "ERROR: " + e);
            return "";
        }
    }

}