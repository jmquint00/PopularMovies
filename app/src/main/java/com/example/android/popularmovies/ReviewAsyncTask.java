package com.example.android.popularmovies;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.android.popularmovies.network.DBService;
import com.example.android.popularmovies.network.Review;
import com.example.android.popularmovies.network.Reviews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class ReviewAsyncTask extends AsyncTask<Long, Void, List<Review>> {

    @SuppressWarnings("unused")
    public static final String LOG_TAG = ReviewAsyncTask.class.getSimpleName();
    private final Listener mListener;

    /**
     * Interface definition for a callback to be invoked when reviews are loaded.
     */
    interface Listener {
        void onReviewsFetchFinished(List<Review> reviews);
    }

    public ReviewAsyncTask(Listener listener) {
        mListener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected List<Review> doInBackground(Long... params) {
        // If there's no movie id, there's nothing to look up.
        if (params.length == 0) {
            return null;
        }
        long movieId = params[0];

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://api.themoviedb.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DBService service = retrofit.create(DBService.class);
        Call<Reviews> call = service.findReviewsById(movieId,
                BuildConfig.API_KEY);
        try {
            Response<Reviews> response = call.execute();
            Reviews reviews = response.body();
            return Objects.requireNonNull(reviews).getReviews();
        } catch (IOException e) {
            Log.e(LOG_TAG, "A problem occurred talking to the movie db ", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Review> reviews) {
        if (reviews != null) {
            mListener.onReviewsFetchFinished(reviews);
        } else {
            mListener.onReviewsFetchFinished(new ArrayList<Review>());
        }
    }
}

