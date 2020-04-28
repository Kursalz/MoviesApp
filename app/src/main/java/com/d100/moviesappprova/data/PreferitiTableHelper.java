package com.d100.moviesappprova.data;

import android.provider.BaseColumns;

public class PreferitiTableHelper implements BaseColumns {
    public static final String TABLE_NAME = "preferiti";

    public static final String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY)";
}
