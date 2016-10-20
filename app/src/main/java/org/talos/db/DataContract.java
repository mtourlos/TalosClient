package org.talos.db;

import android.provider.BaseColumns;



public final class DataContract {

    public DataContract(){}

    public static abstract class DataEntry implements BaseColumns{
        public static final String TABLE_NAME="data";
        public static final String TIME_STAMP="timestamp";
        public static final String USER="user";
        public static final String OPERATOR="operator";
        public static final String CINR="cinr";
        public static final String NETWORK_TYPE="networkType";
        public static final String LATITUDE="latitude";
        public static final String LONGITUDE="longitude";

    }

}
