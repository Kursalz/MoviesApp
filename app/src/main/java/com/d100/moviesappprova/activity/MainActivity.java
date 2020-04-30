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

import androidx.annotation.NonNull;
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
    private ApiAdapter mApiAdapter;
    ProgressDialog mProgressDialog;
    private SwipeRefreshLayout mSwipeLayout;
    private GridLayoutManager mLayoutManager;

    private List<Movie> mListMovies;

    private String mSearchString;
    private int mPreviousTotal = 0, mVisibleThreshold = 5;
    int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount, mCurrentPage;
    private boolean mLoading = true, mSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setViews();
        setListeners();
        setProgressDialog();
        setRecyclerView();

        // get movies from internal database and load into recyclerview
        Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, null, null, null, null);
        mAdapter = new MoviesAdapter(getApplicationContext(), cursor);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        // load movies from api
        mApiAdapter = new ApiAdapter(getApplicationContext(), new ArrayList<Movie>());
        loadJSON(1);
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
                mSearchMode = true;
                mPreviousTotal = 0;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                hideKeyboard(MainActivity.this);
                mSwipeLayout.setEnabled(true);
                mSearchMode = false;
                mPreviousTotal = 0;
                loadJSON(1);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mSearchString = s;
                search(s, 1);
                searchView.clearFocus();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mSearchString = s;
                search(s, 1);

                return false;
            }
        });
        return true;
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
                loadJSON(1);
                mSwipeLayout.setRefreshing(false);
            }
        });
    }

    private void setRecyclerView() {
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager = new GridLayoutManager(this, 2);
        } else {
            mLayoutManager = new GridLayoutManager(this, 4);
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                mVisibleItemCount = mRecyclerView.getChildCount();
                mTotalItemCount = mLayoutManager.getItemCount();
                mFirstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (dy > 0) {
                    Log.d(TAG, "onScrolled: totalItemCount: " + mTotalItemCount);
                    Log.d(TAG, "onScrolled: previousTotal: " + mPreviousTotal);
                    Log.d(TAG, "onScrolled: mloading: " + mLoading);

                    if (mLoading) {
                        if (mTotalItemCount > mPreviousTotal) {
                            mLoading = false;
                            mPreviousTotal = mTotalItemCount;
                        }
                    }

                    if (!mLoading && (mTotalItemCount - mVisibleItemCount) <= (mFirstVisibleItem + mVisibleThreshold)) {
                        if (!mSearchMode) {
                            loadJSON(mCurrentPage + 1);
                        } else {
                            search(mSearchString, mCurrentPage + 1);
                        }
                        mLoading = true;
                        Log.d(TAG, "onScrolled: end of scroll");
                    }
                }
            }
        });
    }

    private void loadJSON(final int page) {
        try {
            checkApiKey();

            final Service vApiService = Client.getClient().create(Service.class);
            Call<MoviesResponse> call = vApiService.getPopularMovies(getString(R.string.api_key), page);

            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies = response.body().getResults();
                    ContentResolver resolver = getContentResolver();

                    for (int i = 0; i < movies.size(); i++) {
                        resolver.insert(Provider.FILMS_URI, createContentValues(movies.get(i)));
                    }
                    mProgressDialog.dismiss();

                    if(page > 1) {
                        mApiAdapter.addMovies(movies);
                    } else {
                        mApiAdapter = new ApiAdapter(getApplicationContext(), movies);
                        mRecyclerView.setAdapter(mApiAdapter);
                        mRecyclerView.smoothScrollToPosition(0);
                    }

                    mCurrentPage = page;
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

    private void search(String s, final int page) {
        final String ms = s;
        try {
            checkApiKey();

            Service vApiService = Client.getClient().create(Service.class);
            Call<MoviesResponse> call = vApiService.getMoviesByTitle(getString(R.string.api_key), s, page);

            call.enqueue(new Callback<MoviesResponse>() {
                @Override
                public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                    List<Movie> movies;

                    if(response.body() == null) {
                        movies = new ArrayList<>();
                    } else {
                        movies = response.body().getResults();
                    }

                    if(page > 1) {
                        mApiAdapter.addMovies(movies);
                    } else {
                        mApiAdapter = new ApiAdapter(getApplicationContext(), movies);
                        mRecyclerView.setAdapter(mApiAdapter);
                        mRecyclerView.smoothScrollToPosition(0);
                    }

                    mCurrentPage = page;
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

    private void loadDb() {
        Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, null, null, null, null);
        mRecyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), cursor));
        mRecyclerView.smoothScrollToPosition(0);
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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
