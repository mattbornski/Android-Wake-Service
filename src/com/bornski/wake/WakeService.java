package com.bornski.wake;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class WakeService extends Service {
	private static final String TAG = "WakeService";
	
	public class CheckUpcomingAlarm extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent)  {
	        Intent launchServiceIntent = new Intent(context, WakeService.class);
	        context.startService(launchServiceIntent);
	        Log.d(TAG, "AutoStart onReceive");
	    }
	}
	
	@Override
    public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
        return null;
    }
	public void onDestroy() {
        Log.d(TAG, "onDestroy");
    }
	
	@Override
    public void onStart(Intent intent, int startid) {
        /*Intent intents = new Intent(getApplicationContext(), hello.class);
        intents.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intents);*/
        Log.d(TAG, "onStart");
        // Schedule a repeating task to check the next alarm.
        Intent checkUpcomingAlarmIntent = new Intent(this, CheckUpcomingAlarm.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(this.getApplicationContext(), 234324243, checkUpcomingAlarmIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0, AlarmManager.INTERVAL_HOUR, sender);
        findNextAlarm(getBaseContext());
    }
	
	private void findNextAlarm(Context context) {
		String timeString = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
		Log.d(TAG, "findNextAlarm " + timeString);
		// TODO schedule myself to wake up ahead of it
	}
}
