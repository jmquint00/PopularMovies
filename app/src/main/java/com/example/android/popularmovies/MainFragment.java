package com.example.android.popularmovies;

import android.support.v4.app.Fragment;
import android.os.Bundle;


public class MainFragment extends Fragment implements MovieAsyncTask.Listener {

    private boolean mPaused = false;
    private Command mWaitingCommand = null;

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPaused = false;
        if (mWaitingCommand != null) {
            onMovieFetchFinished(mWaitingCommand);
        }
    }

    @Override
    public void onMovieFetchFinished(Command command) {
        if (getActivity() instanceof MovieAsyncTask.Listener && !mPaused) {
            MovieAsyncTask.Listener listener = (MovieAsyncTask.Listener) getActivity();
            listener.onMovieFetchFinished(command);
            mWaitingCommand = null;
        } else {

            mWaitingCommand = command;
        }
    }
}
