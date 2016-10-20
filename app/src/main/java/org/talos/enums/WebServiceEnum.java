package org.talos.enums;

import android.content.IntentSender;

/**
 * Created by michael on 8/10/2016.
 */

public enum WebServiceEnum {

    REGISTER_USER("registeruser", WebServiceMethodEnum.POST),

    SEND_DATA("datas", WebServiceMethodEnum.POST);

    private final String url;

    private final WebServiceMethodEnum method;

    WebServiceEnum(String url, WebServiceMethodEnum method){
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public WebServiceMethodEnum getMethod(){
        return method;
    }

}
