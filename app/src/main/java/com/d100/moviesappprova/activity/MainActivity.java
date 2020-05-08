package com.d100.moviesappprova.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.d100.moviesappprova.R;
import com.d100.moviesappprova.adapter.ApiAdapter;
import com.d100.moviesappprova.adapter.MoviesAdapter;
import com.d100.moviesappprova.api.Client;
import com.d100.moviesappprova.api.Service;
import com.d100.moviesappprova.data.PreferitiTableHelper;
import com.d100.moviesappprova.data.Provider;
import com.d100.moviesappprova.data.TableHelper;
import com.d100.moviesappprova.fragment.MyDialogFragment;
import com.d100.moviesappprova.model.Movie;
import com.d100.moviesappprova.model.MoviesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements MyDialogFragment.DialogFragmentInterface {
    public enum Status {
        TOP_RATED,
        POPULAR,
        SEARCH,
        FAVOURITES,
        FAVOURITES_ROTATING
    }

    public static final String TAG = "tagMainActivity";
    public static final String STATUS = "status";
    private static final String SEARCH_TEXT = "search_text";

    private RecyclerView mRecyclerView;
    private ApiAdapter mApiAdapter;
    ProgressDialog mProgressDialog;
    private SwipeRefreshLayout mSwipeLayout;
    private GridLayoutManager mLayoutManager;

    private String mSearchString = "";
    private Toolbar mToolbar;
    private int mPreviousTotal = 0, mVisibleThreshold = 5;
    private int mFirstVisibleItem, mVisibleItemCount, mTotalItemCount, mCurrentPage;
    private boolean mLoading = true;
    private Status mStatus = Status.POPULAR;

    /******* OVERRIDES *******/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        setViews();
        setListeners();
        setProgressDialog();
        setRecyclerView();

        if (savedInstanceState != null) {
            mStatus = (Status) savedInstanceState.getSerializable(STATUS);
            if (mStatus == Status.SEARCH) {
                mSearchString = savedInstanceState.getString(SEARCH_TEXT);
            }
        }

        loadFilmList(mSearchString, 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_filter:
                switch (mStatus) {
                    case FAVOURITES:
                        Toast.makeText(this, "Popular", Toast.LENGTH_SHORT).show();
                        mPreviousTotal = 0;
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                        getSupportActionBar().setTitle("Movies");
                        Window window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                        mStatus = Status.POPULAR;
                        loadFilmList("", 1);
                        mSwipeLayout.setEnabled(true);
                        break;

                    default:
                        Toast.makeText(this, "Preferiti", Toast.LENGTH_SHORT).show();
                        mPreviousTotal = 0;
                        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.favourite)));
                        getSupportActionBar().setTitle("Preferiti");
                        window = getWindow();
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                        window.setStatusBarColor(getResources().getColor(R.color.favouriteDark));
                        mStatus = Status.FAVOURITES;
                        loadFavourite();
                        mSwipeLayout.setEnabled(false);
                        break;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final MenuItem filterItem = menu.findItem(R.id.action_filter);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                Log.d(TAG, "onMenuItemActionExpand: ");
                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
                getSupportActionBar().setTitle("Movies");
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                String text = mSearchString;
                filterItem.setEnabled(false);
                searchView.onActionViewCollapsed();
                mSwipeLayout.setEnabled(false);
                searchView.onActionViewExpanded();

                mSearchString = text;
                mStatus = Status.SEARCH;
                mPreviousTotal = 0;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                Log.d(TAG, "onMenuItemActionCollapse: ");
                hideKeyboard(MainActivity.this);
                mSwipeLayout.setEnabled(true);
                mStatus = Status.POPULAR;
                filterItem.setEnabled(true);
                mPreviousTotal = 0;
                loadFilmList(mSearchString, 1);
                return true;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                mSearchString = s;
                loadFilmList(s, 1);
                searchView.clearFocus();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                mSearchString = s;
                loadFilmList(s, 1);

                return false;
            }
        });

        switch (mStatus) {
            case SEARCH:
                searchItem.expandActionView();
                searchView.post(new Runnable() {
                    @Override
                    public void run() {
                        searchView.setQuery(mSearchString, true);
                    }
                });
                break;

            case FAVOURITES:
                mStatus = Status.FAVOURITES_ROTATING;
                menu.performIdentifierAction(R.id.action_filter, 0);
                break;
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
        outState.putSerializable(STATUS, mStatus);
        outState.putString(SEARCH_TEXT, mSearchString);
    }

    /******* OVERRIDE INTERFACE *******/

    @Override
    public void onResponse(boolean aResponse, long aId, boolean aIsDelete) {
        if (aResponse) {
            if (aIsDelete) {
                int vDeletedRows = getContentResolver().delete(Uri.parse(Provider.PREFERITI_URI + "/" + aId), null, null);
                if (vDeletedRows > 0) {
                    Toast.makeText(this, "Film rimosso dalla lista preferiti", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Errore rimozione film", Toast.LENGTH_SHORT).show();
                }
            } else {
                ContentValues content = new ContentValues();
                content.put(PreferitiTableHelper._ID, aId);
                getContentResolver().insert(Provider.PREFERITI_URI, content);
                Toast.makeText(this, "Film inserito nella lista preferiti", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /******* VIEW INITS *******/

    private void setProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Fetching movies");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
    }

    private void setViews() {
        mSwipeLayout = findViewById(R.id.swipe_layout);
        mRecyclerView = findViewById(R.id.recycler_view);
        mToolbar = findViewById(R.id.toolbar);
    }

    private void setListeners() {
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(MainActivity.this, "Movies refreshed", Toast.LENGTH_SHORT).show();
                loadFilmList("", 1);
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
                    hideKeyboard(MainActivity.this);
                    if (mLoading) {
                        if (mTotalItemCount > mPreviousTotal) {
                            mLoading = false;
                            mPreviousTotal = mTotalItemCount;
                        }
                    }

                    if (!mLoading && (mTotalItemCount - mVisibleItemCount) <= (mFirstVisibleItem + mVisibleThreshold)) {
                        switch (mStatus) {
                            case SEARCH:
                                loadFilmList(mSearchString, mCurrentPage + 1);
                                break;

                            case POPULAR:
                                loadFilmList("", mCurrentPage + 1);
                                break;

                            //case POPULAR:
                        }

                        mLoading = true;
                        Log.d(TAG, "onScrolled: end of scroll");
                    }
                }
            }
        });
    }


    /******* DATA LOADING *******/

    private void loadFilmList(String s, final int page) {
        final String myString = s;

        try {
            checkApiKey();

            Call<MoviesResponse> call;
            final Service vApiService = Client.getClient().create(Service.class);

            switch (mStatus) {
                case SEARCH:
                    call = vApiService.getMoviesByTitle(getString(R.string.api_key), s, page);
                    break;

                case POPULAR:
                    call = vApiService.getPopularMovies(getString(R.string.api_key), page);
                    break;

                case TOP_RATED:
                    call = vApiService.getTopRatedMovies(getString(R.string.api_key), page);

                default:
                    call = null;
            }

            if (call != null) {
                call.enqueue(new Callback<MoviesResponse>() {
                    @Override
                    public void onResponse(Call<MoviesResponse> call, Response<MoviesResponse> response) {
                        List<Movie> movies;
                        if (response.body() == null) {
                            movies = new ArrayList<>();
                        } else {
                            movies = response.body().getResults();
                        }
                        ContentResolver resolver = getContentResolver();

                        for (int i = 0; i < movies.size() && page == 1; i++) {
                            resolver.insert(Provider.FILMS_URI, createContentValues(movies.get(i)));
                        }
                        mProgressDialog.dismiss();

                        if (page > 1) {
                            mApiAdapter.addMovies(movies);
                        } else {
                            mPreviousTotal = 0;
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

                        if (mStatus == Status.SEARCH) {
                            final Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, TableHelper.TITLE + " LIKE \'%" + myString + "%\'", null, null, null);
                            mRecyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), cursor));
                            mRecyclerView.smoothScrollToPosition(0);

                        } else {
                            if (mPreviousTotal == 0) {
                                loadDb();
                            }
                        }
                    }
                });
            }

        } catch (Exception e) {
            Log.d(TAG, "loadJSON: " + e.getMessage());
        }
        mProgressDialog.dismiss();
    }

    private void loadDb() {
        Cursor cursor = getContentResolver().query(Provider.FILMS_URI, null, null, null, null, null);
        mRecyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), cursor));
        mRecyclerView.smoothScrollToPosition(0);
    }

    private void loadFavourite() {
        Cursor cursorPreferiti = getContentResolver().query(Provider.PREFERITI_URI, null, null, null, null, null);
        String idList = "";
        for (int i = 0; i < cursorPreferiti.getCount(); i++) {
            cursorPreferiti.moveToNext();
            idList += cursorPreferiti.getInt(cursorPreferiti.getColumnIndex(PreferitiTableHelper._ID)) + ", ";
        }
        if (idList.length() > 2) {
            idList = idList.substring(0, idList.length() - 2);
        }
        Cursor cursorResult = getContentResolver().query(Provider.FILMS_URI, null, TableHelper._ID + " IN(" + idList + ")", null, null, null);
        mRecyclerView.setAdapter(new MoviesAdapter(getApplicationContext(), cursorResult));
        mRecyclerView.smoothScrollToPosition(0);
    }


    /******* MISC *******/

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
        if (getString(R.string.api_key).isEmpty()) {
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
