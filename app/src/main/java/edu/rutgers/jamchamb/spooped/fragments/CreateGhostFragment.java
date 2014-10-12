package edu.rutgers.jamchamb.spooped.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.rutgers.jamchamb.spooped.R;

/**
 * Ghost creation screen
 */
public class CreateGhostFragment extends Fragment {

    private static final String TAG = "PlaceholderFragment";

    public CreateGhostFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_ghost, container, false);

        return v;
    }

}
