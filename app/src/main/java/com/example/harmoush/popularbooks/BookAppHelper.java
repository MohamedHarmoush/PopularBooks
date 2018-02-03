package com.example.harmoush.popularbooks;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Harmoush on 1/31/2018.
 */

class BookAppHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "BookDB.db";
    private static final int DB_Version = 1;
    private static final String ColumnType =" TEXT";
    private static final String LOG_TAG = BookAppHelper.class.getName();

    private static final String SQL_DELETE_DATABASE = "DROP TABLE IF EXIST " + Contract.BookTable.TABLE_NAME ;

    public BookAppHelper(Context context) {
        super(context, DB_NAME, null, DB_Version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_DATABASE = "CREATE TABLE " +
                Contract.BookTable.TABLE_NAME + "(" +
                Contract.BookTable._ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                Contract.BookTable.COULUMN_BOOK_ID +" TEXT NOT NULL, " +
                Contract.BookTable.COULUMN_BOOK_TITLE +" TEXT NOT NULL, "+
                Contract.BookTable.COULUMN_BOOK_PUBLISHDATE+" TEXT, "+
                Contract.BookTable.COULUMN_BOOK_DESCRIPTION + " TEXT, "+
                Contract.BookTable.COULUMN_BOOK_POSTERIMAGE + " TEXT, "+
                Contract.BookTable.COULUMN_BOOK_AUTHOR + " TEXT"+" );";

        db.execSQL(SQL_CREATE_DATABASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_DATABASE);
        onCreate(db);

    }
}
