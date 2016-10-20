package org.talos.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import java.util.Set;

/**
 * Created by michael on 29/9/2016.
 */

public class PermissionUtils {
    public static void check(Activity activity, String permission, int requestCode){
        if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,new String[]{permission},requestCode);
        }
    }

    public static void checkMultiple(Activity activity, String permissions[], int requestCode){
        for (String s : permissions){
            check(activity, s, requestCode);
        }
    }

}
