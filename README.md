# COSUDemo

## Objective
Build a lock mode App to be used in Kiosk mode.

## Code Components (Main)

DeviceAdminReceiver.java - BroadcastReceiver extending android.app.admin.DeviceAdminReceiver (https://developer.android.com/reference/android/app/admin/DeviceAdminReceiver)
KioskActivity.java - Activity which will serve as the Kiosk mode screen.
onCreate - checks if the app is set as Device owner, exits otherwise. If device owner sets the policies.
onStart - if the app is device owner, the startLockTask is called to start app pinning (Should be called in onStart only to avoid bugs)
onResume - sets the UI properties to make it Immersive Fullscreen

layout - has a linear layout in the right hand top. Keeps track of tap counts. If no tap for 2 seconds the tap count is reset. If tap count reaches 5 or more, the password dialog is launched.
Currently the password is hardcoded inside the activity (check PASS__WORD), should be changed to a suitable alternative.

password dialog - on correct password call method endLockMode, on cancel reset the UI properties to avoid the back button bar at the bottom

endLockMode - called to end screen pinning and reset the user policies (SystemBar hiding, etc)

PackageReplacedReceiver.java - Broadcast receiver used to restart the KioskActivity on Boot completed and on package replaced. (Check Manifest for more information)

## Code Components (Additional)
MainActivity.java - Utility activity which provides additional methods, hidden for simplicity

## Before installing the app
Factory reset the device, do not create/add any account to the device

## After installing the app
Run the command in shell/cmd (don't forget to replace the package name later)
adb shell dpm set-device-owner "com.iago.cosu.demo/.DeviceAdminReceiver"

Launch the KioskActivity to test

### To exit the kiosk mode tap the right-hand corner 5 times and enter 0000 (default password)

## To uninstall
1. Launch the hidden MainActivity and click on "Remove as device owner".
2. Goto Settings->Security->Phone administrators and deactivate the COSU app
3. Uninstall the app
