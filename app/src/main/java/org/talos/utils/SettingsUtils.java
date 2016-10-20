package org.talos.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.talos.enums.SettingEnum;

/**
 * Created by michael on 3/10/2016.
 */

public class SettingsUtils {

    public void updateSetting(Context context, SettingEnum setting, String value){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(setting.getKey(), value);
        editor.commit();
    }

    public String getSetting(Context context, SettingEnum setting){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString(setting.getKey(), null);
    }

    public boolean isUserLoggedIn(Context context){
        return getSetting(context, SettingEnum.ACTIVE_USER)!=null;
    }
}
