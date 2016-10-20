package org.talos.operations;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.talos.beans.DataBean;
import org.talos.db.DataContract;
import org.talos.db.DataDbOperations;
import org.talos.enums.WebServiceEnum;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class UploadDataOperation {

    private static final String TAG = "UploadDataOperation";

    private Context context;

    private String responseMessage;

    private boolean isOperationSuccess;

    public String getResponseMessage(){
        return responseMessage;
    }

    public boolean getIsOperationsSuccess(){
        return isOperationSuccess;
    }


    public UploadDataOperation(Context context) {
        super();
        this.context = context;
    }


    public void uploadData() throws InterruptedException, ExecutionException, JSONException, IllegalAccessException {
        DataDbOperations dbOp = new DataDbOperations(context);
        String response = "Processing";
        if (dbOp.dataExists()) {
            WebServiceCallerOperation wst = new WebServiceCallerOperation(WebServiceEnum.SEND_DATA, context, response);
            wst.setParams(getEntity());
            wst.execute();
            response = wst.get();
            handleResponseFromWebService(response);
            if (isOperationSuccess) {
                dbOp.clearData();
            }
        }
    }

    private void handleResponseFromWebService(String response){
        try {
            JSONObject responseJson = new JSONObject(response);
            responseMessage = responseJson.getString("message");
            isOperationSuccess = responseJson.getBoolean("isOperationSuccess");
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
        }
    }

    private JSONObject getEntity() throws JSONException, IllegalAccessException {
        JSONArray jArray = new JSONArray();
        JSONObject result = new JSONObject();
        DataDbOperations dbOp = new DataDbOperations(context);
        dbOp.initRead();
        dbOp.moveCursorToFirst();
        DataBean data = dbOp.getData();
        jArray.put(createJsonObject(data));
        while (!dbOp.isCursorLast()) {
            dbOp.moveCursorNext();
            data = dbOp.getData();
            if(!hasNull(data)) {
                jArray.put(createJsonObject(data));
            }
        }
        result.put("dataBean", jArray);
        dbOp.close();
        return result;
    }

    private JSONObject createJsonObject(DataBean data) throws JSONException {
        JSONObject result = new JSONObject();
        result.put(DataContract.DataEntry.TIME_STAMP, data.getTimeStamp());
        result.put(DataContract.DataEntry.USER, data.getUser());
        result.put(DataContract.DataEntry.OPERATOR, data.getOperator());
        result.put(DataContract.DataEntry.NETWORK_TYPE, data.getNetworkType());
        result.put(DataContract.DataEntry.CINR, data.getCinr());
        result.put(DataContract.DataEntry.LATITUDE, data.getLatitude());
        result.put(DataContract.DataEntry.LONGITUDE, data.getLongitude());
        return result;
    }

    private boolean hasNull(DataBean data) throws IllegalAccessException {
        for (Field f : data.getClass().getFields()){
            f.setAccessible(true);
            String value = (String) f.get(data);
            if (value == null || value.isEmpty()) {
                return true;
            }
        }
        return false;
    }

}
