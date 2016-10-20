package org.talos;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.talos.beans.DataBean;
import org.talos.db.DataContract.DataEntry;
import org.talos.db.DataDbHelper;
import org.talos.db.DataDbOperations;
import org.talos.enums.SettingEnum;
import org.talos.operations.UploadDataOperation;
import org.talos.services.TalosService;
import org.talos.services.TalosService.TalosBinder;
import org.talos.utils.PermissionUtils;
import org.talos.utils.SettingsUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String KEY_PREF_ACTIVE_USER = "settings_active_user";
    public static final String KEY_PREF_SERVER_IP = "settings_server_ip";
    public static final int PERMISSION_LOCATION_REQUEST_CODE = 1;
    public static final int PERMISSION_NETWORK_REQUEST_CODE = 2;

    // network type
    private String networkType;

    // textViewers
    private TextView serverIpField;
    private TextView activeUserField;
    private TextView timestampField;
    private TextView latitudeField;
    private TextView longitudeField;
    private TextView signalStrengthField;
    private TextView operatorNameField;
    private TextView networkTypeField;

    // preferences
    String activeUser;
    String serverIp;



    String timestamp;

    // dbHelper
    DataDbHelper mDbHelper;

    TalosService mService;
    boolean mBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        loadSettings();
        mDbHelper = new DataDbHelper(getBaseContext());
        latitudeField = (TextView) findViewById(R.id.lat);
        longitudeField = (TextView) findViewById(R.id.lon);
        operatorNameField = (TextView) findViewById(R.id.operator_name);
        timestampField = (TextView) findViewById(R.id.timestamp);
        serverIpField = (TextView) findViewById(R.id.server_ip);
        activeUserField = (TextView) findViewById(R.id.active_user);
        networkTypeField = (TextView) findViewById(R.id.network_type);
        signalStrengthField = (TextView) findViewById(R.id.signal_out);
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionUtils.checkMultiple(this, new String[] {Manifest.permission.CHANGE_WIFI_STATE, Manifest.permission.CHANGE_NETWORK_STATE}, PERMISSION_NETWORK_REQUEST_CODE );
        LocalBroadcastManager.getInstance(this).registerReceiver((locationBrReceiver), new IntentFilter(TalosService.LOCATION_RESULT));
        SettingsUtils sUtils = new SettingsUtils();
        if(!sUtils.isUserLoggedIn(this)) {
            Intent signIn = new Intent(this, SignInActivity.class);
            startActivity(signIn);
        }
        Intent serviceIntent = new Intent(this, TalosService.class);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                Intent i = new Intent(this, SettingActivity.class);
                startActivity(i);
                return true;
            case R.id.sign_out_prop:
                Intent signInAc = new Intent(this, SignInActivity.class);
                startActivity(signInAc);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGPSStatus();

        // timestamp
        timestamp = getCurrentTimeStamp();
        timestampField.setText("Timestamp: " + timestamp);

        // load settings
        if (loadSettings()) {
            // username and active user textview
            activeUserField.setText("Active User: " + activeUser);
            serverIpField.setText("Server Ip: " + serverIp);
        }

    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationBrReceiver);
        if(mBound){
            unbindService(serviceConnection);
            mBound = false;
        }
        super.onStop();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static String getCurrentTimeStamp() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentTimeStamp = dateFormat.format(new Date()); // Find
            return currentTimeStamp;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Loads users settings from settings.xml
     *
     * @return true in order to triggers changes and display them
     */
    public boolean loadSettings() {
        SettingsUtils sUtils = new SettingsUtils();
        serverIp = sUtils.getSetting(this, SettingEnum.SERVER_IP);
        activeUser = sUtils.getSetting(this, SettingEnum.ACTIVE_USER);
        return true;
    }

    /**
     * Sends single Data to Server
     *
     * @param v
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void sData(View v) throws InterruptedException, ExecutionException, JSONException, IllegalAccessException {
        UploadDataOperation upOp = new UploadDataOperation(getApplicationContext());
        upOp.uploadData();
    }

    /**
     * Starts the StoreLocationService
     *
     * @param v
     */
    public void startService(View v) {
        PermissionUtils.checkMultiple(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE );
        Intent serviceIntent = new Intent(this, TalosService.class);
        startService(serviceIntent);
    }

    /**
     * Stops the StoreLocationService
     *
     * @param v
     */
    public void stopService(View v) {
        Intent serviceIntent = new Intent(this, TalosService.class);
        stopService(serviceIntent);
    }

    /**
     * Checks whether GPS provides is enabled and if is not prompts an
     * AlertDialog
     */
    private void checkGPSStatus() {
        LocationManager lmService = (LocationManager) getSystemService(LOCATION_SERVICE);
        boolean enabled = lmService.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.main_alerDialog_message).setTitle(R.string.main_alerDialog_title);
            builder.setPositiveButton(R.string.enable,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked OK button
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.cancel,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    System.exit(0);
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void storePilotData(View v) {
        if (mBound){
            DataBean bean = new DataBean();
            bean.setUser(mService.getActiveUser());
            bean.setTimeStamp(mService.getCurrentTimeStamp());
            bean.setOperator(mService.getOperatorName());
            bean.setNetworkType(mService.getNetworkType());
            bean.setCinr(Integer.toString(mService.getSignalStrength()));
            bean.setLatitude(Float.toString(mService.getLatitude()));
            bean.setLongitude(Float.toString(mService.getLongitude()));
            DataDbOperations dbOp = new DataDbOperations(v.getContext());
            dbOp.initWrite();
            dbOp.storeData(bean);
        }
    }

    public void loadLocalDb(View v) throws JSONException {
        JSONArray jArray = new JSONArray();
        JSONObject jObject = new JSONObject();
        DataDbOperations dbOp = new DataDbOperations(v.getContext());
        DataBean data = new DataBean();
        if (dbOp.dataExists()) {
            dbOp.moveCursorToFirst();
            data = dbOp.getData();
            jArray.put(createJsonObject(data));
            while (!dbOp.isCursorLast()) {
                dbOp.moveCursorNext();
                data = dbOp.getData();
                jArray.put(createJsonObject(data));
            }
            jObject.put("data", jArray);
            System.out.println(jObject);
        }
    }

    public void clearDb(View v) {
        DataDbOperations dbOp = new DataDbOperations(v.getContext());
        dbOp.clearData();
    }

    JSONObject createJsonObject(DataBean data) throws JSONException {
        JSONObject result = new JSONObject();

        result.put(DataEntry.TIME_STAMP, data.getTimeStamp());
        result.put(DataEntry.USER, data.getUser());
        result.put(DataEntry.OPERATOR, data.getOperator());
        result.put(DataEntry.NETWORK_TYPE, data.getNetworkType());
        result.put(DataEntry.CINR, data.getCinr());
        result.put(DataEntry.LATITUDE, data.getLatitude());
        result.put(DataEntry.LONGITUDE, data.getLongitude());
        System.out.println(result);
        return result;
    }

    private void updateLocationUI() {
        latitudeField.setText("Latitute: " + mService.getLatitude());
        longitudeField.setText("Longitude: " + mService.getLongitude());

    }

    private void updateOperatorDetailsUI() {
        signalStrengthField.setText("Signal strength: " + mService.getSignalStrength());
        networkTypeField.setText("Network type: " + mService.getNetworkType());
        operatorNameField.setText("Operator name: " + mService.getOperatorName());
    }

    ServiceConnection serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className,IBinder service) {
                TalosBinder binder = (TalosBinder) service;
                mService = binder.getService();
                mBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }
    };

    BroadcastReceiver locationBrReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mBound){
                updateLocationUI();
                updateOperatorDetailsUI();
            }
        }
    };

}
