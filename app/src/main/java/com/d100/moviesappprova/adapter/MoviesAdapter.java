package com.d100.moviesappprova.adapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.d100.moviesappprova.R;
import com.d100.moviesappprova.activity.DetailActivity;
import com.d100.moviesappprova.data.TableHelper;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {
    private Context mContext;
    private CursorAdapter mCursorAdapter;
    private MyViewHolder holder;

    public MoviesAdapter(Context context, Cursor cursor) {
        this.mContext = context;
        mCursorAdapter = new CursorAdapter(mContext, cursor, 0) {

            @Override
            public View newView(Context context, Cursor cursor, ViewGroup parent) {
                // Inflate the view here
                View v = LayoutInflater.from(context).inflate(R.layout.card_movie, parent, false);
                return v;
            }

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Binding operations
                String poster_path = cursor.getString(cursor.getColumnIndexOrThrow(TableHelper.POSTER_PATH));
                Glide.with(mContext)
                        .load(poster_path)
                        .placeholder(R.drawable.load)//load è una gif del loading
                        .into(holder.thumbnail);
            }
        };
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vView = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        holder = new MyViewHolder(vView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();}

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public static final String ORIGINAL_TITLE = "original_title";
        public static final String BACKDROP_PATH = "backdrop_path";
        public static final String OVERVIEW = "overview";
        public static final String VOTE_AVERAGE = "vote_average";
        public static final String RELEASE_DATE = "release_date";
        public TextView title, userRating;
        public ImageView thumbnail;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Cursor cursor = mCursorAdapter.getCursor();
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Intent vIntent = new Intent(mContext, DetailActivity.class);
                        vIntent.putExtra(ORIGINAL_TITLE, cursor.getString(cursor.getColumnIndex(TableHelper.ORIGINAL_TITLE)));
                        vIntent.putExtra(BACKDROP_PATH, cursor.getString(cursor.getColumnIndex(TableHelper.BACKDROP_PATH)));
                        vIntent.putExtra(OVERVIEW, cursor.getString(cursor.getColumnIndex(TableHelper.OVERVIEW)));
                        vIntent.putExtra(VOTE_AVERAGE, cursor.getDouble(cursor.getColumnIndex(TableHelper.VOTE_AVERAGE)));
                        vIntent.putExtra(RELEASE_DATE, cursor.getString(cursor.getColumnIndex(TableHelper.RELEASE_DATE)));
                        vIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(vIntent);
                    }
                }
            });
        }
    }

    public CursorAdapter getmCursorAdapter() {
        return mCursorAdapter;
    }
}
