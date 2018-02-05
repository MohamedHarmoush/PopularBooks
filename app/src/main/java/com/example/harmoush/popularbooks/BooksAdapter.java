package com.example.harmoush.popularbooks;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Harmoush on 1/31/2018.
 */

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BooksViewHolder> {

    private Context context;
    private ArrayList<Book> mBooks;
    final private ListItemClickListener mOnClickListener;

    public BooksAdapter(ArrayList<Book> mBooks, ListItemClickListener mOnClickListener) {
        this.mBooks = mBooks;
        this.mOnClickListener = mOnClickListener;
    }

    public interface ListItemClickListener {
        void onListItemClickListener(int clikedItemIndex);
    }
    @Override
    public BooksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list, parent, false);
        BooksViewHolder holder = new BooksViewHolder(view);
        return holder;

    }

    @Override
    public void onBindViewHolder(BooksViewHolder holder, int position) {
        final Book book = mBooks.get(position);
        Glide.with(context)
                .load(book.getBookPosterImage())
                .placeholder(R.drawable.bookdash_placeholder).error(R.drawable.bookdash_placeholder)
                .into(holder.bookCover);
        holder.bookTitle.setText(book.getBookTitle());
    }
    @Override
    public int getItemCount() {
        return (mBooks != null) ? mBooks.size(): 0;
    }

    public class BooksViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.iv_book_cover) ImageView bookCover;
        @BindView(R.id.tv_book_name) TextView bookTitle;
        public BooksViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            itemView.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClickListener(clickedPosition);
        }
    }
}
