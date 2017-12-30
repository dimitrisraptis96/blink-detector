package com.example.dimitris.blinkdetector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class Bluetooth {

    private final String TAG = "Bluetooth";

    ConnectThread mConnectThread;

    ConnectedThread mConnectedThread;

    BluetoothAdapter mBluetoothAdapter;

    void main(){

        setBluetooth();

        mConnectThread = new ConnectThread(getPairedBluetoothDevice());
        mConnectThread.start();
    }


    public void setBluetooth() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Log.i(TAG, "Device doesn't support Bluetooth.");
//            Toast.makeText(getApplicationContext(), "Your device doesn't support Bluetooth.", Toast.LENGTH_SHORT).show();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Log.i(TAG,"Device hasn't Bluetooth enabled.");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    //Get the paired devices. Thus, user should connect with arduino before using the app
    public BluetoothDevice getPairedBluetoothDevice(){

        BluetoothDevice mDevice = null;

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.i(TAG,"Device name: " + device.getName()); //name of device
                Log.i(TAG,"Device address:" + device.getAddress()); //MAC address

                mDevice = device;
            }
        }
        return mDevice;
    }


    private class ConnectThread extends Thread {

        private final BluetoothSocket mSocket;

        private final BluetoothDevice mDevice;

        private final String BLUETOOTH_UUID = "00001101-0000-1000-8000-00805f9b34fb";

        private /*static */final UUID MY_UUID = UUID.fromString(BLUETOOTH_UUID);

        public ConnectThread(BluetoothDevice device) {

            BluetoothSocket tmp = null;
            mDevice = device;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG,"BluetoothSocket from UUID failed to be created.");
            }
            mSocket = tmp;
        }

        public void run() {
            mBluetoothAdapter.cancelDiscovery();
            try {
                mSocket.connect();
            } catch (IOException connectException) {
                try {
                    Log.e(TAG,"BluetoothSocket failed to connect.");
                    mSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG,"BluetoothSocket failed to be closed.");
                }
                //Create thread to connect
                mConnectedThread = new ConnectedThread(mSocket);
                mConnectedThread.start();

                return;
            }
        }

        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) {
                Log.e(TAG,"BluetoothSocket failed to be closed.");
            }
        }
    }

    private class ConnectedThread extends Thread {

        private final BluetoothSocket mSocket;

        private final InputStream mInStream;

        private final OutputStream mOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG,"Failed to get socket's streams.  ");
            }
            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        public void run() {

            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {

                    bytes += mInStream.read(buffer, bytes, buffer.length - bytes);
                    for(int i = begin; i < bytes; i++) {
                        if(buffer[i] == "#".getBytes()[0]) {
                            mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                            begin = i + 1;
                            if(i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG,"Error reading socket's InputStream.");
                    break;
                }
            }
        }
        public void write(byte[] bytes) {
            try {
                mOutStream.write(bytes);
            } catch (IOException e) { }
        }
        public void cancel() {
            try {
                mSocket.close();
            } catch (IOException e) { }
        }
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //Handle passed arguments
            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int)msg.arg1;
            int end = (int)msg.arg2;

            switch(msg.what) {
                case 1:
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    break;
            }
        }
    };
    }
