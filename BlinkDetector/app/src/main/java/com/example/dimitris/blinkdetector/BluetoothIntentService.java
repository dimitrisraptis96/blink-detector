package com.example.dimitris.blinkdetector;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import static android.os.Looper.getMainLooper;


public class BluetoothIntentService extends IntentService {

    private static final String TAG = "BluetoothIntentService";

    // Unique UUID for this application
    private static  final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    // Member fields
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mDevice;
    private BluetoothSocket mSocket;

    private InputStream mInStream;
    private OutputStream mOutStream;

    public BluetoothIntentService() {
        super("BluetoothIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        setBluetoothAdapter();
        setBluetoothDevice();
    }

    public void showToast(final String text){
        Handler toastHandler = new Handler(getMainLooper());

        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setBluetoothAdapter() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Log.i(TAG, "Device doesn't support Bluetooth.");
            this.showToast("Device doesn't support Bluetooth.");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Log.i(TAG, "Device hasn't Bluetooth enabled.");
            // TODO: call startActivityFOrResult within a service
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, 1);
            this.showToast("Enabling bluetooth.");
        }
    }

    //Get the paired devices. Thus, user should connect with arduino before using the app
    public void setBluetoothDevice() {

        mDevice = null;

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                Log.i(TAG, "Device name: " + device.getName()); //name of device
                Log.i(TAG, "Device address:" + device.getAddress()); //MAC address

                mDevice = device;
            }
        }
    }

    private void setSocket() {

        BluetoothSocket tmp = null;
        try {
            tmp = mDevice.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            Log.e(TAG, "BluetoothSocket from UUID failed to be created.", e);
        }
        mSocket = tmp;
    }

    private void connectToDevice() {
        mBluetoothAdapter.cancelDiscovery();
        try {
            mSocket.connect();
        } catch (IOException connectException) {
            try {
                Log.e(TAG, "BluetoothSocket failed to connect.", connectException);
                mSocket.close();
            } catch (IOException closeException) {
                Log.e(TAG, "BluetoothSocket failed to be closed.", closeException);
            }
        }
    }

    private void setSocketStreams(){
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the input and output streams; using temp objects because
        // member streams are final.
        try {
            tmpIn = mSocket.getInputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating input stream", e);
        }
        try {
            tmpOut = mSocket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "Error occurred when creating output stream", e);
        }

        mInStream = tmpIn;
        mOutStream = tmpOut;
    }





}
