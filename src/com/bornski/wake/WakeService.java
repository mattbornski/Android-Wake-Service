package com.bornski.wake;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

public class WakeService extends Service {
	private static final String TAG = "WakeService";
	private static final long wakePreludeMillis = 300000;
	
	public static class LaunchServiceAfterBoot extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent)  {
	        Intent launchServiceIntent = new Intent(context, WakeService.class);
	        context.startService(launchServiceIntent);
	    }
	}
	
	public static class WifiStateChanged extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent)  {
			Log.d(TAG, "WifiStateChanged onReceive");
			/*// Are there any alarms which are pending Wifi?
			switch (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
				case WifiManager.WIFI_STATE_UNKNOWN:
					Log.d(TAG, "WifiStateChanged -> unknown");
					break;
				case WifiManager.WIFI_STATE_ENABLING:
					Log.d(TAG, "WifiStateChanged -> enabling");
					break;
				case WifiManager.WIFI_STATE_ENABLED:
					Log.d(TAG, "WifiStateChanged -> enabled");
					break;
				case WifiManager.WIFI_STATE_DISABLING:
					Log.d(TAG, "WifiStateChanged -> disabling");
					break;
				case WifiManager.WIFI_STATE_DISABLED:
					Log.d(TAG, "WifiStateChanged -> disabled");
					break;
				default:
					Log.d(TAG, "WifiStateChanged -> " + intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
					break;
			}
			switch (intent.getIntExtra(WifiManager.EXTRA_NETWORK_INFO, WifiManager.WIFI_STATE_UNKNOWN)) {
				case WifiManager.WIFI_STATE_UNKNOWN:
					Log.d(TAG, "WifiStateChanged -> unknown");
					break;
				case WifiManager.WIFI_STATE_ENABLING:
					Log.d(TAG, "WifiStateChanged -> enabling");
					break;
				case WifiManager.WIFI_STATE_ENABLED:
					Log.d(TAG, "WifiStateChanged -> enabled");
					break;
				case WifiManager.WIFI_STATE_DISABLING:
					Log.d(TAG, "WifiStateChanged -> disabling");
					break;
				case WifiManager.WIFI_STATE_DISABLED:
					Log.d(TAG, "WifiStateChanged -> disabled");
					break;
				default:
					Log.d(TAG, "WifiStateChanged -> " + intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
					break;
			}
			SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_name), MODE_PRIVATE);
			if (prefs.getBoolean("waitingOnWifi", false)) {
				if (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN) == WifiManager.WIFI_STATE_ENABLED) {
					SharedPreferences.Editor ed = prefs.edit();
					ed.putBoolean("waitingOnWifi", false);
					ed.commit();
					
					Log.d(TAG, "WifiStateChanged reissue WakeAlarm");
					Intent wakeAlarmIntent = new Intent(context, WakeAlarm.class);
					AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
					PendingIntent sender = PendingIntent.getBroadcast(context, 234324243, wakeAlarmIntent, 0);
					alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), sender);
				}
			}*/
	    }
	}
	
	public static class WakeAlarm extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent)  {
	        Log.d(TAG, "WakeAlarm onReceive");
	        
	        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        	WifiLock wifilock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, context.getString(R.string.wifi_lock_name));
        	wifilock.setReferenceCounted(false);
	        SharedPreferences prefs = context.getSharedPreferences(context.getString(R.string.shared_preferences_name), MODE_PRIVATE);
	        if (prefs.getBoolean("wifi", false)) {
	            wifilock.acquire();
	        	if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
	        		wifiManager.setWifiEnabled(true);
	        		SharedPreferences.Editor ed = prefs.edit();
	        		ed.putBoolean("waitingOnWifi", true);
	        		ed.commit();
	        		return;
	        	}
	        }
	        String endpoint = prefs.getString("endpoint", "");
	        String method = prefs.getString("method", "");
			HttpClient httpclient = new DefaultHttpClient();
			HttpRequestBase request = null;
			if (method.equals("POST")) {
				request = new HttpPost(endpoint);
			} else if (method.equals("GET")) {
				request = new HttpGet(endpoint);
			}
			if (request == null) {
				Log.w(TAG, "WakeAlarm invalid HTTP method '" + method + "' configured");
				return;
			}
			if (endpoint.equals("")) {
				Log.w(TAG, "WakeAlarm invalid HTTP endpoint '" + endpoint + "' configured");
				return;
			}
	        try {
				Log.d(TAG, "WakeAlarm execute");
				@SuppressWarnings("unused")
				HttpResponse response = httpclient.execute(request);
				// Mark that we have executed this alarm callback.
				Log.d(TAG, "WakeAlarm execute OK");
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            wifilock.release();
            
            // Repeat if necessary
            int repeatCountRemaining = prefs.getInt("repeatCountRemaining", 5);
            if (repeatCountRemaining > 0) {
            	SharedPreferences.Editor ed = prefs.edit();
        		ed.putInt("repeatCountRemaining", repeatCountRemaining - 1);
        		ed.commit();
            	Intent wakeAlarmIntent = new Intent(context, WakeAlarm.class);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
                PendingIntent sender = PendingIntent.getBroadcast(context, 234324243, wakeAlarmIntent, 0);
                alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, sender);
            } else {
            	// Reset for next alarm
            	SharedPreferences.Editor ed = prefs.edit();
        		ed.putInt("repeatCountRemaining", 5);
        		ed.commit();
            }
	    }
	}
	
	public static class CheckUpcomingUserAlarm extends BroadcastReceiver {
		private long nextAlarmTimestamp(Context context) {
		    String timeString = Settings.System.getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
	    	Log.d(TAG, "CheckUpcomingUserAlarm determined next alarm (formatted) at " + timeString);
	    	if (timeString.equals("")) {
	    		// Clear any existing alarms.
	    		Intent wakeAlarmIntent = new Intent(context, WakeAlarm.class);
	            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
	    		PendingIntent sender = PendingIntent.getBroadcast(context, 234324243, wakeAlarmIntent, 0);
	            alarmManager.cancel(sender);
	    		return 0;
	    	}
	    	try {
	    		// Split string at first whitespace character into two substrings.
	    		String[] segments = timeString.split("\\s+", 2);
	    		// Parse out the hour + minute of the upcoming alarm from the formatted text string.
	    		Calendar hourAndMinuteCalendar = Calendar.getInstance();
	    		// These date formats are derived from the stock Android alarm clock.  YMMV.
	    		SimpleDateFormat format = (android.text.format.DateFormat.is24HourFormat(context)
	    				? new SimpleDateFormat("h:mm")
	    				: new SimpleDateFormat("h:mm aa"));
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
	    		finalDateCalendar.set(Calendar.SECOND, 0);
	    		finalDateCalendar.set(Calendar.MILLISECOND, 0);
	    		
				Log.d(TAG, "CheckUpcomingUserAlarm determined next alarm (deformatted) at " + (finalDateCalendar.getTimeInMillis() / 1000) + " (" + ((finalDateCalendar.getTimeInMillis() - System.currentTimeMillis()) / 1000) + " seconds from now)");
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
	            alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp - wakePreludeMillis, sender);
	        }
	    }
	}
	
	@Override
    public IBinder onBind(Intent intent) {
        return null;
    }
	public void onDestroy() {}
	
	@Override
    public void onStart(Intent intent, int startid) {
        Log.d(TAG, "onStart");
        // Schedule a repeating task to check the next alarm.
        Intent CheckUpcomingUserAlarmIntent = new Intent(this, CheckUpcomingUserAlarm.class);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(this.getApplicationContext(), 234324243, CheckUpcomingUserAlarmIntent, 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis(), AlarmManager.INTERVAL_HOUR, sender);
        // Until debugging complete, check every 10 seconds.
        //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10000, sender);
    }
}
