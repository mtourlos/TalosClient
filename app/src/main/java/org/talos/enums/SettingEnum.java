package org.talos.enums;

/**
 * Created by michael on 3/10/2016.
 */

public enum SettingEnum {
    ACTIVE_USER("settings_active_user"),

    SERVER_IP("settings_server_ip"),

    ACCURACY("settings_accuracy");



    private final String key;

    SettingEnum(String key){
        this.key = key;
    }

    public String getKey(){
        return key;
    }

}
