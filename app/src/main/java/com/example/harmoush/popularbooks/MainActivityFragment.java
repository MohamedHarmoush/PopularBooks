package com.example.harmoush.popularbooks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.JsonArray;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Harmoush on 2/1/2018.
 */

public class MainActivityFragment extends Fragment implements BooksAdapter.ListItemClickListener{

    public MainActivityFragment() {
    }
    private View root;
    final static String BASIC_API_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    final static String API_KEY = BuildConfig.GoogleBooksApiKey;

    private SQLiteDatabase mDb;

    @BindView(R.id.rv_books)RecyclerView mBooksRecyclerView;
    private String mSortType;
    private BooksAdapter adapter;
    private GridLayoutManager mgridLayoutManager;
    private int numberOfViews ;
    private String App_ID;
    @BindView(R.id.pb_loading_indiactor)
    ProgressBar mLoadingProgressBar;
    private Context context;
    private static ArrayList<Book> mBooks;
    public static ArrayList<Book> mBooksWidget;
    @BindView(R.id.adView) AdView mAdView;
    @BindView(R.id.btn_search)
    Button mSearch;
    @BindView(R.id.et_search)
    TextView mSearchText;
    @BindView(R.id.tv_no_connection)
    TextView mNoConnection;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.display_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_popular_books:
                mSortType = "popular";
                break;
            case R.id.action_favorite:
                mSortType = "favourites";
                break;

        }
        if( mSortType != "favourites")
            fetchDataFromInternet();
        else
            getBooksFromDB();
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.content_main, container, false);
        ButterKnife.bind(this,root);
        App_ID = "ca-app-pub-9337464230725544/4549973935";
        MobileAds.initialize(getActivity() ,App_ID );
        mBooks = new ArrayList<>();
        mBooksWidget = new ArrayList<>();
        adapter = new BooksAdapter(mBooks,this);
        context = getActivity();
        numberOfViews = calculateNoOfColumns(context);
        mgridLayoutManager = new GridLayoutManager(context,numberOfViews,GridLayoutManager.VERTICAL,false);
        mBooksRecyclerView.setLayoutManager(mgridLayoutManager);
        mBooksRecyclerView.setAdapter(adapter);

        /////////////////////////////////////
        /*get Data from firebse*/
        Bundle b = getActivity().getIntent().getExtras();
        if(b != null) {
            String data = b.getString("data");
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
        }
        ////////////////////////////////////////////////////
        if(savedInstanceState != null){
            mBooks = savedInstanceState.getParcelableArrayList("books");
            adapter = new BooksAdapter(mBooks,this);
            mBooksRecyclerView.setAdapter(adapter);
        }else {
            if (isNetworkAvailable()) {
                try {
                    mNoConnection.setVisibility(View.INVISIBLE);
                    fetchDataFromInternet();
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);
                } catch (Exception e) {
                    FirebaseCrash.log("add error");
                    FirebaseCrash.report(e);
                    Toast.makeText(context,"Ads google error",Toast.LENGTH_SHORT).show();
                }
            } else {
                getBooksFromDB();
                Toast.makeText(context,"No Internet Connection!!",Toast.LENGTH_SHORT).show();
               // mNoConnection.setText(R.string.no_connection);
                //mNoConnection.setVisibility(View.VISIBLE);
            }

        }
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    hideKeyboard();
                    fetchDataFromInternet();
                }else {
                  //  mBooks.clear();
                    //mNoConnection.setText(R.string.no_connection);
                    //mNoConnection.setVisibility(View.VISIBLE);
                    Toast.makeText(context,"No Internet Connection!!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        return root;
    }

    public static ArrayList<Book> getmBooks() {

        return mBooks;
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 180;
        int noOfColumns = (int) (dpWidth / scalingFactor);
        return noOfColumns;
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void fetchDataFromInternet() {

        mLoadingProgressBar.setVisibility(View.VISIBLE);
        mBooks.clear();
        String searchText = "love";
        if(!(mSearchText == null || mSearchText.getText().equals("")))
            searchText = mSearchText.getText().toString();
        Ion.with(this)
                .load(BASIC_API_URL+searchText+"+insubject:keyes&key=" +API_KEY)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject  result) {
                        // do stuff with the result or error
                        if (e == null) {
                            JsonArray jsonArray= result.getAsJsonArray("items");
                            for(int i =0 ;i<jsonArray.size();i++) {
                                JsonObject bookJsonObject = jsonArray.get(i).getAsJsonObject();
                                String bookId,title,publishedDate,description,imageLink;
                                if(bookJsonObject.has("id"))
                                    bookId = bookJsonObject.get("id").getAsString().replace("\"","");
                                else
                                    bookId ="";
                                JsonObject volumeInfo;
                                if(bookJsonObject.has("volumeInfo")) {
                                    volumeInfo = bookJsonObject.get("volumeInfo").getAsJsonObject();
                                    if( volumeInfo.has("title"))
                                        title = volumeInfo.get("title").getAsString().replace("\"","");
                                    else
                                        title ="";
                                    String bookAuthors ="";
                                    if(volumeInfo.has("authors")){
                                        JsonArray authors =  volumeInfo.get("authors").getAsJsonArray();
                                        for(int j=0;j<authors.size();j++) {
                                            String au = authors.get(j).getAsString().replace("\"","");
                                            if (j+1 <authors.size())
                                                bookAuthors+=au +" && ";
                                            else
                                                bookAuthors+=au;
                                        }
                                    }else if(volumeInfo.has("publisher")){
                                        String au=  volumeInfo.get("publisher").getAsString().replace("\"","");
                                        bookAuthors+=au;
                                    }
                                    if( volumeInfo.has("publishedDate"))
                                        publishedDate = volumeInfo.get("publishedDate").getAsString().replace("\"","");
                                    else
                                        publishedDate ="";
                                    if( volumeInfo.has("description"))
                                        description = volumeInfo.get("description").getAsString().replace("\"","");
                                    else
                                        description ="";
                                    if( volumeInfo.has("imageLinks")){

                                        JsonObject imageLinks = volumeInfo.get("imageLinks").getAsJsonObject();
                                        if (imageLinks.has("thumbnail"))
                                            imageLink = imageLinks.get("thumbnail").getAsString().replace("\"","");
                                        else if(imageLinks.has("medium"))
                                            imageLink = imageLinks.get("medium").getAsString().replace("\"","");
                                        else if(imageLinks.has("smallThumbnail"))
                                            imageLink = imageLinks.get("smallThumbnail").getAsString().replace("\"","");
                                        else if(imageLinks.has("large"))
                                            imageLink = imageLinks.get("large").getAsString().replace("\"","");
                                        else
                                            imageLink ="";
                                    }
                                    else
                                        imageLink ="";
                                    if(imageLink =="")
                                        continue;
                                    mBooks.add(new Book(title,publishedDate,description,bookAuthors,imageLink,bookId));
                                }else
                                    continue;
                            }
                            adapter.notifyDataSetChanged();
                        }

                    }
                });

        mLoadingProgressBar.setVisibility(View.INVISIBLE);


    }
    // ToDo 1:- onSaveInstanceState ....Done
    // ToDo 2:- DataBase
    //ToDo  3:- menu item favorites .... Done
    //ToDo  4:- Widget
    //ToDo RTL

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("books", mBooks);
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState !=null)
            mBooks  = savedInstanceState.getParcelableArrayList("books");
    }
    @Override
    public void onListItemClickListener(int clikedItemIndex) {

        Book book = mBooks.get(clikedItemIndex);
        Intent intent =  new Intent(context,DetailActivity.class);
        intent.putExtra("book",book);
        startActivity(intent);

    }

    private void getBooksFromDB(){
        Cursor cursor = getActivity().getContentResolver().query(Contract.BookTable.CONTENT_URI,null,null,null,null);
        mBooksRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mBooks = getBooks(cursor);
        adapter = new BooksAdapter(mBooks,this);
        mBooksRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public ArrayList<Book> getBooks(Cursor cursor) {
        ArrayList<Book> favouriteBooks = new ArrayList<>();
        if (cursor == null)
            return favouriteBooks;
        // cursor.moveToFirst();
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
