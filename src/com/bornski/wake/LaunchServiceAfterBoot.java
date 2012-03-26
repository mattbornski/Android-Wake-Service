package com.bornski.wake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LaunchServiceAfterBoot extends BroadcastReceiver {
	private static final String TAG = "WakeService";
	
	public void onReceive(Context context, Intent intent)  {
        Intent launchServiceIntent = new Intent(context, WakeService.class);
        context.startService(launchServiceIntent);
        Log.d(TAG, "AutoStart onReceive");
    }
}
