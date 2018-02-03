package com.example.harmoush.popularbooks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.DefaultItemAnimator;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harmoush on 2/3/2018.
 */

public class WidgetDataProvider implements RemoteViewsService.RemoteViewsFactory {

    List<String> mCollection = new ArrayList<>();
    Context mContext = null;
    List<Book> mBooks  = new ArrayList<>();

    public WidgetDataProvider(Context context, Intent intent) {
        mContext = context;
    }

    @Override
    public void onCreate() {
        initData();
    }

    @Override
    public void onDataSetChanged() {
        initData();
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return mCollection.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews view = new RemoteViews(mContext.getPackageName(),
                android.R.layout.simple_list_item_1);
        view.setTextViewText(android.R.id.text1, mCollection.get(position));
        return view;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    private void initData() {
        mCollection.clear();
        Thread thread = new Thread() {
            public void run() {
                getBooksFromDB();
            }
        };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
        }
        if(mBooks.size() == 0)
            mCollection.add(String.valueOf(R.string.no_connection));
        else{
            for (int i = 0; i < mBooks.size(); i++) {
                //collection.add(mBooks.get(i).bookTitle);
                mCollection.add(mBooks.get(i).bookTitle);
            }
        }
    }

    private void getBooksFromDB(){
        Cursor cursor = mContext.getContentResolver().query(Contract.BookTable.CONTENT_URI,null,null,null,null);
        mBooks = getBooks(cursor);
    }

    public ArrayList<Book> getBooks(Cursor cursor) {
        ArrayList<Book> favouriteBooks = new ArrayList<>();
        if (cursor == null)
            return favouriteBooks;
        while (cursor.moveToNext()) {
            Book book = new Book();
            book.bookId = cursor.getString(cursor.getColumnIndex("BookID"));
            book.bookTitle = cursor.getString(cursor.getColumnIndex("BookTitle"));
            book.bookPublishedDate = cursor.getString(cursor.getColumnIndex("BookPublishDate"));
            book.bookDescription = cursor.getString(cursor.getColumnIndex("BookDescription"));
            book.bookPosterImage = cursor.getString(cursor.getColumnIndex("BookPosterImage"));
            book.bookAuthor = cursor.getString(cursor.getColumnIndex("BookAuthor"));

            favouriteBooks.add(book);
        }
        return favouriteBooks;
    }

}
