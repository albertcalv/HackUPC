package com.example.adam.barcelona;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;


import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.DeviceProfile;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;
import com.kontakt.sdk.android.manager.KontaktProximityManager;

import java.util.List;

//    private static final String API_KEY = "sEdPqkvAZwxVZWdbCZpnyfhtQeoayiiz";


public class MainActivity extends AppCompatActivity implements ProximityManager.ProximityListener {

    private static final String API_KEY = "sEdPqkvAZwxVZWdbCZpnyfhtQeoayiiz";

    private static final String TAG = MainActivity.class.getSimpleName();

    private ProximityManagerContract proximityManager;
    private ScanContext scanContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KontaktSDK.initialize(API_KEY);
        proximityManager = new KontaktProximityManager(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        proximityManager.initializeScan(getScanContext(), new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.attachListener(MainActivity.this);
            }

            @Override
            public void onConnectionFailure() {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        proximityManager.detachListener(this);
        proximityManager.disconnect();
    }

    private ScanContext getScanContext() {
        if (scanContext == null) {
            scanContext = new ScanContext.Builder()
                    .setScanPeriod(ScanPeriod.RANGING) // or for monitoring for 15 seconds scan and 10 seconds waiting:
                            //.setScanPeriod(new ScanPeriod(TimeUnit.SECONDS.toMillis(15), TimeUnit.SECONDS.toMillis(10)))
//                    .setScanMode(ProximityManager.SCAN_MODE_BALANCED)
                    .setScanMode(ProximityManager.SCAN_MODE_LOW_POWER)
                    .setActivityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                    .setForceScanConfiguration(ForceScanConfiguration.MINIMAL)
                    .setIBeaconScanContext(new IBeaconScanContext.Builder().build())
                    .setEddystoneScanContext(new EddystoneScanContext.Builder().build())
                    .setForceScanConfiguration(ForceScanConfiguration.MINIMAL)
                    .build();
        }
        return scanContext;
    }

    @Override
    public void onEvent(BluetoothDeviceEvent bluetoothDeviceEvent) {
        List<? extends RemoteBluetoothDevice> deviceList = bluetoothDeviceEvent.getDeviceList();
        long timestamp = bluetoothDeviceEvent.getTimestamp();
        DeviceProfile deviceProfile = bluetoothDeviceEvent.getDeviceProfile();
        if (deviceList.size() > 0) {
            RemoteBluetoothDevice rbd = deviceList.get(0);
            rbd.getAddress();
            for (int i=0; i < deviceList.size(); i++) {
                Log.d(TAG, "\n");
                Log.d(TAG, "----------------------------------------------------------------");
                Log.d(TAG, "device address: " + deviceList.get(i).getAddress());
                Log.d(TAG, "device name: " + deviceList.get(i).getName());
                Log.d(TAG, "device unique Id: " + deviceList.get(i).getUniqueId());
                Log.d(TAG, "----------------------------------------------------------------");
            }

        }

        switch (bluetoothDeviceEvent.getEventType()) {
            case SPACE_ENTERED:
                Log.d(TAG, "namespace or region entered");
                break;
            case DEVICE_DISCOVERED:
                Log.d(TAG, "found new beacon");
                break;
            case DEVICES_UPDATE:
                Log.d(TAG, "updated beacons");
                break;
            case DEVICE_LOST:
                Log.d(TAG, "lost device");
                break;
            case SPACE_ABANDONED:
                Log.d(TAG, "namespace or region abandoned");
                break;
        }
    }

    @Override
    public void onScanStart() {
        Log.d(TAG, "scan started");
    }

    @Override
    public void onScanStop() {
        Log.d(TAG, "scan stopped");
    }

}
