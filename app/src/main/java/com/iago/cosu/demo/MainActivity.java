package com.iago.cosu.demo;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * Created by ankurkumar on 6/15/18.
 */

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private Button checkDeviceOwnerBtn;

    private Button startPinningBtn;

    private Button removeDeviceOwnerBtn;

    private DevicePolicyManager devicePolicyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        checkDeviceOwnerBtn = findViewById(R.id.check_device_owner_btn);
        startPinningBtn = findViewById(R.id.start_pinning_btn);
        removeDeviceOwnerBtn = findViewById(R.id.remove_device_owner_btn);

        // ask the system to use this app as the device owner
        // make sure no google accounts are there in the device
        checkDeviceOwnerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (devicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                    Toast.makeText(MainActivity.this, R.string.already_device_owner, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.not_lock_whitelisted, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // start the kiosk activity
        startPinningBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (devicePolicyManager.isDeviceOwnerApp(
                        getApplicationContext().getPackageName())) {
                    Intent lockIntent = new Intent(getApplicationContext(),
                            KioskActivity.class);

                    // launch the kiosk activity and exit
                    startActivity(lockIntent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(),
                            R.string.not_lock_whitelisted, Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

        removeDeviceOwnerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                devicePolicyManager.clearDeviceOwnerApp(getPackageName());
                Toast.makeText(MainActivity.this, R.string.remove_owner_help, Toast.LENGTH_LONG).show();
            }
        });
    }
}
