package com.example.harmoush.popularbooks;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Harmoush on 2/3/2018.
 */

public class Contract {
    public static final String CONTENT_AUTHORITY = "com.example.harmoush.popularbooks";  // Authority
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
        public static final String PATH_BOOKS = "FavoriteBooks";

    public static final class BookTable implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();
        public static final Uri URI_FOR_SPECIFIC_ROW = CONTENT_URI.buildUpon().appendPath("id").build();

        public static final String TABLE_NAME = "FavoriteBooks";

        public static final String COULUMN_BOOK_ID = "BookID";
        public static final String COULUMN_BOOK_TITLE = "BookTitle";
        public static final String COULUMN_BOOK_PUBLISHDATE = "BookPublishDate";
        public static final String COULUMN_BOOK_DESCRIPTION = "BookDescription";
        public static final String COULUMN_BOOK_POSTERIMAGE = "BookPosterImage";
        public static final String COULUMN_BOOK_AUTHOR= "BookAuthor";

    }
}
