package org.talos.services;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.talos.MainActivity;
import org.talos.R;
import org.talos.beans.DataBean;
import org.talos.db.DataDbOperations;
import org.talos.enums.SettingEnum;
import org.talos.utils.SettingsUtils;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


public class TalosService extends Service implements LocationListener {
    private static final String TAG = "TalosService";
    // broadcast strings
    public static final String LOCATION_MESSAGE = "org.talos.services.LocationService.LOCATION_CHANGED";
    public static final String LOCATION_RESULT = "org.talos.services.LocationService.REQUEST_PROCESSED";
    LocalBroadcastManager locationBroadcaster;
    // indicates how to behave if the service is killed
    int mStartMode;
    // indicates whether onRebind should be used
    boolean mAllowRebind;
    // interface for clients that bind
    private final IBinder mBinder = new TalosBinder();

    private String activeUser;
    private String operatorName;
    private int signalStrength;
    private String networkType;
    private float latitude;

    public String getActiveUser() {
        return activeUser;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public int getSignalStrength() {
        return signalStrength;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getServerIp() {
        return serverIp;
    }

    public String getNetworkType(){
        return networkType;
    }

    private float longitude;
    private String serverIp;

    // location listener
    LocationManager locationManager;
    String provider;

    // signal listeners
    TelephonyManager Tel;
    MyPhoneStateListener signalStrengthListener;

    SettingsUtils sUtils = new SettingsUtils();

    public class TalosBinder extends Binder {
        public TalosService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TalosService.this;
        }
    }

    @Override
    public void onCreate() {
        loadSettings();
        locationBroadcaster = LocalBroadcastManager.getInstance(this);
        checkGPSStatus();
        signalStrengthListener = new MyPhoneStateListener();
        Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Tel.listen(signalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        operatorName = Tel.getNetworkOperatorName();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = LocationManager.GPS_PROVIDER;
        networkType = findNetworkType();
        Location location = null;
        try {
            location = locationManager.getLastKnownLocation(provider);
        }catch (SecurityException e){
            Log.d(TAG, e.toString());
            // TODO: 27/9/2016 handle the fucking exception
        }
        if (location != null) {
            System.out.println("Provider " + provider + " has been selected.");
            onLocationChanged(location);
        } else {
            latitude = -1;
            longitude = -1;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            String stringValue = sUtils.getSetting(this, SettingEnum.ACCURACY);
            int accuracy = Integer.parseInt(stringValue);
            locationManager.requestLocationUpdates(provider, accuracy, 1, this);
        }catch (SecurityException e){
            Log.d(TAG, e.toString());
            // TODO: 27/9/2016 handle the fucking exception
        }
        notificationBuild(true);
        return mStartMode;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    @Override
    public void onRebind(Intent intent) {
        //empty
    }

    @Override
    public void onDestroy() {
        try{
            locationManager.removeUpdates(this);
        }catch (SecurityException e){
            Log.d(TAG, e.toString());
            // TODO: 27/9/2016 handel the fucking exception
        }
        notificationDestroy();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = (float) location.getLatitude();
        longitude = (float) location.getLongitude();
        DataDbOperations dbOp = new DataDbOperations(this);
        dbOp.initWrite();
        DataBean bean = new DataBean();
        bean.setCinr(Integer.toString(signalStrength));
        bean.setLatitude(Float.toString(latitude));
        bean.setLongitude(Float.toString(longitude));
        bean.setNetworkType(networkType);
        bean.setOperator(operatorName);
        bean.setTimeStamp(getCurrentTimeStamp());
        bean.setUser(activeUser);
        dbOp.storeData(bean);
        dbOp.close();
        broadcast();
        Log.d(TAG, "Data stored");
    }

    @Override
    public void onProviderDisabled(String arg0) {
        checkGPSStatus();

    }

    @Override
    public void onProviderEnabled(String arg0) {
        checkGPSStatus();

    }

    @Override
    public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        checkGPSStatus();

    }

    public void broadcast() {
        Intent intent = new Intent(LOCATION_RESULT);
        locationBroadcaster.sendBroadcastSync(intent);
    }

    private void checkGPSStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            Toast.makeText(getApplicationContext(), "GPS provides must be enabled to start service", Toast.LENGTH_SHORT).show();
            stopSelf();
        }
    }

    private void notificationBuild(Boolean ongoing) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.talos);
        mBuilder.setContentTitle(getResources().getString(R.string.app_name));
        mBuilder.setContentText(getResources().getString(R.string.notification_message));
        mBuilder.setOngoing(ongoing);

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationID = 0;
        mNotificationManager.notify(notificationID, mBuilder.build());

        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void notificationDestroy() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);
    }

    private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrengths) {
            super.onSignalStrengthsChanged(signalStrengths);
            signalStrength = signalStrengths.getGsmSignalStrength();
            networkType = findNetworkType();
            broadcast();
        }
    }

    private String findNetworkType() {
        int networkType;
        String result = null;
        networkType = Tel.getNetworkType();
        switch (networkType) {
            case 7:
                result = "1xRTT";
                break;
            case 4:
                result = "CDMA";
                break;
            case 2:
                result = "EDGE";
                break;
            case 14:
                result = "eHRPD";
                break;
            case 5:
                result = "EVDO rev. 0";
                break;
            case 6:
                result = "EVDO rev. A";
                break;
            case 12:
                result = "EVDO rev. B";
                break;
            case 1:
                result = "GPRS";
                break;
            case 8:
                result = "HSDPA";
                break;
            case 10:
                result = "HSPA";
                break;
            case 15:
                result = "HSPA+";
                break;
            case 9:
                result = "HSUPA";
                break;
            case 11:
                result = "iDen";
                break;
            case 13:
                result = "LTE";
                break;
            case 3:
                result = "UMTS";
                break;
            case 0:
                result = "Unknown";
                break;
        }

        return result;
    }

    public String getCurrentTimeStamp() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date());
            return currentTimeStamp;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
            return null;
        }
    }

    public boolean loadSettings() {
        activeUser = sUtils.getSetting(this, SettingEnum.ACTIVE_USER);
        serverIp = sUtils.getSetting(this, SettingEnum.SERVER_IP);

        return true;
    }

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
