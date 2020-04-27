package com.d100.moviesappprova.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.d100.moviesappprova.R;
import com.d100.moviesappprova.activity.DetailActivity;
import com.d100.moviesappprova.model.Movie;

import java.util.List;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {
    private Context mContext;
    private List<Movie> mListMovies;

    public MoviesAdapter(Context aContext, List<Movie> aListMovies) {
        this.mContext = aContext;
        this.mListMovies = aListMovies;
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
                .placeholder(R.drawable.load)//load è una gif del loading
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
        public TextView title, userRating;
        public ImageView thumbnail;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Movie clickedDataItem = mListMovies.get(position);
                        Intent vIntent = new Intent(mContext, DetailActivity.class);
                        vIntent.putExtra(ORIGINAL_TITLE, mListMovies.get(position).getOriginal_title());
                        vIntent.putExtra(BACKDROP_PATH, mListMovies.get(position).getBackdrop_path());
                        vIntent.putExtra(OVERVIEW, mListMovies.get(position).getOverview());
                        vIntent.putExtra(VOTE_AVERAGE, Double.toString(mListMovies.get(position).getVote_average()));
                        vIntent.putExtra(RELEASE_DATE, mListMovies.get(position).getRelease_date());
                        vIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(vIntent);
                        Toast.makeText(view.getContext(), "You clicked " + clickedDataItem.getOriginal_title(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
