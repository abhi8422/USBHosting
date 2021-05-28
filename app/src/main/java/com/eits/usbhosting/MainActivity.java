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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    UsbManager usbManager;
    PendingIntent mPermissionIntent;
    UsbDevice device;
    private byte[] bytes;
    private static int TIMEOUT = 0;
    private boolean forceClaim = true;
    Button btnCheck;
    TextView textInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnCheck = (Button) findViewById(R.id.check);
        textInfo = (TextView) findViewById(R.id.info);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                textInfo.setText("");
                checkInfo();
            }
        });
        UsbDevice device = getIntent().getParcelableExtra(UsbManager.EXTRA_DEVICE);
        Toast.makeText(this, device.getDeviceId(), Toast.LENGTH_SHORT).show();
    }

    private void checkInfo() {
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);
        HashMap<String , UsbDevice> deviceList = usbManager.getDeviceList();
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
                Toast.makeText(context, "ACTION_USB_PERMISSION", Toast.LENGTH_SHORT).show();
                synchronized (this) {
                    UsbDevice device =intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            UsbInterface intf = device.getInterface(0);
                            UsbEndpoint endpoint = intf.getEndpoint(0);
                            UsbDeviceConnection connection = usbManager.openDevice(device);
                            if(connection.claimInterface(intf, forceClaim)){
                                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(context, "Not Connected", Toast.LENGTH_SHORT).show();
                            }
                           
                        }
                    }
                    else {
                        Log.d("MainActivity", "permission denied for device " + device);
                    }
                }
            }
        }
    };
}