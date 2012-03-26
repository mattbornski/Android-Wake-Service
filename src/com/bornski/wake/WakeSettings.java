package com.bornski.wake;

import com.bornski.wake.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class WakeSettings extends PreferenceActivity {
    
    // The name of the SharedPreferences file we'll store preferences in.
    public static final String SHARED_PREFERENCES_NAME = "WakeService";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_NAME);
        addPreferencesFromResource(R.xml.preferences);
    }
}
