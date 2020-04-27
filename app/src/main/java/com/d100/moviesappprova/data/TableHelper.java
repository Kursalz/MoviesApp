package com.d100.moviesappprova.data;

import android.provider.BaseColumns;

public class TableHelper implements BaseColumns {

    public static final String TABLE_NAME = "Film";
    public static final String POSTER_PATH = "poster_path";
    public static final String ADULT ="adult";
    public static final String OVERVIEW ="overview";
    public static final String RELEASE_DATE ="release_date";
    public static final String ORIGINAL_TITLE ="original_title";
    public static final String ORIGINAL_LANGUAGE ="original_language";
    public static final String TITLE ="title";
    public static final String BACKDROP_PATH ="backdrop_path";
    public static final String POPULARITY ="popularity";
    public static final String VOTE_COUNT ="vote_count";
    public static final String VIDEO ="video";
    public static final String VOTE_AVERAGE ="vote_average";

    public static final String CREATE_TABLE ="CREATE TABLE "+TABLE_NAME+" ( "
            + _ID + " INTEGER PRIMARY KEY,"
            +POSTER_PATH+" TEXT,"
            +ADULT+" INTEGER,"
            +OVERVIEW+" TEXT,"
            +RELEASE_DATE+" TEXT,"
            +ORIGINAL_TITLE+" TEXT,"
            +ORIGINAL_LANGUAGE+" TEXT,"
            +TITLE+" TEXT,"
            +BACKDROP_PATH+" TEXT,"
            +POPULARITY+" REAL,"
            +VOTE_COUNT+" INTEGER,"
            +VIDEO+" INTEGER,"
            +VOTE_AVERAGE+" REAL);";

}
