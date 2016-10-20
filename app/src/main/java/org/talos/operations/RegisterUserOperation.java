package org.talos.operations;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.talos.enums.WebServiceEnum;

import java.util.concurrent.ExecutionException;

/**
 * Created by michael on 18/10/2016.
 */
public class RegisterUserOperation {

    private static final String TAG = "RegisterUserOperation";

    private Context context;

    private String email;

    private String firstName;

    private String lastName;

    private String responseMessage;

    private boolean isOperationSuccess;

    public RegisterUserOperation(Context context) {
        this.context = context;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getResponseMessage(){
        return responseMessage;
    }

    public boolean getIsOperationsSuccess(){
        return isOperationSuccess;
    }

    public void execute() throws InterruptedException, ExecutionException {
        WebServiceCallerOperation wst = new WebServiceCallerOperation(WebServiceEnum.REGISTER_USER, context, responseMessage);
        wst.setParams(createJsonObject());
        wst.execute();
        try {
            JSONObject responseJson = new JSONObject(wst.get());
            responseMessage = responseJson.getString("message");
            isOperationSuccess = responseJson.getBoolean("isOperationSuccess");
        } catch (JSONException e) {
            Log.d(TAG, e.toString());
        }
    }

    private JSONObject createJsonObject() {
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("firstName", firstName);
            json.put("lastName", lastName);
            return json;
        } catch (JSONException e){
            Log.d(TAG, e.toString());
            return null;
        }
    }
}
