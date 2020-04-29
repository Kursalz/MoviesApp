package com.d100.moviesappprova.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.d100.moviesappprova.R;
import com.d100.moviesappprova.activity.DetailActivity;
import com.d100.moviesappprova.model.Movie;

import java.util.List;

public class ApiAdapter extends RecyclerView.Adapter<ApiAdapter.MyViewHolder> {
    private Context mContext;
    private List<Movie> mListMovies;

    public ApiAdapter(Context aContext, List<Movie> aListMovies) {
        mContext = aContext;
        mListMovies = aListMovies;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_movie, parent, false);
        return new MyViewHolder(vView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(mContext)
            .load(mListMovies.get(position).getPoster_path())
            .placeholder(R.drawable.poster_default)//load è una gif del loading
            .into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return mListMovies.size();
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
                    int vPosition = getAdapterPosition();
                    Movie vMovie = mListMovies.get(vPosition);

                    if (vPosition != RecyclerView.NO_POSITION) {
                        Intent vIntent = new Intent(mContext, DetailActivity.class);

                        vIntent.putExtra(ORIGINAL_TITLE, vMovie.getOriginal_title());
                        vIntent.putExtra(BACKDROP_PATH, vMovie.getBackdrop_path());
                        vIntent.putExtra(OVERVIEW, vMovie.getOverview());
                        vIntent.putExtra(VOTE_AVERAGE, vMovie.getVote_average());
                        vIntent.putExtra(RELEASE_DATE, vMovie.getRelease_date());

                        vIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(vIntent);
                    }
                }
            });
        }
    }
}
