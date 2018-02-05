package com.example.harmoush.popularbooks;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.crash.FirebaseCrash;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DetailFragment extends Fragment {
    public DetailFragment() {
    }
    private String App_ID;
    private  Book mBook;
    @BindView(R.id.adView1) AdView mAdView;
    private View rootView;
    private Context context;
    @BindView(R.id.tv_author_name) TextView mAuthor;
    @BindView(R.id.tv_description) TextView mDescription;
    @BindView(R.id.tv_publish_date) TextView mDate;
    @BindView(R.id.btn_favourite) Button favouriteButton;
    @BindView(R.id.btn_share) Button shareButton;
    //private Button favouriteButton;
    //private Button shareButton;
    @BindView(R.id.tv_book_name) TextView mBookName;
    @BindView(R.id.iv_book_cover)
    ImageView mBookCover;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable  Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.content_detail, container, false);
        context = getActivity();
        App_ID = getString(R.string.AppIDads);
        MobileAds.initialize(getActivity() ,App_ID );
        ButterKnife.bind(this,rootView);
        if(savedInstanceState !=null)
            mBook = savedInstanceState.getParcelable(getString(R.string.bookdata));
        else {
            mBook = getActivity().getIntent().getParcelableExtra(getString(R.string.book));
            if (isNetworkAvailable()) {
                try {
                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdView.loadAd(adRequest);
                } catch (Exception e) {
                    FirebaseCrash.log(getString(R.string.errorAdd));
                    FirebaseCrash.report(e);
                    Toast.makeText(context, R.string.errorAds, Toast.LENGTH_SHORT).show();
                }
            }
        }
            if(mBook!= null) {
            if(!mBook.getBookAuthor().equals("")){
                String e = mBook.getBookAuthor();
                mAuthor.setText(e);
            }

            if(!mBook.getBookDescription().equals(""))
                mDescription.setText(mBook.getBookDescription());
            else
                mDescription.setText(R.string.no_description);
            if(!mBook.getBookTitle().equals(""))
                mBookName.setText(mBook.getBookTitle());
            if(!mBook.getBookPosterImage().equals(""))
                Glide.with(context).load(mBook.getBookPosterImage()).into(mBookCover);
            else
                Glide.with(context).load(R.drawable.bookdash_placeholder).into(mBookCover);
            if(!mBook.getBookPublishedDate().equals(""))
                mDate.setText(mBook.getBookPublishedDate());
            else
                mDate.setText(R.string.no_date);
        }
        ///////////////////////////////////////////////////////
        final Cursor cursor = getActivity().getContentResolver().query(Contract.BookTable.CONTENT_URI,null,null,null,null);
        boolean inDB = false;
        if(cursor != null ) {
            String BookTitle=getString(R.string.booktitle);
            for(int i =0;i<cursor.getCount();i++){
                int idx = cursor.getColumnIndex(getString(R.string.BookTitleColumn));
                if (cursor.moveToNext())
                    BookTitle = cursor.getString(idx);
                if(BookTitle.equals(mBook.getBookTitle())){
                    inDB = true;
                    break;
                }
            }
            if (inDB) {
                favouriteButton.setText(R.string.mark_unFavourite);
            }

        }
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, mBook.getBookTitle()+getString(R.string.bookAwwsome));
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent,getString(R.string.shareby)));
            }
        });
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String s = favouriteButton.getText().toString();
                if(s.equals(getString(R.string.mark__Favourite))) {
                    addDataBase();
                    favouriteButton.setText(R.string.mark_unFavourite);
                }

                else {
                    deleteDataBase();
                    favouriteButton.setText(R.string.mark_Favourite);
                }
            }
        });

        return rootView;
    }
    public void addDataBase() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contract.BookTable.COULUMN_BOOK_ID, mBook.getBookId());
        contentValues.put(Contract.BookTable.COULUMN_BOOK_AUTHOR, mBook.bookAuthor);
        contentValues.put(Contract.BookTable.COULUMN_BOOK_TITLE, mBook.bookTitle);
        contentValues.put(Contract.BookTable.COULUMN_BOOK_DESCRIPTION, mBook.bookDescription);
        contentValues.put(Contract.BookTable.COULUMN_BOOK_PUBLISHDATE, mBook.getBookPublishedDate());
        contentValues.put(Contract.BookTable.COULUMN_BOOK_POSTERIMAGE, mBook.getBookPosterImage());

        Uri uri = getActivity().getContentResolver().insert(Contract.BookTable.CONTENT_URI, contentValues);
    }
    public void deleteDataBase() {
        getActivity().getContentResolver().delete(Contract.BookTable.CONTENT_URI, mBook.getBookId(), null);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(getString(R.string.bookdata), mBook);
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState !=null)
            mBook  = savedInstanceState.getParcelable(getString(R.string.bookdata));
    }
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
