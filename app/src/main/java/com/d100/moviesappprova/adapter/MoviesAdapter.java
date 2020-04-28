package com.d100.moviesappprova.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.d100.moviesappprova.R;
import com.d100.moviesappprova.activity.DetailActivity;
import com.d100.moviesappprova.data.TableHelper;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {
    private Context mContext;
    private Cursor mCursor;

    public MoviesAdapter(Context aContext, Cursor cursor) {
        this.mContext = aContext;
        this.mCursor = cursor;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_movie, parent, false);
        return new MyViewHolder(vView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        String poster_path = mCursor.getString(mCursor.getColumnIndexOrThrow(TableHelper.POSTER_PATH));
        Glide.with(mContext)
                .load(poster_path)
                .placeholder(R.drawable.poster_default)//load è una gif del loading
                .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public static final String ORIGINAL_TITLE = "original_title";
        public static final String BACKDROP_PATH = "backdrop_path";
        public static final String OVERVIEW = "overview";
        public static final String VOTE_AVERAGE = "vote_average";
        public static final String RELEASE_DATE = "release_date";
        public ImageView thumbnail;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    mCursor.moveToPosition(position);
                    if (position != RecyclerView.NO_POSITION) {
                        Intent vIntent = new Intent(mContext, DetailActivity.class);
                        vIntent.putExtra(ORIGINAL_TITLE, mCursor.getString(mCursor.getColumnIndex(TableHelper.ORIGINAL_TITLE)));
                        vIntent.putExtra(BACKDROP_PATH, mCursor.getString(mCursor.getColumnIndex(TableHelper.BACKDROP_PATH)));
                        vIntent.putExtra(OVERVIEW, mCursor.getString(mCursor.getColumnIndex(TableHelper.OVERVIEW)));
                        vIntent.putExtra(VOTE_AVERAGE, mCursor.getString(mCursor.getColumnIndex(TableHelper.VOTE_AVERAGE)));
                        vIntent.putExtra(RELEASE_DATE, mCursor.getString(mCursor.getColumnIndex(TableHelper.RELEASE_DATE)));
                        vIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(vIntent);
                    }
                }
            });
        }
    }
}
