package com.example.android.popularmovies;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

import com.example.android.popularmovies.data.Contract;
import com.example.android.popularmovies.network.Movie;
import com.example.android.popularmovies.network.Trailer;
import com.example.android.popularmovies.network.Review;


public class DetailFragment extends Fragment implements TrailerAsyncTask.Listener,
        TrailerListAdapter.Callbacks, ReviewAsyncTask.Listener, ReviewListAdapter.Callbacks {

    @SuppressWarnings("unused")
    public static final String LOG_TAG = DetailFragment.class.getSimpleName();
    public static final String ARG_MOVIE = "ARG_MOVIE";
    private static final String EXTRA_TRAILERS = "EXTRA_TRAILERS";
    private static final String EXTRA_REVIEWS = "EXTRA_REVIEWS";

    private Movie mMovie;
    private TrailerListAdapter mTrailerListAdapter;
    private ReviewListAdapter mReviewListAdapter;
    private ShareActionProvider mShareActionProvider;




    @BindView(R.id.trailer_list) public RecyclerView mTrailerRecyclerView;
    @BindView(R.id.review_list) public RecyclerView mReviewRecyclerView;

    @BindView(R.id.movie_title) public TextView mMovieTitleView;
    @BindView(R.id.movie_overview) public TextView mMovieOverviewView;
    @BindView(R.id.movie_release_date) public TextView mMovieReleaseDateView;
    @BindView(R.id.movie_user_rating) public TextView mMovieRatingView;
    @BindView(R.id.movie_poster) public ImageView mMoviePosterView;


    @BindView(R.id.fab_mark_favorite) public FloatingActionButton mButtonMarkAsFavorite;
    @BindView(R.id.fab_remove_favorite) public FloatingActionButton mButtonRemoveFromFavorites;

    @BindViews({R.id.rating_first_star, R.id.rating_second_star, R.id.rating_third_star, R.id.rating_fourth_star, R.id.rating_fifth_star}) public List<ImageView> ratingStarViews;

    public DetailFragment() {
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Objects.requireNonNull(getArguments()).containsKey(ARG_MOVIE)) {
            mMovie = getArguments().getParcelable(ARG_MOVIE);
        }
        setHasOptionsMenu(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Activity activity = getActivity();
        CollapsingToolbarLayout appBarLayout = Objects.requireNonNull(activity).findViewById(R.id.toolbar_layout);
        if (appBarLayout != null && activity instanceof DetailActivity) {
            appBarLayout.setTitle(mMovie.getTitle());
        }

        ImageView movieBackdrop = activity.findViewById(R.id.movie_backdrop);
        if (movieBackdrop != null) {
            Picasso.with(activity)
                    .load(mMovie.getBackdropUrl(getContext()))
                    .config(Bitmap.Config.RGB_565)
                    .into(movieBackdrop);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.movie_detail_content, container, false);
        ButterKnife.bind(this, rootView);

        mMovieTitleView.setText(mMovie.getTitle());
        mMovieOverviewView.setText(mMovie.getOverview());
        mMovieReleaseDateView.setText(mMovie.getReleaseDate(getContext()));

        Picasso.with(getContext())
                .load(mMovie.getPosterUrl(getContext()))
                .config(Bitmap.Config.RGB_565)
                .into(mMoviePosterView);

        updateRatingBar();
        updateFavButtons();


        LinearLayoutManager layoutManager
                = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mTrailerRecyclerView.setLayoutManager(layoutManager);
        mTrailerListAdapter = new TrailerListAdapter(new ArrayList<Trailer>(), this);
        mTrailerRecyclerView.setAdapter(mTrailerListAdapter);
        mTrailerRecyclerView.setNestedScrollingEnabled(false);


        mReviewListAdapter = new ReviewListAdapter(new ArrayList<Review>(), this);
        mReviewRecyclerView.setAdapter(mReviewListAdapter);


        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_TRAILERS)) {
            List<Trailer> trailers = savedInstanceState.getParcelableArrayList(EXTRA_TRAILERS);
            mTrailerListAdapter.add(trailers);

        } else {
            fetchTrailers();
        }


        if (savedInstanceState != null && savedInstanceState.containsKey(EXTRA_REVIEWS)) {
            List<Review> reviews = savedInstanceState.getParcelableArrayList(EXTRA_REVIEWS);
            mReviewListAdapter.add(reviews);
        } else {
            fetchReviews();
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Trailer> trailers = mTrailerListAdapter.getTrailers();
        if (trailers != null && !trailers.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_TRAILERS, trailers);
        }

        ArrayList<Review> reviews = mReviewListAdapter.getReviews();
        if (reviews != null && !reviews.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_REVIEWS, reviews);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_share, menu);
        MenuItem shareTrailerMenuItem = menu.findItem(R.id.share_trailer);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareTrailerMenuItem);
    }

    @Override
    public void watch(Trailer trailer) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trailer.getTrailerUrl())));
    }

    @Override
    public void read(Review review) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(review.getUrl())));
    }

    @Override
    public void onTrailersFetchFinished(List<Trailer> trailers) {
        mTrailerListAdapter.add(trailers);


        if (mTrailerListAdapter.getItemCount() > 0) {
            Trailer trailer = mTrailerListAdapter.getTrailers().get(0);
            updateShareActionProvider(trailer);
        }
    }

    @Override
    public void onReviewsFetchFinished(List<Review> reviews) {
        mReviewListAdapter.add(reviews);
    }

    private void fetchTrailers() {
        TrailerAsyncTask task = new TrailerAsyncTask(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMovie.getId());
    }

    private void fetchReviews() {
        ReviewAsyncTask task = new ReviewAsyncTask(this);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mMovie.getId());
    }

    @SuppressLint("StaticFieldLeak")
    private class addFavoriteTask extends AsyncTask<Void, Void, Void> {

        //new AsyncTask<Void, Void, Void>() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(Void... params) {
            if (!isFavorite()) {
                ContentValues mV = new ContentValues();
                mV.put(Contract.MovieEntry.COLUMN_MOVIE_ID,
                        mMovie.getId());
                mV.put(Contract.MovieEntry.COLUMN_MOVIE_TITLE,
                        mMovie.getTitle());
                mV.put(Contract.MovieEntry.COLUMN_MOVIE_POSTER_PATH,
                        mMovie.getPoster());
                mV.put(Contract.MovieEntry.COLUMN_MOVIE_OVERVIEW,
                        mMovie.getOverview());
                mV.put(Contract.MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE,
                        mMovie.getUserRating());
                mV.put(Contract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                        mMovie.getReleaseDate());
                mV.put(Contract.MovieEntry.COLUMN_MOVIE_BACKDROP_PATH,
                        mMovie.getBackdrop());
                Objects.requireNonNull(getContext()).getContentResolver().insert(
                        Contract.MovieEntry.CONTENT_URI,
                        mV
                );
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateFavButtons();
        }
    }//.executeOnExecutor(addFavoriteTask.THREAD_POOL_EXECUTOR);



    @SuppressLint("StaticFieldLeak")
    private class  removeFavoriteTask extends AsyncTask<Void, Void, Void> {

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            protected Void doInBackground(Void... params) {
                if (isFavorite()) {
                    Objects.requireNonNull(getContext()).getContentResolver().delete(Contract.MovieEntry.CONTENT_URI,
                            Contract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(), null);

                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateFavButtons();
            }
        //.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }



    @SuppressLint("StaticFieldLeak")
    private void updateFavButtons() {

            new AsyncTask<Void, Void, Boolean>() {

                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                protected Boolean doInBackground(Void... params) {
                    return isFavorite();
                }

                @Override
                protected void onPostExecute(Boolean isFavorite) {
                    if (isFavorite) {
                        mButtonRemoveFromFavorites.setVisibility(View.VISIBLE);
                        mButtonMarkAsFavorite.setVisibility(View.GONE);
                    } else {
                        mButtonMarkAsFavorite.setVisibility(View.VISIBLE);
                        mButtonRemoveFromFavorites.setVisibility(View.GONE);
                    }
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mButtonMarkAsFavorite.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new addFavoriteTask().execute();

                        //Snackbar.make(v, "Added to Favorites", Snackbar.LENGTH_SHORT).setAction("Action", null).show(); -->does not work well with older devices
                        Toast.makeText(getActivity(), "Added to Favorites",
                                Toast.LENGTH_SHORT).show();

                    }
                });



        mButtonRemoveFromFavorites.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new removeFavoriteTask().execute();

                        //Snackbar.make(v, "Removed from Favorites", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                        Toast.makeText(getActivity(), "Removed from Favorites",
                                Toast.LENGTH_SHORT).show();

                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private boolean isFavorite() {
        Cursor movieCursor = Objects.requireNonNull(getContext()).getContentResolver().query(
                Contract.MovieEntry.CONTENT_URI,
                new String[]{Contract.MovieEntry.COLUMN_MOVIE_ID},
                Contract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovie.getId(),
                null,
                null);

        if (movieCursor != null && movieCursor.moveToFirst()) {
            movieCursor.close();
            return true;
        } else {
            return false;
        }
    }

    private void updateRatingBar() {
        if (mMovie.getUserRating() != null && !mMovie.getUserRating().isEmpty()) {
            String userRatingStr = getResources().getString(R.string.user_rating_movie,
                    mMovie.getUserRating());
            mMovieRatingView.setText(userRatingStr);

            float userRating = Float.valueOf(mMovie.getUserRating()) / 2;
            int integerPart = (int) userRating;

            // Fill stars
            for (int i = 0; i < integerPart; i++) {
                ratingStarViews.get(i).setImageResource(R.drawable.ic_star_black_24dp);
            }

            // Fill half star
            if (Math.round(userRating) > integerPart) {
                ratingStarViews.get(integerPart).setImageResource(
                        R.drawable.ic_star_half_black_24dp);
            }

        } else {
            mMovieRatingView.setVisibility(View.GONE);
        }
    }

    private void updateShareActionProvider(Trailer trailer) {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mMovie.getTitle());
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, trailer.getName() + ": "
                + trailer.getTrailerUrl());
        mShareActionProvider.setShareIntent(sharingIntent);
    }
}
