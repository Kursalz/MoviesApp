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
import com.d100.moviesappprova.adapter.MoviesAdapter;
import com.d100.moviesappprova.api.Client;
import com.d100.moviesappprova.api.Service;
import com.d100.moviesappprova.data.Provider;
import com.d100.moviesappprova.data.TableHelper;
import com.d100.moviesappprova.model.Movie;
import com.d100.moviesappprova.model.MoviesResponse;

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

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Fetching movies");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        mRecyclerView = findViewById(R.id.recycler_view);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        }

        Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, null, null, null, null);
        mAdapter = new MoviesAdapter(getApplicationContext(), cursor);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);
        loadJSON();

    }

    private void setViews() {
        mSwipeLayout = findViewById(R.id.swipe_layout);
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
            if (getString(R.string.api_key).isEmpty()) { //BuildConfig.THE_MOVIE_DB_API_TOKEN
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
                    ContentValues content = new ContentValues();
                    Movie movie;
                    for (int i = 0; i < movies.size(); i++) {
                        movie = movies.get(i);

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

                        resolver.insert(Provider.FILMS_URI, content);
                    }
                    mProgressDialog.dismiss();
                }

                @Override
                public void onFailure(Call<MoviesResponse> call, Throwable t) {
                    Log.d(TAG, "onFailure: " + t.getMessage());
                    Toast.makeText(MainActivity.this, "Server non raggiungibile", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "loadJSON: " + e.getMessage());
        }
        mProgressDialog.dismiss();
        loadDb();
    }

    private void loadDb() {
        Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, null, null, null, null);
        mRecyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), cursor));
        mRecyclerView.smoothScrollToPosition(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                mSwipeLayout.setEnabled(false);
                searchView.onActionViewExpanded();
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                hideKeyboard(MainActivity.this);
                searchView.onActionViewCollapsed();
                loadJSON();
                mSwipeLayout.setEnabled(true);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                final Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, TableHelper.TITLE + " LIKE \'%" + s + "%\'", null, null, null);
                mRecyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), cursor));
                mRecyclerView.smoothScrollToPosition(0);
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                final Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, TableHelper.TITLE + " LIKE \'%" + s + "%\'", null, null, null);
                mRecyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), cursor));
                mRecyclerView.smoothScrollToPosition(0);
                return false;

            }
        });
        return true;
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
