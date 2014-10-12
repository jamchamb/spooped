package edu.rutgers.jamchamb.spooped.fragments;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.rutgers.jamchamb.spooped.R;
import edu.rutgers.jamchamb.spooped.items.LocationProvider;

/**
 * Ghost creation screen
 */
public class CreateGhostFragment extends Fragment {

    private static final String TAG = "CreateGhostFragment";

    private LocationProvider mLocationProvider;
    private PagerAdapter mPagerAdapter;

    public CreateGhostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mLocationProvider = (LocationProvider) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_ghost, container, false);

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        EditText nameEditText = (EditText) v.findViewById(R.id.nameEditText);
        Button createButton = (Button) v.findViewById(R.id.createButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mLocationProvider != null) {
                    Location lastLocation = mLocationProvider.getLastLocation();
                    if(lastLocation != null) {
                        Toast.makeText(getActivity(), lastLocation.toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Wow!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mLocationProvider = null;
    }

}
