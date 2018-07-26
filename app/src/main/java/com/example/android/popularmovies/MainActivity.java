package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.example.android.popularmovies.data.Contract;
import com.example.android.popularmovies.network.Movie;


import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        MovieAsyncTask.Listener, MovieListAdapter.Callbacks {

    private static final String EXTRA_MOVIES = "EXTRA_MOVIES";
    private static final String EXTRA_SORT_BY = "EXTRA_SORT_BY";
    private static final int FAVORITE_MOVIES_LOADER = 0;
    private boolean mBigScreen;//for later
    private MainFragment mMainFragment;
    private MovieListAdapter mAdapter;
    private String mSortBy = MovieAsyncTask.MOST_POPULAR;

    @BindView(R.id.movie_list)
    public RecyclerView mRecyclerView;
    @BindView(R.id.toolbar)
    public
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);



        mToolbar.setTitle(R.string.title_movie_list);
        setSupportActionBar(mToolbar);

        String tag = MainFragment.class.getName();
        this.mMainFragment = (MainFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (this.mMainFragment == null) {
            this.mMainFragment = new MainFragment();
            getSupportFragmentManager().beginTransaction().add(this.mMainFragment, tag).commit();
        }

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, getResources().getInteger(R.integer.grid_number_cols)));
        mAdapter = new MovieListAdapter(new ArrayList<Movie>(), this);
        mRecyclerView.setAdapter(mAdapter);
        mBigScreen = findViewById(R.id.movie_detail_container) != null;

        if (savedInstanceState != null) {
            mSortBy = savedInstanceState.getString(EXTRA_SORT_BY);
            if (savedInstanceState.containsKey(EXTRA_MOVIES)) {
                List<Movie> movies = savedInstanceState.getParcelableArrayList(EXTRA_MOVIES);
                mAdapter.add(movies);
                findViewById(R.id.progress).setVisibility(View.GONE);


                if (mSortBy.equals(MovieAsyncTask.FAVORITES)) {
                    getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER, null, this);
                }
            }
            updateEmptyState();
        } else {
            fetchMovies(mSortBy);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Movie> movies = mAdapter.getMovies();
        if (movies != null && !movies.isEmpty()) {
            outState.putParcelableArrayList(EXTRA_MOVIES, movies);
        }
        outState.putString(EXTRA_SORT_BY, mSortBy);

        if (!mSortBy.equals(MovieAsyncTask.FAVORITES)) {
            getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_sort, menu);

        switch (mSortBy) {
            case MovieAsyncTask.MOST_POPULAR:
                menu.findItem(R.id.sort_by_most_popular).setChecked(true);
                break;
            case MovieAsyncTask.TOP_RATED:
                menu.findItem(R.id.sort_by_top_rated).setChecked(true);
                break;
            case MovieAsyncTask.FAVORITES:
                menu.findItem(R.id.sort_by_favorites).setChecked(true);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sort_by_top_rated:
                if (mSortBy.equals(MovieAsyncTask.FAVORITES)) {
                    getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
                }
                mSortBy = MovieAsyncTask.TOP_RATED;
                fetchMovies(mSortBy);
                item.setChecked(true);
                break;
            case R.id.sort_by_most_popular:
                if (mSortBy.equals(MovieAsyncTask.FAVORITES)) {
                    getSupportLoaderManager().destroyLoader(FAVORITE_MOVIES_LOADER);
                }
                mSortBy = MovieAsyncTask.MOST_POPULAR;
                fetchMovies(mSortBy);
                item.setChecked(true);
                break;
            case R.id.sort_by_favorites:
                mSortBy = MovieAsyncTask.FAVORITES;
                item.setChecked(true);
                fetchMovies(mSortBy);
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void open(Movie movie, int position) {
        if (mBigScreen) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.ARG_MOVIE, movie);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailFragment.ARG_MOVIE, movie);
            startActivity(intent);
        }
    }

    @Override
    public void onMovieFetchFinished(Command command) {
        if (command instanceof MovieAsyncTask.NotifyAboutTaskCompletionCommand) {
            mAdapter.add(((MovieAsyncTask.NotifyAboutTaskCompletionCommand) command).getMovies());
            updateEmptyState();
            findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.add(cursor);
        updateEmptyState();
        findViewById(R.id.progress).setVisibility(View.GONE);

    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        findViewById(R.id.progress).setVisibility(View.VISIBLE);
        return new CursorLoader(this,
                Contract.MovieEntry.CONTENT_URI,
                Contract.MovieEntry.MOVIE_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> cursorLoader) {
        // empty
    }

    private void fetchMovies(String sortBy) {

        //this causes conflict with the logic behind the screen shown when no connection. Still looking into it.
        //NetworkInfo activeNetwork = null;
        //boolean isWiFi = activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
        //ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        //activeNetwork = cm.getActiveNetworkInfo();
        //boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!sortBy.equals(MovieAsyncTask.FAVORITES)) {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
            MovieAsyncTask.NotifyAboutTaskCompletionCommand command = new MovieAsyncTask.NotifyAboutTaskCompletionCommand(this.mMainFragment);
            new MovieAsyncTask(sortBy, command).execute();
        } else {
            getSupportLoaderManager().initLoader(FAVORITE_MOVIES_LOADER, null, this);
        }
    }

    private void updateEmptyState() {
        if (mAdapter.getItemCount() == 0) {
            if (mSortBy.equals(MovieAsyncTask.FAVORITES)) {
                findViewById(R.id.empty_state_container).setVisibility(View.GONE);
                findViewById(R.id.empty_state_favorites_container).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.empty_state_container).setVisibility(View.VISIBLE);
                findViewById(R.id.empty_state_favorites_container).setVisibility(View.GONE);
            }
        } else {
            findViewById(R.id.empty_state_container).setVisibility(View.GONE);
            findViewById(R.id.empty_state_favorites_container).setVisibility(View.GONE);
        }
    }
    /*public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }*/




}
