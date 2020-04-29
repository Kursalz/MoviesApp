package com.d100.moviesappprova.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.d100.moviesappprova.R;
import com.d100.moviesappprova.adapter.MoviesAdapter.MyViewHolder;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class DetailActivity extends AppCompatActivity {
    TextView mTxtMovieName, mTxtSynopsis, mTxtUserRating, mTxtReleaseDate;
    ImageView mImageView;
    AppBarLayout mAppBarLayout;
    private String TAG = "tagDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        initCollapsingToolbar();

        mImageView = findViewById(R.id.thumbnail_image_header);
        mTxtMovieName = findViewById(R.id.title);
        mTxtSynopsis = findViewById(R.id.plotsynopsis);
        mTxtUserRating = findViewById(R.id.userrating);
        mTxtReleaseDate = findViewById(R.id.releasedate);

        Intent vStartingIntent = getIntent();
        if(vStartingIntent.hasExtra(MyViewHolder.ORIGINAL_TITLE)) {
            String thumbnail = vStartingIntent.getExtras().getString(MyViewHolder.BACKDROP_PATH);
            String movieName = vStartingIntent.getExtras().getString(MyViewHolder.ORIGINAL_TITLE);
            String synopsis = vStartingIntent.getExtras().getString(MyViewHolder.OVERVIEW);
            String rating = vStartingIntent.getExtras().getString(MyViewHolder.VOTE_AVERAGE);
            String releaseDate = vStartingIntent.getExtras().getString(MyViewHolder.RELEASE_DATE);

            Glide.with(this)
                    .load(thumbnail)
                    .placeholder(R.drawable.backdrop_default)
                    .into(mImageView);

            mTxtMovieName.setText(movieName);
            mTxtSynopsis.setText(synopsis);
            mTxtUserRating.setText(rating);
            mTxtReleaseDate.setText(releaseDate);
        } else {
            Toast.makeText(this, "No API data", Toast.LENGTH_SHORT).show();
        }
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
                if(scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }

                if(scrollRange + verticalOffset <= 0) {
                    collToolLayout.setTitle(getString(R.string.movie_details));
                    isShown = true;

                } else if(isShown) {
                    collToolLayout.setTitle("");
                    isShown = false;
                }
            }
        });
    }
}
