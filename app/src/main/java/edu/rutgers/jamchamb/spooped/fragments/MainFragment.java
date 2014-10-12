package edu.rutgers.jamchamb.spooped.fragments;

/**
 * Main display
 */


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import edu.rutgers.jamchamb.spooped.R;
import edu.rutgers.jamchamb.spooped.SpoopService;

/**
 * Main screen
 */
public class MainFragment extends Fragment {

    private static final String TAG = "MainFragment";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final ToggleButton toggleButton = (ToggleButton) rootView.findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean on = ((ToggleButton)v).isChecked();
                toggleService(on);
            }
        });

        return rootView;
    }

    public void toggleService(boolean set) {
        if(getActivity() == null) {
            Log.w(TAG, "activity null");
        }
        if(set) getActivity().startService(new Intent(getActivity(), SpoopService.class));
        else getActivity().stopService(new Intent(getActivity(), SpoopService.class));
    }

}
