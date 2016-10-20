package org.talos.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import org.talos.operations.UploadDataOperation;

public class NetworkChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkChangeReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI){
            UploadDataOperation up = new UploadDataOperation(context);
            try {
                up.uploadData();
                if (up.getIsOperationsSuccess()){
                    Toast.makeText(context, "Upload Succeed", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(context, "Upload Failed", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }
    }
}
