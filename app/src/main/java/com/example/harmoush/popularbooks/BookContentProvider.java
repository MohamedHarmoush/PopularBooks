package com.example.harmoush.popularbooks;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;


public class BookContentProvider extends ContentProvider{

    private  BookAppHelper bookAppHelper;
    private SQLiteDatabase database;

    public static final int BOOKS = 100;
    public static final int BOOK_WITH_ID = 101;
    private static final UriMatcher uriMatcher = buildUriMatcher();

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        final String authority = Contract.CONTENT_AUTHORITY;

        matcher.addURI(authority, Contract.BookTable.TABLE_NAME,BOOKS);
        matcher.addURI(authority, Contract.BookTable.TABLE_NAME+ "/id", BOOK_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {

        Context context = getContext();
        bookAppHelper = new BookAppHelper(context);

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        database = bookAppHelper.getReadableDatabase();
        switch (uriMatcher.match(uri)) {
            case BOOKS: {
                String selectQuery = "select * from " + Contract.BookTable.TABLE_NAME;
                cursor = database.rawQuery(selectQuery, null);
                break;
            }
            case BOOK_WITH_ID: {
                String selectQuery = "select * from " + Contract.BookTable.TABLE_NAME + " where id =" + selection;
                cursor = database.rawQuery(selectQuery, null);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        if (cursor.isAfterLast()) {
            return null;
        }
        database.close();
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        database = bookAppHelper.getWritableDatabase();
        Uri returnUri;
        long newRowID = database.insert(Contract.BookTable.TABLE_NAME, null, values);
        if (newRowID > 0) {
            returnUri = ContentUris.withAppendedId(Contract.BookTable.URI_FOR_SPECIFIC_ROW, newRowID);
            getContext().getContentResolver().notifyChange(returnUri, null);
            return returnUri;
        }
        throw new SQLException("Failed to insert new Book ");

    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        database = bookAppHelper.getWritableDatabase();
        int count = database.delete(Contract.BookTable.TABLE_NAME, "BookID = "+ "'"+selection+ "'", selectionArgs);

        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}

