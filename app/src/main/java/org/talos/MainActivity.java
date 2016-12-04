package org.talos;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.talos.enums.SettingEnum;
import org.talos.services.TalosService;
import org.talos.services.TalosService.TalosBinder;
import org.talos.utils.ServiceUtils;
import org.talos.utils.SettingsUtils;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    public static final int PERMISSION_LOCATION_REQUEST_CODE = 1;
    public static final int PERMISSION_NETWORK_REQUEST_CODE = 2;

    TalosService mService;
    boolean mBound = false;
    SettingsUtils sUtils = new SettingsUtils();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        TextView user = (TextView) findViewById(R.id.user);
        user.setText(sUtils.getSetting(this, SettingEnum.ACTIVE_USER));
        findViewById(R.id.start_service_button).setOnClickListener(this);
        findViewById(R.id.stop_service_button).setOnClickListener(this);
        updateUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((locationBrReceiver), new IntentFilter(TalosService.LOCATION_RESULT));
        SettingsUtils sUtils = new SettingsUtils();
        if(!sUtils.isUserLoggedIn(this)) {
            Intent signIn = new Intent(this, SignInActivity.class);
            startActivity(signIn);
        }
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
            case R.id.debug_prop:
                Intent debugAc = new Intent(this, DebugActivity.class);
                startActivity(debugAc);
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
        if (ServiceUtils.isMyServiceRunning(this) && !mBound){
            Intent serviceIntent = new Intent(this, TalosService.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        updateUI();

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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.start_service_button:
                startService();
                break;
            case R.id.stop_service_button:
                stopService();
                break;
        }
    }

    /**
     * Starts the {@link TalosService}
     */
    public void startService() {
        Intent serviceIntent = new Intent(this, TalosService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        updateUI();
    }

    /**
     * Stops the {@link TalosService}
     */
    public void stopService() {
        if(mBound){
            unbindService(serviceConnection);
            mBound = false;
        }
        Intent serviceIntent = new Intent(this, TalosService.class);
        stopService(serviceIntent);
        updateUI();
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
                //empty
            }
        }
    };

    void updateUI(){
        TextView serviceStatus = (TextView) findViewById(R.id.service_status);
        if (ServiceUtils.isMyServiceRunning(this)){
            findViewById(R.id.start_service_button).setVisibility(View.GONE);
            findViewById(R.id.stop_service_button).setVisibility(View.VISIBLE);
            serviceStatus.setText(getResources().getString(R.string.running));
            serviceStatus.setTextColor(getResources().getColor(R.color.colorPrimary));
        } else {
            findViewById(R.id.start_service_button).setVisibility(View.VISIBLE);
            findViewById(R.id.stop_service_button).setVisibility(View.GONE);
            serviceStatus.setText(getResources().getString(R.string.stopped));
            serviceStatus.setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

}
