package org.talos.utils;

import android.app.ActivityManager;
import android.content.Context;

import org.talos.services.TalosService;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Created by michael on 4/12/2016.
 */

public class ServiceUtils {

    public static boolean isMyServiceRunning(Context c) {
        ActivityManager manager = (ActivityManager) c.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (TalosService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
