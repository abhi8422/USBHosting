package com.eits.usbhosting;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mjdev.libaums.UsbMassStorageDevice;
import com.github.mjdev.libaums.fs.FileSystem;
import com.github.mjdev.libaums.fs.UsbFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private static final String TAG = "MainActivity";
    UsbManager usbManager;
    PendingIntent mPermissionIntent;
    UsbDevice device;
    private byte[] bytes;
    private static int TIMEOUT = 0;
    private boolean forceClaim = true;
    Button btnCheck,btnCheck1;
    TextView textInfo;
    ListView listView;
    ArrayList list=new ArrayList();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCheck = (Button) findViewById(R.id.check);
        btnCheck1 =  findViewById(R.id.check1);
        textInfo = (TextView) findViewById(R.id.info);
        listView=findViewById(R.id.list_view);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                textInfo.setText("");
                checkInfo();
            }
        });
        btnCheck1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                ArrayAdapter<String> adapter=new ArrayAdapter(MainActivity.this,
                        android.R.layout.simple_list_item_1,
                        list);
                listView.setAdapter(adapter);
            }
        });
    }

    private void checkInfo() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        String i = "";
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();
            usbManager.requestPermission(device, mPermissionIntent);
            i += "\n" + "DeviceID: " + device.getDeviceId() + "\n"
                    + "DeviceName: " + device.getDeviceName() + "\n"
                    + "DeviceClass: " + device.getDeviceClass() + " - "
                    + "DeviceSubClass: " + device.getDeviceSubclass() + "\n"
                    + "VendorID: " + device.getVendorId() + "\n"
                    + "ProductID: " + device.getProductId() + "\n";
        }

        textInfo.setText(i);
    }

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {

                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            UsbInterface intf = device.getInterface(0);
                            UsbDeviceConnection connection = usbManager.openDevice(device);
                            if (connection.claimInterface(intf, forceClaim)) {
                                UsbMassStorageDevice[] devices = UsbMassStorageDevice.getMassStorageDevices(MainActivity.this);

                                for (UsbMassStorageDevice device1 : devices) {

                                    // before interacting with a device you need to call init()!
                                    try {
                                        device1.init();
                                        // Only uses the first partition on the device
                                        FileSystem currentFs = device1.getPartitions().get(0).getFileSystem();
                                        UsbFile root = currentFs.getRootDirectory();

                                        UsbFile[] files = root.listFiles();


                                        for (int i=0;i<files.length;i++) {
                                            Log.d(TAG,"File name: "+files[i].getName());
                                            list.add(files[i].getName());
                                        }


                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }

                            }
                        } else {
                            Log.d("MainActivity", "permission denied for device " + device);
                        }
                    }
                }
            }
        }

    };

}
