package com.iago.cosu.demo;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserManager;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/**
 * Created by ankurkumar on 6/15/18.
 */

public class KioskActivity extends Activity {

    private static final String TAG = "KioskActivity";

    private static final String PASS__WORD = "0000";

    private static final int TAP_COUNT_TO_EXIT = 5;

    private ComponentName adminComponentName;

    private DevicePolicyManager devicePolicyManager;

    private ActivityManager activityManager;

    private int tapCount = 0;

    private Handler resetHandler = new Handler();

    private Runnable resetRunnable = new Runnable() {
        @Override
        public void run() {
            tapCount = 1;
        }
    };

    private boolean stopLockTaskRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kiosk);

        devicePolicyManager = (DevicePolicyManager)
                getSystemService(Context.DEVICE_POLICY_SERVICE);

        activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        // Setup stop lock task button
        Button stopLockButton = findViewById(R.id.stop_pinning_btn);
        stopLockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endLockMode();
            }
        });

        final LinearLayout tapLayout = findViewById(R.id.tap_layout);
        tapLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tapCount++;
                // if required tap Count has reached
                if (tapCount >= TAP_COUNT_TO_EXIT) {

                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(KioskActivity.this);
                    View promptsView = li.inflate(R.layout.dialog_pass, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                            KioskActivity.this);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = promptsView.findViewById(R.id.password_input_edit_text);

                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // get user input and set it to result
                                            // edit text
                                            if (PASS__WORD.equals(userInput.getText().toString())) {
                                                endLockMode();
                                                Toast.makeText(KioskActivity.this, R.string.ending_lock_mode,
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            } else {
                                                userInput.getText().clear();
                                                Toast.makeText(KioskActivity.this, R.string.wrong_password,
                                                        Toast.LENGTH_SHORT)
                                                        .show();
                                            }
                                        }
                                    })
                            .setNegativeButton(android.R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                            hideSystemUI();
                                            tapCount = 0;
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();

                    resetHandler.removeCallbacks(resetRunnable);

                } else {
                    resetHandler.removeCallbacks(resetRunnable);
                    resetHandler.postDelayed(resetRunnable, 2000L);
                }
            }
        });

        // Set Default COSU policy
        adminComponentName = DeviceAdminReceiver.getComponentName(this);
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (!devicePolicyManager.isAdminActive(adminComponentName)) {
            Toast.makeText(this, R.string.not_lock_whitelisted, Toast.LENGTH_SHORT).show();
            finish();

        } else {
            if (devicePolicyManager.isDeviceOwnerApp(getPackageName())) {
                setDefaultCosuPolicies(true);

            } else {
                Toast.makeText(getApplicationContext(),
                        R.string.not_lock_whitelisted, Toast.LENGTH_LONG)
                        .show();
            }
        }

    }

    private void endLockMode() {
        ActivityManager am = (ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE);

        if (am != null && am.getLockTaskModeState() == ActivityManager.LOCK_TASK_MODE_LOCKED) {
            stopLockTaskRequested = true;
            stopLockTask();
        }

        setDefaultCosuPolicies(false);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // start lock task mode if its not already active
        if (devicePolicyManager.isLockTaskPermitted(this.getPackageName())) {
            if (!isAppInLockTaskMode() && !stopLockTaskRequested) {
                startLockTask();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {

    }

    private void setDefaultCosuPolicies(boolean active) {
        // set user restrictions
        // disallow safe boot
        setUserRestriction(UserManager.DISALLOW_SAFE_BOOT, active);

        // disallow factory reset
        setUserRestriction(UserManager.DISALLOW_FACTORY_RESET, active);

        // disallow volume up/down
        setUserRestriction(UserManager.DISALLOW_ADJUST_VOLUME, active);

        // disable keyguard and status bar
        devicePolicyManager.setKeyguardDisabled(adminComponentName, active);
        devicePolicyManager.setStatusBarDisabled(adminComponentName, active);

        // enable STAY_ON_WHILE_PLUGGED_IN
        enableStayOnWhilePluggedIn(active);

        // set system update policy
        if (active) {
            // allow install between 1AM and 2AM every night
            // policy is only set for 30days, end of which it should be reset
            devicePolicyManager.setSystemUpdatePolicy(adminComponentName,
                    SystemUpdatePolicy.createWindowedInstallPolicy(60, 120));
        } else {
            devicePolicyManager.setSystemUpdatePolicy(adminComponentName,
                    null);
        }

        // set this Activity as a lock task package
        devicePolicyManager.setLockTaskPackages(adminComponentName,
                active ? new String[]{getPackageName()} : new String[]{});
    }

    private void setUserRestriction(String restriction, boolean disallow) {
        if (disallow) {
            devicePolicyManager.addUserRestriction(adminComponentName,
                    restriction);
        } else {
            devicePolicyManager.clearUserRestriction(adminComponentName,
                    restriction);
        }
    }

    private void hideSystemUI() {
        // hide everything (fully immersive full screen mode)
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void enableStayOnWhilePluggedIn(boolean enabled) {
        if (enabled) {
            devicePolicyManager.setGlobalSetting(
                    adminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    Integer.toString(BatteryManager.BATTERY_PLUGGED_AC
                            | BatteryManager.BATTERY_PLUGGED_USB
                            | BatteryManager.BATTERY_PLUGGED_WIRELESS));

            getWindow().addFlags(FLAG_KEEP_SCREEN_ON);

        } else {
            devicePolicyManager.setGlobalSetting(
                    adminComponentName,
                    Settings.Global.STAY_ON_WHILE_PLUGGED_IN,
                    "0"
            );

            getWindow().clearFlags(FLAG_KEEP_SCREEN_ON);
        }
    }

    public boolean isAppInLockTaskMode() {
        int lockTaskMode = activityManager.getLockTaskModeState();
        return lockTaskMode == ActivityManager.LOCK_TASK_MODE_LOCKED;

    }
}
