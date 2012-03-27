package com.bornski.wake;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

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
	
	public static class CheckUpcomingAlarm extends BroadcastReceiver {
		private long nextAlarmTimestamp(Context context) {
			/*boolean use24HourMode = android.text.format.DateFormat.is24HourFormat(context);
		    String DM12 = "E h:mm aa";
		    String DM24 = "E k:mm";*/
		    String timeString = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
	    	Log.d(TAG, "CheckUpcomingAlarm determined next alarm (formatted) at " + timeString);
	    	if (timeString.equals("")) {
	    		return 0;
	    	}
	    	try {
	    		// Split string at first whitespace character.
	    		String[] segments = timeString.split("\\s+", 1);
	    		for (int i = 0; i < segments.length; i++) {
	    			Log.d(TAG, segments[i]);
	    		}
	    		Calendar calendar = Calendar.getInstance();
	    		calendar.setTime(DateFormat.getTimeInstance().parse(segments[1]));
	    		while (!String.format("%ta", calendar).equals(segments[0])) {
	    			calendar.add(Calendar.DATE, 1);
	    			Log.d(TAG, "CheckUpcomingAlarm comparing '" + segments[0] + "' to '" + String.format("%ta", calendar) + "'");
	    		}
	    		
				Log.d(TAG, "CheckUpcomingAlarm determined next alarm (deformatted) at " + calendar.getTimeInMillis());
				return calendar.getTimeInMillis();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return 0;
		}
		
		public void onReceive(Context context, Intent intent)  {
	        Intent launchServiceIntent = new Intent(context, WakeService.class);
	        context.startService(launchServiceIntent);
	        Log.d(TAG, "CheckUpcomingAlarm onReceive");
	        
	        long timestamp = nextAlarmTimestamp(context);
	        Log.d(TAG, "CheckUpcomingAlarm determined next alarm (deformatted) at " + timestamp);
	        
	    	// TODO schedule myself to wake up ahead of it
	        if (timestamp > 0) {
	        	Log.d(TAG, "CheckUpcomingAlarm please schedule wakeup earlier than " + timestamp);
	        }
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
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, sender);
        // TODO make INTERVAL_HOUR when we're done testing
    }
	
	// 
	public void determineNextAlarm(Context context) {
		
	}
}
