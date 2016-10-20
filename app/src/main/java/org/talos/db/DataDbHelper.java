package org.talos.db;

import org.talos.db.DataContract.DataEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataDbHelper extends SQLiteOpenHelper  {

    public static final int DATABASE_VERSION = 7;
    public static final String DATABASE_NAME = "Data.db";
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + DataEntry.TABLE_NAME + " (" +
                    DataEntry.TIME_STAMP + " " + TEXT_TYPE + " PRIMARY KEY," +
                    DataEntry.USER + TEXT_TYPE + COMMA_SEP +
                    DataEntry.OPERATOR + TEXT_TYPE + COMMA_SEP +
                    DataEntry.NETWORK_TYPE + TEXT_TYPE + COMMA_SEP +
                    DataEntry.CINR + TEXT_TYPE + COMMA_SEP +
                    DataEntry.LATITUDE + TEXT_TYPE + COMMA_SEP +
                    DataEntry.LONGITUDE + TEXT_TYPE + " )";
    static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + DataEntry.TABLE_NAME;

    public DataDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
