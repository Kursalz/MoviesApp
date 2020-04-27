package com.d100.moviesappprova.activity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.d100.moviesappprova.R;
import com.d100.moviesappprova.adapter.MoviesAdapter;
import com.d100.moviesappprova.api.Client;
import com.d100.moviesappprova.api.Service;
import com.d100.moviesappprova.data.Provider;
import com.d100.moviesappprova.data.TableHelper;
import com.d100.moviesappprova.model.Movie;
import com.d100.moviesappprova.model.MoviesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "tagMainActivity";

    private RecyclerView mRecyclerView;
    private MoviesAdapter mAdapter;
    private List<Movie> mListMovies;
    ProgressDialog mProgressDialog;
    private SwipeRefreshLayout mSwipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setViews();
        setListeners();
        initViews();
    }

    private void setViews() {
        mSwipeLayout = findViewById(R.id.swipe_layout);
    }

    private void setListeners() {
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this, "Movies refreshed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Fetching movies");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        mRecyclerView = findViewById(R.id.recycler_view);

        mListMovies = new ArrayList<>();
        mAdapter = new MoviesAdapter(this, mListMovies);

        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        loadJSON();
    }

    private void loadJSON() {
        try {
            if(getString(R.string.api_key).isEmpty()) { //BuildConfig.THE_MOVIE_DB_API_TOKEN
                Toast.makeText(this, "Please obtain api key", Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
                return;
            }

            Client vClient = new Client();
            Service vApiService = Client.getClient().create(Service.class);
            Call<MoviesResponse> call = vApiService.getPopularMovies(getString(R.string.api_key));
            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    ContentResolver resolver = getContentResolver();
                    ContentValues content = new ContentValues(13);
                    Movie movie;
                    for(int i = 0; i < movies.size(); i++){
                        movie = movies.get(i);
                        content.put(TableHelper.ID, movie.getId());
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

                        resolver.insert(Provider.FILMS_URI, content);
                    }
                    //mRecyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), movies));
                    //mRecyclerView.smoothScrollToPosition(0);
                    //if(mSwipeLayout.isRefreshing()) {
                    //    mSwipeLayout.setRefreshing(false);
                    //}
                    mProgressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getMessage());
                    Toast.makeText(MainActivity.this, "Error occurred while fetching data", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "loadJSON: " + e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
