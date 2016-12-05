package org.talos.enums;

import android.content.IntentSender;

/**
 * Created by michael on 8/10/2016.
 */

public enum WebServiceEnum {

    REGISTER_USER("registeruser", WebServiceMethodEnum.POST),

    SEND_DATA("datas", WebServiceMethodEnum.POST);

    public static final String SERVER_IP_PARAMETER = ":serverIp";
    private static final String COMPLETE_URL = "http://"+ SERVER_IP_PARAMETER +":8080/TalosServer/service/userservice/";

    private final String url;

    private final WebServiceMethodEnum method;

    WebServiceEnum(String url, WebServiceMethodEnum method){
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return COMPLETE_URL + url;
    }

    public WebServiceMethodEnum getMethod(){
        return method;
    }

}
