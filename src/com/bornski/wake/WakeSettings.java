package com.bornski.wake;

import com.bornski.wake.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class WakeSettings extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(this.getString(R.string.shared_preferences_name));
        addPreferencesFromResource(R.xml.preferences);
        
        // Ensure service is running
        Intent launchServiceIntent = new Intent(this, WakeService.class);
        this.startService(launchServiceIntent);
    }
}
