package com.d100.moviesappprova.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.d100.moviesappprova.R;
import com.d100.moviesappprova.adapter.MoviesAdapter.MyViewHolder;
import com.d100.moviesappprova.api.Client;
import com.d100.moviesappprova.api.Service;
import com.d100.moviesappprova.data.PreferitiTableHelper;
import com.d100.moviesappprova.data.Provider;
import com.d100.moviesappprova.data.TableHelper;
import com.d100.moviesappprova.model.Movie;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailActivity extends AppCompatActivity {
    TextView mTxtMovieName, mTxtSynopsis, mTxtReleaseDate;
    RatingBar mUserRating;
    ImageView mImageView;
    ImageButton mBtnFavourite;
    AppBarLayout mAppBarLayout;
    Toolbar mToolbar;
    private String TAG = "tagDetailActivity";
    Boolean isFavourite;
    int movieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        initCollapsingToolbar();

        mBtnFavourite = findViewById(R.id.btnFavourite);
        mImageView = findViewById(R.id.thumbnail_image_header);
        mTxtMovieName = findViewById(R.id.title);
        mTxtSynopsis = findViewById(R.id.plotsynopsis);
        mUserRating = findViewById(R.id.userrating);
        mTxtReleaseDate = findViewById(R.id.releasedate);


        Intent vStartingIntent = getIntent();
        if (vStartingIntent.hasExtra(MyViewHolder.ORIGINAL_TITLE)) {
            String thumbnail = vStartingIntent.getExtras().getString(MyViewHolder.BACKDROP_PATH);
            String movieName = vStartingIntent.getExtras().getString(MyViewHolder.ORIGINAL_TITLE);
            String synopsis = vStartingIntent.getExtras().getString(MyViewHolder.OVERVIEW);
            double rating = vStartingIntent.getExtras().getDouble(MyViewHolder.VOTE_AVERAGE);
            String releaseDate = vStartingIntent.getExtras().getString(MyViewHolder.RELEASE_DATE);
            movieId = vStartingIntent.getExtras().getInt(MyViewHolder._ID);
            Glide.with(this)
                    .load(thumbnail)
                    .placeholder(R.drawable.backdrop_default)
                    .into(mImageView);

            mTxtMovieName.setText(movieName);
            mTxtSynopsis.setText(synopsis);
            mUserRating.setMax(10);
            mUserRating.setEnabled(false);
            mUserRating.setRating((float)rating);
            mTxtReleaseDate.setText(releaseDate);

            Cursor cursor = getContentResolver().query(Uri.parse(Provider.PREFERITI_URI + "/" + movieId), null, null, null, null);
            if (cursor.getCount() > 0) {
                isFavourite = true;
                mBtnFavourite.setImageDrawable(getDrawable(R.drawable.star_true));
            } else {
                isFavourite = false;
                mBtnFavourite.setImageDrawable(getDrawable(R.drawable.star_false));
            }
        } else {
            Toast.makeText(this, "No API data", Toast.LENGTH_SHORT).show();
        }

        mBtnFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favourite(mBtnFavourite);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collToolLayout = findViewById(R.id.collapsingToolBar);
        collToolLayout.setTitle("");

        mAppBarLayout = findViewById(R.id.appbar);
        mAppBarLayout.setExpanded(true);

        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShown = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                if (scrollRange + verticalOffset <= 0) {
                    collToolLayout.setTitle(getString(R.string.movie_details));
                    isShown = true;

                } else if (isShown) {
                    collToolLayout.setTitle("");
                    isShown = false;
                }
            }
        });
    }

    public void favourite(ImageButton btn) {
        if (isFavourite) {
            getContentResolver().delete(Uri.parse(Provider.PREFERITI_URI + "/" + movieId), null, null);
            btn.setImageDrawable(getDrawable(R.drawable.star_false));
            isFavourite = false;
        } else {
            ContentValues content = new ContentValues();
            content.put(PreferitiTableHelper._ID, movieId);
            getContentResolver().insert(Provider.PREFERITI_URI, content);
            btn.setImageDrawable(getDrawable(R.drawable.star_true));
            isFavourite = true;
            getFilm(movieId);
        }
    }

    private void getFilm(int id) {
        try {
            if (getString(R.string.api_key).isEmpty()) { //BuildConfig.THE_MOVIE_DB_API_TOKEN
                Toast.makeText(this, "Please obtain api key", Toast.LENGTH_SHORT).show();
                return;
            }
            Call<Movie> call;
            final Service vApiService = Client.getClient().create(Service.class);
            call = vApiService.getMovieDetail(id, getString(R.string.api_key));
            call.enqueue(new Callback<Movie>() {
                @Override
                public void onResponse(Call<Movie> call, Response<Movie> response) {
                    if (response.body() != null) {
                        ContentResolver resolver = getContentResolver();
                        resolver.insert(Provider.FILMS_URI, createContentValues(response.body()));
                    }
                }

                @Override
                public void onFailure(Call<Movie> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "loadJSON: " + e.getMessage());
        }
    }

    private ContentValues createContentValues(Movie movie) {
        ContentValues content = new ContentValues();

        content.put(TableHelper._ID, movie.getId());
        content.put(TableHelper.POSTER_PATH, movie.getPoster_path());
        content.put(TableHelper.ADULT, movie.isAdult());
        content.put(TableHelper.OVERVIEW, movie.getOverview());
        content.put(TableHelper.RELEASE_DATE, movie.getRelease_date());
        content.put(TableHelper.ORIGINAL_TITLE, movie.getTitle());
        content.put(TableHelper.ORIGINAL_LANGUAGE, movie.getOriginal_language());
        content.put(TableHelper.TITLE, movie.getTitle());
        content.put(TableHelper.BACKDROP_PATH, movie.getBackdrop_path());
        content.put(TableHelper.POPULARITY, movie.getPopularity());
        content.put(TableHelper.VOTE_COUNT, movie.getVote_count());
        content.put(TableHelper.VIDEO, movie.isVideo());
        content.put(TableHelper.VOTE_AVERAGE, movie.getVote_average());

        return content;
    }
}
