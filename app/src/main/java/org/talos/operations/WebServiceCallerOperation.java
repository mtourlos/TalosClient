package org.talos.operations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.talos.beans.DataBean;
import org.talos.db.DataContract.DataEntry;
import org.talos.db.DataDbHelper;
import org.talos.db.DataDbOperations;
import org.talos.enums.SettingEnum;
import org.talos.enums.WebServiceEnum;
import org.talos.enums.WebServiceMethodEnum;
import org.talos.utils.SettingsUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class WebServiceCallerOperation extends AsyncTask<Void, Void, String> {

    // connection parameters
    private static final int CONN_TIMEOUT = 3000;
    private static final int SOCKET_TIMEOUT = 5000;
    private static final String TAG = "WebServiceCallerOp";
    private static final String URL_PREFIX = "http://";
    private static final String URL_SUFIX = ":8080/TalosServer/service/userservice/";
    private Context context = null;

    private WebServiceEnum taskType;

    private JSONObject params;

    private String response;

    public WebServiceCallerOperation(WebServiceEnum taskType, Context context, String response) {
        this.taskType = taskType;
        this.context = context;
        this.response = response;
    }

    public void setParams(JSONObject params) {
        this.params = params;
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected String doInBackground(Void... params) {
        String url = getUrl();
        String result = "";
        HttpResponse response = doResponse(url);

        if (response == null) {
            return result;
        } else {
            try {
                result = inputStreamToString(response.getEntity().getContent());
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(String response) {


        System.out.println(response);
    }

    // Connection Establishment
    private HttpParams getHttpParams() {

        HttpParams http = new BasicHttpParams();

        HttpConnectionParams.setConnectionTimeout(http, CONN_TIMEOUT);
        HttpConnectionParams.setSoTimeout(http, SOCKET_TIMEOUT);

        return http;
    }

    private HttpResponse doResponse(String url) {

        // Use our connection and data timeouts as parameters for our
        // DefaultHttpClient
        HttpClient httpclient = new DefaultHttpClient(getHttpParams());

        HttpResponse response = null;

        try {
            if (taskType.getMethod().equals(WebServiceMethodEnum.POST)) {
                HttpPost httppost = new HttpPost(url);
                String entity = params.toString();
                System.out.println(entity);
                StringEntity se = new StringEntity(entity);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setHeader("Content-type", "application/json");
                httppost.setEntity(se);
                response = httpclient.execute(httppost);
            }else if(taskType.getMethod().equals(WebServiceMethodEnum.GET)){
                HttpGet httpget = new HttpGet(url);
                response = httpclient.execute(httpget);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        return response;
    }

    private String inputStreamToString(InputStream is) {

        String line = "";
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            // Read responseMessage until the end
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        // Return full string
        return total.toString();
    }

    private String getUrl(){
        String url;
        SettingsUtils su = new SettingsUtils();
        String serverIp = su.getSetting(context, SettingEnum.SERVER_IP);
        url = URL_PREFIX + serverIp + URL_SUFIX + taskType.getUrl();
        return url;
    }

}
