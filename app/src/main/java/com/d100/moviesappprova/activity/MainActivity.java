package com.d100.moviesappprova.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.d100.moviesappprova.R;
import com.d100.moviesappprova.adapter.ApiAdapter;
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
    ProgressDialog mProgressDialog;
    private SwipeRefreshLayout mSwipeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setViews();
        setListeners();
        setProgressDialog();

        // set number of movies shown in a row based on screen orientation
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        // get movies from internal database and load into recyclerview
        Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, null, null, null, null);
        mAdapter = new MoviesAdapter(getApplicationContext(), cursor);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // load movies from api
        loadJSON();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                searchView.onActionViewCollapsed();
                mSwipeLayout.setEnabled(false);
                searchView.onActionViewExpanded();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                hideKeyboard(MainActivity.this);
                mSwipeLayout.setEnabled(true);
                loadJSON();
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                search(s);
                searchView.clearFocus();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                search(s);
                return false;
            }
        });
        return true;
    }

    private void search(String s) {
        final String ms = s;
        try {
            checkApiKey();

            Service vApiService = Client.getClient().create(Service.class);
            Call<MoviesResponse> call = vApiService.getMoviesByTitle(getString(R.string.api_key), s);

            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies;

                    if(response.body() == null) {
                        movies = new ArrayList<>();
                    } else {
                        movies = response.body().getResults();
                    }
                    mRecyclerView.setAdapter(new ApiAdapter(getApplicationContext(), movies));
                    mRecyclerView.smoothScrollToPosition(0);
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d(TAG, "onFailure search: " + t.getMessage());
                    Toast.makeText(MainActivity.this, "Server non raggiungibile", Toast.LENGTH_SHORT).show();
                    final Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, TableHelper.TITLE + " LIKE \'%" + ms + "%\'", null, null, null);
                    mRecyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), cursor));
                    mRecyclerView.smoothScrollToPosition(0);
                }
            });

        } catch (Exception e) {
            Log.d(TAG, "onQueryTextSubmit: exception: " + e.getMessage());
        }

    }

    private void setProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Fetching movies");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void setViews() {
        mSwipeLayout = findViewById(R.id.swipe_layout);
        mRecyclerView = findViewById(R.id.recycler_view);
    }

    private void setListeners() {
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this, "Movies refreshed", Toast.LENGTH_SHORT).show();
                loadJSON();
                mSwipeLayout.setRefreshing(false);
            }
        });
    }

    private void loadJSON() {
        try {
            checkApiKey();
            final Service vApiService = Client.getClient().create(Service.class);
            Call<MoviesResponse> call = vApiService.getPopularMovies(getString(R.string.api_key));

            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    ContentResolver resolver = getContentResolver();
                    ContentValues content = new ContentValues();
                    Movie movie;
                    for (int i = 0; i < movies.size(); i++) {
                        movie = movies.get(i);
                        content = createContentValues(movie);
                        resolver.insert(Provider.FILMS_URI, content);
                    }
                    mProgressDialog.dismiss();
                    mRecyclerView.setAdapter(new ApiAdapter(getApplicationContext(),movies));
                    mRecyclerView.smoothScrollToPosition(0);
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getMessage());
                    Toast.makeText(MainActivity.this, "Server non raggiungibile", Toast.LENGTH_SHORT).show();
                    loadDb();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "loadJSON: " + e.getMessage());
        }
        mProgressDialog.dismiss();
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

    private void checkApiKey() {
        if (getString(R.string.api_key).isEmpty()) { //BuildConfig.THE_MOVIE_DB_API_TOKEN
            Toast.makeText(this, "Please obtain api key", Toast.LENGTH_SHORT).show();
            mProgressDialog.dismiss();
            return;
        }
    }

    private void loadDb() {
        Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, null, null, null, null);
        mRecyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), cursor));
        mRecyclerView.smoothScrollToPosition(0);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
