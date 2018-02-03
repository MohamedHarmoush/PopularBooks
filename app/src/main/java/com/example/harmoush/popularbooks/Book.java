package com.example.harmoush.popularbooks;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Harmoush on 1/31/2018.
 */
public class Book implements Parcelable {
    public String bookTitle;
    public String bookPublishedDate ;
    public String bookDescription ;
    public String bookAuthor ;
    public String bookPosterImage ;
    public String bookId ;
    public Book(){}
    public Book(String bookTitle, String bookPublishedDate, String bookDescription, String bookAuthor, String bookPosterImage, String bookId) {
        this.bookTitle = bookTitle;
        this.bookPublishedDate = bookPublishedDate;
        this.bookDescription = bookDescription;
        this.bookAuthor = bookAuthor;
        this.bookPosterImage = bookPosterImage;
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookPublishedDate() {
        return bookPublishedDate;
    }

    public String getBookDescription() {
        return bookDescription;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public String getBookPosterImage() {
        return bookPosterImage;
    }

    public String getBookId() {
        return bookId;
    }



    protected Book(Parcel in) {
        bookTitle = in.readString();
        bookPublishedDate = in.readString();
        bookDescription = in.readString();
        bookAuthor = in.readString();
        bookPosterImage = in.readString();
        bookId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookTitle);
        dest.writeString(bookPublishedDate);
        dest.writeString(bookDescription);
        dest.writeString(bookAuthor);
        dest.writeString(bookPosterImage);
        dest.writeString(bookId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}