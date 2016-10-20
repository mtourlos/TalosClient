package org.talos;

import org.talos.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class SettingActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

    }
}
