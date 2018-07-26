package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.popularmovies.data.Contract;
import com.example.android.popularmovies.network.Movie;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.ViewHolder> {

    @SuppressWarnings("unused")
    private final static String LOG_TAG = MovieListAdapter.class.getSimpleName();

    private final ArrayList<Movie> mMovies;
    private final Callbacks mCallbacks;




    public interface Callbacks {
        void open(Movie movie, int position);
    }

    public MovieListAdapter(ArrayList<Movie> movies, Callbacks callbacks) {
        mMovies = movies;
        this.mCallbacks = callbacks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_content, parent, false);
        final Context context = view.getContext();

        int gridColsNumber = context.getResources()
                .getInteger(R.integer.grid_number_cols);

        view.getLayoutParams().height = (int) (parent.getWidth() / gridColsNumber *
                Movie.POSTER_ASPECT_RATIO);

        return new ViewHolder(view);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.cleanUp();
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Movie movie = mMovies.get(position);
        final Context context = holder.mView.getContext();

        holder.mMovie = movie;
        holder.mTitleView.setText(movie.getTitle());

        String posterUrl = movie.getPosterUrl(context);
        // onError() will not be called, if url is null.
        // Empty url leads to app crash.
        if (posterUrl == null) {
            holder.mTitleView.setVisibility(View.VISIBLE);
        }

        Picasso.with(context)
                .load(movie.getPosterUrl(context))
                .config(Bitmap.Config.RGB_565)
                .into(holder.mThumbnailView,
                        new Callback() {
                            @Override
                            public void onSuccess() {
                                if (holder.mMovie.getId() != movie.getId()) {
                                    holder.cleanUp();
                                } else {
                                    holder.mThumbnailView.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError() {
                                holder.mTitleView.setVisibility(View.VISIBLE);
                            }
                        }
                );

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.open(movie, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        @BindView(R.id.thumbnail)
        ImageView mThumbnailView;
        @BindView(R.id.title)
        TextView mTitleView;
        Movie mMovie;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            mView = view;
        }

        void cleanUp() {
            final Context context = mView.getContext();
            Picasso.with(context).cancelRequest(mThumbnailView);
            mThumbnailView.setImageBitmap(null);
            mThumbnailView.setVisibility(View.INVISIBLE);
            mTitleView.setVisibility(View.GONE);
        }

    }

    public void add(List<Movie> movies) {
        mMovies.clear();
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }

    public void add(Cursor cursor) {
        mMovies.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(Contract.MovieEntry.COL_MOVIE_ID);
                String title = cursor.getString(Contract.MovieEntry.COL_MOVIE_TITLE);
                String posterPath = cursor.getString(Contract.MovieEntry.COL_MOVIE_POSTER_PATH);
                String overview = cursor.getString(Contract.MovieEntry.COL_MOVIE_OVERVIEW);
                String rating = cursor.getString(Contract.MovieEntry.COL_MOVIE_VOTE_AVERAGE);
                String releaseDate = cursor.getString(Contract.MovieEntry.COL_MOVIE_RELEASE_DATE);
                String backdropPath = cursor.getString(Contract.MovieEntry.COL_MOVIE_BACKDROP_PATH);
                Movie movie = new Movie(id, title, posterPath, overview, rating, releaseDate, backdropPath);
                mMovies.add(movie);
            } while (cursor.moveToNext());
        }
        notifyDataSetChanged();
    }

    public ArrayList<Movie> getMovies() {
        return mMovies;
    }
}

