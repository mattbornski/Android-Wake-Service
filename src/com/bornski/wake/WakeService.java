package com.bornski.wake;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

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
	private static final long wakePreludeMillis = 300000;
	
	public static class WakeAlarm extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent)  {
	        Log.d(TAG, "WakeAlarm onReceive");
	        
	        String url = "http://192.168.10.77/N/2/on";
	        
			HttpClient httpclient = new DefaultHttpClient();
	        HttpPost httppost = new HttpPost(url);
	        try {
				Log.d(TAG, "WakeAlarm execute");
				@SuppressWarnings("unused")
				HttpResponse response = httpclient.execute(httppost);
				// Mark that we have executed this alarm callback.
				Log.d(TAG, "WakeAlarm execute OK");
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
	public static class CheckUpcomingUserAlarm extends BroadcastReceiver {
		private long nextAlarmTimestamp(Context context) {
		    String timeString = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
	    	Log.d(TAG, "CheckUpcomingUserAlarm determined next alarm (formatted) at " + timeString);
	    	if (timeString.equals("")) {
	    		return 0;
	    	}
	    	try {
	    		// Split string at first whitespace character into two substrings.
	    		String[] segments = timeString.split("\\s+", 2);
	    		// Parse out the hour + minute of the upcoming alarm from the formatted text string.
	    		Calendar hourAndMinuteCalendar = Calendar.getInstance();
	    		// These date formats are derived from the stock Android alarm clock.  YMMV.
	    		SimpleDateFormat format = (android.text.format.DateFormat.is24HourFormat(context)
	    				? new SimpleDateFormat("h:mm aa")
	    				: new SimpleDateFormat("h:mm"));
	    		hourAndMinuteCalendar.setTime(format.parse(segments[1]));
	    		
	    		// Compose a calendar which starts with today, since any alarms we are interested in
	    		// are in the future.
	    		Calendar finalDateCalendar = Calendar.getInstance();
	    		// Iterate forward in time until the day-of-week matches with the formatted day-of-week
	    		// we read from the system settings.
	    		while (!String.format("%ta", finalDateCalendar).equals(segments[0])) {
	    			finalDateCalendar.add(Calendar.DATE, 1);
	    		}
	    		// Set the hour and minute of the final calendar as the hour and minute read from the system settings.
	    		finalDateCalendar.set(Calendar.HOUR_OF_DAY, hourAndMinuteCalendar.get(Calendar.HOUR_OF_DAY));
	    		finalDateCalendar.set(Calendar.MINUTE, hourAndMinuteCalendar.get(Calendar.MINUTE));
	    		
				Log.d(TAG, "CheckUpcomingUserAlarm determined next alarm (deformatted) at " + (finalDateCalendar.getTimeInMillis() / 1000));
				return finalDateCalendar.getTimeInMillis();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	return 0;
		}
		
		public void onReceive(Context context, Intent intent)  {
	        Log.d(TAG, "CheckUpcomingUserAlarm onReceive");
	        long timestamp = nextAlarmTimestamp(context);
	    	// Schedule an alarm event which will trigger WakeAlarm just ahead of the upcoming user alarm.
	        // TODO check if callback has already been scheduled instead of just comparing timestamps.
	        if ((timestamp > 0) && ((timestamp - wakePreludeMillis) > System.currentTimeMillis())) {
	        	Log.d(TAG, "CheckUpcomingUserAlarm please schedule wakeup at " + ((timestamp - wakePreludeMillis) / 1000));
	        	Intent wakeAlarmIntent = new Intent(context, WakeAlarm.class);
	            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
	            PendingIntent sender = PendingIntent.getBroadcast(context, 234324243, wakeAlarmIntent, 0);
	            alarmManager.set(AlarmManager.RTC_WAKEUP,  timestamp - wakePreludeMillis, sender);
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
        Log.d(TAG, "onStart");
        // Schedule a repeating task to check the next alarm.
        Intent CheckUpcomingUserAlarmIntent = new Intent(this, CheckUpcomingUserAlarm.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(this.getApplicationContext(), 234324243, CheckUpcomingUserAlarmIntent, 0);
        //alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES, sender);
        // TODO make INTERVAL_HOUR when we're done testing
        // Until debugging complete, check every 10 seconds.
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10000, sender);
    }
}
