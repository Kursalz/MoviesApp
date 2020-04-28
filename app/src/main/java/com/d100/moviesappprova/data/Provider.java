package com.d100.moviesappprova.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.d100.moviesappprova.activity.MainActivity;

public class Provider extends ContentProvider {

    public static final String AUTORITY = "com.d100.moviesappprova.data.Provider";
    public static final String BASE_PATH_FILMS = "films";
    public static final int ALL_FILM = 1;
    public static final int SINGLE_FILM = 0;
    public static final String MIME_TYPE_FILMS = ContentResolver.CURSOR_DIR_BASE_TYPE + "vnd.all_films";
    public static final String MIME_TYPE_FILM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "vnd.single_film";

    public static Uri FILMS_URI = Uri.parse(ContentResolver.SCHEME_CONTENT + "://" + AUTORITY + "/" + BASE_PATH_FILMS);

    private DB mDb;

    private static final UriMatcher mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH); // serve per capire cosa ritorna

    static { // questa parte viene eseguita prima del main, è una subrutine scritta in c
        mUriMatcher.addURI(AUTORITY, BASE_PATH_FILMS, ALL_FILM);
        mUriMatcher.addURI(AUTORITY, BASE_PATH_FILMS + "/#", SINGLE_FILM);
    }

    @Override
    public boolean onCreate() {
        mDb = new DB(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        SQLiteDatabase vDb = mDb.getReadableDatabase();
        SQLiteQueryBuilder vBuilder = new SQLiteQueryBuilder();

        switch (mUriMatcher.match(uri)) {
            case ALL_FILM:
                vBuilder.setTables(TableHelper.TABLE_NAME);
                break;
            case SINGLE_FILM:
                vBuilder.setTables(TableHelper.TABLE_NAME);
                vBuilder.appendWhere(TableHelper._ID + " = " + uri.getLastPathSegment());
                break;
        }

        Cursor vCursor = vBuilder.query(vDb, strings, s, strings1, null, null, s1);

        vCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return vCursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case ALL_FILM:
                return MIME_TYPE_FILMS;
            case SINGLE_FILM:
                return MIME_TYPE_FILM;
        }
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        try {
            if (mUriMatcher.match(uri) == ALL_FILM) {
                SQLiteDatabase vDb = mDb.getWritableDatabase();
                long vResult = vDb.insertOrThrow(TableHelper.TABLE_NAME, null, contentValues);
                String vString = ContentResolver.SCHEME_CONTENT + "://" + BASE_PATH_FILMS + "/" + vResult;
                getContext().getContentResolver().notifyChange(uri, null);
                return Uri.parse(uri.toString() + "/" + vString);
            }

        } catch (SQLiteConstraintException e) {
            int movieId = contentValues.getAsInteger(TableHelper._ID);
            int nRows = update(Uri.parse(Provider.FILMS_URI + "/" + movieId), contentValues, null, null);
            if (nRows == 0){
                throw e;
            }
        }
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        String vTableName = "";
        String vQuery = "";

        SQLiteDatabase vDb = mDb.getWritableDatabase();

        switch (mUriMatcher.match(uri)) {
            case ALL_FILM:
                vTableName = TableHelper.TABLE_NAME;
                vQuery = s;
                break;
            case SINGLE_FILM:
                vTableName = TableHelper.TABLE_NAME;
                vQuery = TableHelper._ID + " = " + uri.getLastPathSegment();
                if (s != null) {
                    vQuery += " AND " + s;
                }
                break;
        }
        int vDeletedRows = vDb.delete(vTableName, vQuery, strings);
        getContext().getContentResolver().notifyChange(uri, null);

        return vDeletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        String vTableName = "";
        String vQuery = "";

        SQLiteDatabase vDb = mDb.getWritableDatabase();

        switch (mUriMatcher.match(uri)) {
            case ALL_FILM:
                vTableName = TableHelper.TABLE_NAME;
                vQuery = s;
                break;
            case SINGLE_FILM:
                vTableName = TableHelper.TABLE_NAME;
                vQuery = TableHelper._ID + " = " + uri.getLastPathSegment();
                if (s != null) {
                    vQuery += " AND " + s;
                }
                break;
        }
        int vUpdatedRows = vDb.update(vTableName, contentValues, vQuery, strings);
        getContext().getContentResolver().notifyChange(uri, null);

        return vUpdatedRows;
    }
}
