package com.example.android.popularmovies;

import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.example.android.popularmovies.network.DBService;
import com.example.android.popularmovies.network.Trailer;
import com.example.android.popularmovies.network.Trailers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class TrailerAsyncTask extends AsyncTask<Long, Void, List<Trailer>> {

    @SuppressWarnings("unused")
    public static final String LOG_TAG = TrailerAsyncTask.class.getSimpleName();
    private final Listener mListener;

    /**
     * Interface definition for a callback to be invoked when trailers are loaded.
     */
    interface Listener {
        void onTrailersFetchFinished(List<Trailer> trailers);
    }

    public TrailerAsyncTask(Listener listener) {
        mListener = listener;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected List<Trailer> doInBackground(Long... params) {
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
        Call<Trailers> call = service.findTrailersById(movieId,
                BuildConfig.API_KEY);
        try {
            Response<Trailers> response = call.execute();
            Trailers trailers = response.body();
            return Objects.requireNonNull(trailers).getTrailers();
        } catch (IOException e) {
            Log.e(LOG_TAG, "A problem occurred talking to the movie db ", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(List<Trailer> trailers) {
        if (trailers != null) {
            mListener.onTrailersFetchFinished(trailers);
        } else {
            mListener.onTrailersFetchFinished(new ArrayList<Trailer>());
        }
    }
}

