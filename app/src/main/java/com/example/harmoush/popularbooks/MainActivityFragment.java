package com.example.harmoush.popularbooks;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.LoaderManager;
import android.content.Context;

import android.content.Intent;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
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
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Harmoush on 2/1/2018.
 */

public class MainActivityFragment extends Fragment implements BooksAdapter.ListItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    public MainActivityFragment() {
    }

    private View root;
    final static String BASIC_API_URL = "https://www.googleapis.com/books/v1/volumes?q=";
    final static String API_KEY = BuildConfig.GoogleBooksApiKey;

    final int LOADER_LOAD = 1;
    @BindView(R.id.rv_books)
    RecyclerView mBooksRecyclerView;
    private String mSortType;
    private BooksAdapter adapter;
    private GridLayoutManager mgridLayoutManager;
    private int numberOfViews;
    private String App_ID;
    @BindView(R.id.pb_loading_indiactor)
    ProgressBar mLoadingProgressBar;
    private Context context;
    private static ArrayList<Book> mBooks;
    public static ArrayList<Book> mBooksWidget;
    @BindView(R.id.adView)
    AdView mAdView;
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
        switch (id) {
            case R.id.action_popular_books:
                mSortType = getString(com.example.harmoush.popularbooks.R.string.popular);
                break;
            case R.id.action_favorite:
                mSortType = getString(R.string.favorites);
                break;

        }
        if (mSortType != getString(R.string.favorites))
            fetchDataFromInternet();
        else
            offlineMode();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isNetworkAvailable()) {
            offlineMode();

        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.content_main, container, false);
        ButterKnife.bind(this, root);
        App_ID = getString(R.string.AppIDads);
        MobileAds.initialize(getActivity(), App_ID);
        mBooks = new ArrayList<>();
        mBooksWidget = new ArrayList<>();
        adapter = new BooksAdapter(mBooks, this);
        context = getActivity();
        numberOfViews = calculateNoOfColumns(context);
        mgridLayoutManager = new GridLayoutManager(context, numberOfViews, GridLayoutManager.VERTICAL, false);
        mBooksRecyclerView.setLayoutManager(mgridLayoutManager);
        mBooksRecyclerView.setAdapter(adapter);

        /////////////////////////////////////
        /*get Data from firebse*/
        Bundle b = getActivity().getIntent().getExtras();
        if (b != null) {
            String data = b.getString(getString(R.string.datakey));
            Toast.makeText(context, data, Toast.LENGTH_SHORT).show();
        }
        ////////////////////////////////////////////////////
        if (savedInstanceState != null) {
            mBooks = savedInstanceState.getParcelableArrayList(getString(R.string.bookskey));
            adapter = new BooksAdapter(mBooks, this);
            mBooksRecyclerView.setAdapter(adapter);
        } else {
            if (isNetworkAvailable()) {
                try {
                    mNoConnection.setVisibility(View.INVISIBLE);
                    fetchDataFromInternet();
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);
                } catch (Exception e) {
                    FirebaseCrash.log(getString(R.string.firebaseError));
                    FirebaseCrash.report(e);
                    //Toast.makeText(context, R.string.adsError,Toast.LENGTH_SHORT).show();
                }
            } else {
                //getBooksFromDB();
                offlineMode();
                Snackbar.make(root, R.string.noConnection, Snackbar.LENGTH_SHORT).show();
                //Toast.makeText(context,"No Internet Connection!!",Toast.LENGTH_SHORT).show();
            }

        }
        mSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable()) {
                    hideKeyboard();
                    //fetchDataFromInternet();
                    URL url = null;
                    try {
                        String searchText = mSearchText.getText().toString();
                        String urlstr =BASIC_API_URL + searchText + getString(R.string.APISearch) + API_KEY;
                        url = new URL(urlstr);
                        JsonObject jsonObject = new FetchBooksDataFromInternet().execute(url).get();
                        parseJsonObject(jsonObject);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                } else {
                    //  mBooks.clear();
                    //mNoConnection.setText(R.string.no_connection);
                    //mNoConnection.setVisibility(View.VISIBLE);
                    offlineMode();
                    Snackbar.make(root, R.string.noConnection, Snackbar.LENGTH_SHORT).show();
                    // Toast.makeText(context,"No Internet Connection!!",Toast.LENGTH_SHORT).show();
                }
            }
        });

        return root;
    }

    void offlineMode() {
        mBooks.clear();
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.restartLoader(LOADER_LOAD, null, MainActivityFragment.this);
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
        String searchText = getString(R.string.love);
        if (!(mSearchText == null || mSearchText.getText().equals("")))
            searchText = mSearchText.getText().toString();
        Ion.with(this)
                .load(BASIC_API_URL + searchText + getString(R.string.APISearch) + API_KEY)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        // do stuff with the result or error
                        if (e == null) {
                            /*JsonArray jsonArray = result.getAsJsonArray(getString(R.string.items));
                            for (int i = 0; i < jsonArray.size(); i++) {
                                JsonObject bookJsonObject = jsonArray.get(i).getAsJsonObject();
                                String bookId, title, publishedDate, description, imageLink;
                                if (bookJsonObject.has(getString(R.string.id)))
                                    bookId = bookJsonObject.get(getString(R.string.id)).getAsString().replace("\"", "");
                                else
                                    bookId = "";
                                JsonObject volumeInfo;
                                if (bookJsonObject.has(getString(R.string.volumeInfo))) {
                                    volumeInfo = bookJsonObject.get(getString(R.string.volumeInfo)).getAsJsonObject();
                                    if (volumeInfo.has(getString(R.string.title)))
                                        title = volumeInfo.get(getString(R.string.title)).getAsString().replace("\"", "");
                                    else
                                        title = "";
                                    String bookAuthors = "";
                                    if (volumeInfo.has(getString(R.string.authors))) {
                                        JsonArray authors = volumeInfo.get(getString(R.string.authors)).getAsJsonArray();
                                        for (int j = 0; j < authors.size(); j++) {
                                            String au = authors.get(j).getAsString().replace("\"", "");
                                            if (j + 1 < authors.size())
                                                bookAuthors += au + getString(com.example.harmoush.popularbooks.R.string.and);
                                            else
                                                bookAuthors += au;
                                        }
                                    } else if (volumeInfo.has(getString(R.string.publisher))) {
                                        String au = volumeInfo.get(getString(R.string.publisher)).getAsString().replace("\"", "");
                                        bookAuthors += au;
                                    }
                                    if (volumeInfo.has(getString(R.string.publishedDate)))
                                        publishedDate = volumeInfo.get(getString(R.string.publishedDate)).getAsString().replace("\"", "");
                                    else
                                        publishedDate = "";
                                    if (volumeInfo.has(getString(R.string.description)))
                                        description = volumeInfo.get(getString(R.string.description)).getAsString().replace("\"", "");
                                    else
                                        description = "";
                                    if (volumeInfo.has(getString(R.string.imageLinks))) {

                                        JsonObject imageLinks = volumeInfo.get(getString(R.string.imageLinks)).getAsJsonObject();
                                        if (imageLinks.has(getString(R.string.thumbnail)))
                                            imageLink = imageLinks.get(getString(R.string.thumbnail)).getAsString().replace("\"", "");
                                        else if (imageLinks.has(getString(R.string.medium)))
                                            imageLink = imageLinks.get(getString(R.string.medium)).getAsString().replace("\"", "");
                                        else if (imageLinks.has(getString(R.string.smallThumbnail)))
                                            imageLink = imageLinks.get(getString(R.string.smallThumbnail)).getAsString().replace("\"", "");
                                        else if (imageLinks.has(getString(R.string.large)))
                                            imageLink = imageLinks.get(getString(R.string.large)).getAsString().replace("\"", "");
                                        else
                                            imageLink = "";
                                    } else
                                        imageLink = "";
                                    if (imageLink == "")
                                        continue;
                                    mBooks.add(new Book(title, publishedDate, description, bookAuthors, imageLink, bookId));
                                } else
                                    continue;
                            }*/
                            parseJsonObject(result);

                            adapter.notifyDataSetChanged();
                        }

                    }
                });

        mLoadingProgressBar.setVisibility(View.INVISIBLE);


    }

    public void parseJsonObject(JsonObject jsonObject){
        JsonArray jsonArray = jsonObject.getAsJsonArray(getString(R.string.items));
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject bookJsonObject = jsonArray.get(i).getAsJsonObject();
            String bookId, title, publishedDate, description, imageLink;
            if (bookJsonObject.has(getString(R.string.id)))
                bookId = bookJsonObject.get(getString(R.string.id)).getAsString().replace("\"", "");
            else
                bookId = "";
            JsonObject volumeInfo;
            if (bookJsonObject.has(getString(R.string.volumeInfo))) {
                volumeInfo = bookJsonObject.get(getString(R.string.volumeInfo)).getAsJsonObject();
                if (volumeInfo.has(getString(R.string.title)))
                    title = volumeInfo.get(getString(R.string.title)).getAsString().replace("\"", "");
                else
                    title = "";
                String bookAuthors = "";
                if (volumeInfo.has(getString(R.string.authors))) {
                    JsonArray authors = volumeInfo.get(getString(R.string.authors)).getAsJsonArray();
                    for (int j = 0; j < authors.size(); j++) {
                        String au = authors.get(j).getAsString().replace("\"", "");
                        if (j + 1 < authors.size())
                            bookAuthors += au + getString(com.example.harmoush.popularbooks.R.string.and);
                        else
                            bookAuthors += au;
                    }
                } else if (volumeInfo.has(getString(R.string.publisher))) {
                    String au = volumeInfo.get(getString(R.string.publisher)).getAsString().replace("\"", "");
                    bookAuthors += au;
                }
                if (volumeInfo.has(getString(R.string.publishedDate)))
                    publishedDate = volumeInfo.get(getString(R.string.publishedDate)).getAsString().replace("\"", "");
                else
                    publishedDate = "";
                if (volumeInfo.has(getString(R.string.description)))
                    description = volumeInfo.get(getString(R.string.description)).getAsString().replace("\"", "");
                else
                    description = "";
                if (volumeInfo.has(getString(R.string.imageLinks))) {

                    JsonObject imageLinks = volumeInfo.get(getString(R.string.imageLinks)).getAsJsonObject();
                    if (imageLinks.has(getString(R.string.thumbnail)))
                        imageLink = imageLinks.get(getString(R.string.thumbnail)).getAsString().replace("\"", "");
                    else if (imageLinks.has(getString(R.string.medium)))
                        imageLink = imageLinks.get(getString(R.string.medium)).getAsString().replace("\"", "");
                    else if (imageLinks.has(getString(R.string.smallThumbnail)))
                        imageLink = imageLinks.get(getString(R.string.smallThumbnail)).getAsString().replace("\"", "");
                    else if (imageLinks.has(getString(R.string.large)))
                        imageLink = imageLinks.get(getString(R.string.large)).getAsString().replace("\"", "");
                    else
                        imageLink = "";
                } else
                    imageLink = "";
                if (imageLink == "")
                    continue;
                mBooks.add(new Book(title, publishedDate, description, bookAuthors, imageLink, bookId));
            }else
                continue;;
        }
        adapter.notifyDataSetChanged();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(getString(R.string.datakey), mBooks);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null)
            mBooks = savedInstanceState.getParcelableArrayList(getString(R.string.datakey));
    }

    @Override
    public void onListItemClickListener(int clikedItemIndex) {

        Book book = mBooks.get(clikedItemIndex);
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(getString(R.string.book), book);
        startActivity(intent);

    }

    private void getBooksFromDB(Cursor cursor) {
        // Cursor cursor = getActivity().getContentResolver().query(Contract.BookTable.CONTENT_URI,null,null,null,null);
        mBooksRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mBooks = getBooks(cursor);
        adapter = new BooksAdapter(mBooks, this);
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
            book.bookId = cursor.getString(cursor.getColumnIndex(getString(R.string.BookID)));
            book.bookTitle = cursor.getString(cursor.getColumnIndex(getString(R.string.BookTitle)));
            book.bookPublishedDate = cursor.getString(cursor.getColumnIndex(getString(R.string.BookPublishDate)));
            book.bookDescription = cursor.getString(cursor.getColumnIndex(getString(R.string.BookDescription)));
            book.bookPosterImage = cursor.getString(cursor.getColumnIndex(getString(R.string.BookPosterImage)));
            book.bookAuthor = cursor.getString(cursor.getColumnIndex(getString(R.string.BookAuthor)));

            favouriteBooks.add(book);
        }
        return favouriteBooks;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_LOAD)
            return new CursorLoader(getActivity(), Contract.BookTable.CONTENT_URI, null, null, null, null);
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        getBooksFromDB(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    ///////////////////////////////////////////////////////////////////////////////////////
    //// using asyntask to fetch data for search
    ////////////////////////////////////////////////////////////////////////////////////////
    class FetchBooksDataFromInternet extends AsyncTask<URL,Void,JsonObject>{

        @Override
        protected JsonObject doInBackground(URL... urls) {
            URL url = urls[0];
            JsonObject jsonObjectRes = null;
            try {
                String jsonRes = getResponseFromHttpUrl(url);
                JsonParser parser = new JsonParser();
                jsonObjectRes = parser.parse(jsonRes).getAsJsonObject();
            }catch (Exception e){
                e.printStackTrace();
            }
            return jsonObjectRes;
        }

        private String getResponseFromHttpUrl(URL url) throws IOException{
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = con.getInputStream();
                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                boolean hasInput = scanner.hasNext();
                if (hasInput) {
                    return scanner.next();
                } else {
                    return null;
               }
            } finally {
                con.disconnect();
            }
        }

    }

}
