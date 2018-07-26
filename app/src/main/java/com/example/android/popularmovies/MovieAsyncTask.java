package com.example.android.popularmovies;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.StringDef;
import android.util.Log;

import com.example.android.popularmovies.network.DBService;
import com.example.android.popularmovies.network.Movie;
import com.example.android.popularmovies.network.Movies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieAsyncTask extends AsyncTask<Void, Void, List<Movie>> {

    @SuppressWarnings("unused")
    public static final String LOG_TAG = MovieAsyncTask.class.getSimpleName();

    public final static String MOST_POPULAR = "popular";
    public final static String TOP_RATED = "top_rated";
    public final static String FAVORITES = "favorites";

    @StringDef({MOST_POPULAR, TOP_RATED, FAVORITES})
    public @interface SORT_BY {
    }

    /**
     * Will be called in {@link MovieAsyncTask#onPostExecute(List)} to notify subscriber to about
     * task completion.
     */
    private final NotifyAboutTaskCompletionCommand mCommand;
    private
    @SORT_BY
    String mSortBy;

    /**
     * Interface definition for a callback to be invoked when movies are loaded.
     */
    interface Listener {
        void onMovieFetchFinished(Command command);
    }

    /**
     * Possible good idea to use {@link android.content.AsyncTaskLoader}, which by default is tied
     * to lifecycle method, but this approach has its own limitations
     * (i. e. publish progress is not possible in this case).
     * <p/>
     * Idea is to use AsyncTasks in combination with non-UI retained fragment and Command pattern.
     * It helps save calls which we cannot execute immediately for later.
     */
    public static class NotifyAboutTaskCompletionCommand implements Command {
        private final MovieAsyncTask.Listener mListener;
        // The result of the task execution.
        private List<Movie> mMovies;

        public NotifyAboutTaskCompletionCommand(MovieAsyncTask.Listener listener) {
            mListener = listener;
        }

        @Override
        public void execute() {
            mListener.onMovieFetchFinished(this);
        }

        public List<Movie> getMovies() {
            return mMovies;
        }
    }

    public MovieAsyncTask(@SORT_BY String sortBy, NotifyAboutTaskCompletionCommand command) {
        mCommand = command;
        mSortBy = sortBy;
    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        if (movies != null) {
            mCommand.mMovies = movies;
        } else {
            mCommand.mMovies = new ArrayList<>();
        }
        mCommand.execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected List<Movie> doInBackground(Void... params) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DBService service = retrofit.create(DBService.class);
        Call<Movies> call = service.discoverMovies(mSortBy,
                BuildConfig.API_KEY);
        try {
            Response<Movies> response = call.execute();
            Movies movies = response.body();
            return Objects.requireNonNull(movies).getMovies();

        } catch (IOException e) {
            Log.e(LOG_TAG, "An error has occurred when talking to TMDB", e);
        }
        return null;
    }
}

