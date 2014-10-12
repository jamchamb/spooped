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

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import edu.rutgers.jamchamb.spooped.R;
import edu.rutgers.jamchamb.spooped.api.SpiritRealm;
import edu.rutgers.jamchamb.spooped.items.Ghost;
import edu.rutgers.jamchamb.spooped.items.JSendResponse;
import edu.rutgers.jamchamb.spooped.items.LocationProvider;

/**
 * Ghost creation screen
 */
public class CreateGhostFragment extends Fragment {

    private static final String TAG = "CreateGhostFragment";

    private LocationProvider mLocationProvider;
    private SpiritRealm mSpiritRealm;
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

        mSpiritRealm = new SpiritRealm(getActivity());

        final ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        final EditText nameEditText = (EditText) v.findViewById(R.id.nameEditText);
        final EditText userEditText = (EditText) v.findViewById(R.id.userEditText);
        final Button createButton = (Button) v.findViewById(R.id.createButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Make sure username and ghost name are set
                if(nameEditText.getText().toString().isEmpty() || userEditText.getText().toString().isEmpty()) {
                    Toast.makeText(getActivity(), "Set both names!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get location and submit ghost
                if(mLocationProvider != null) {
                    Location lastLocation = mLocationProvider.getLastLocation();
                    if(lastLocation != null) {
                        Ghost newGhost = new Ghost();
                        newGhost.setName(nameEditText.getText().toString());
                        newGhost.setUser(userEditText.getText().toString());
                        newGhost.setLocation(lastLocation);

                        mSpiritRealm.submitGhost(newGhost).done(new DoneCallback<JSendResponse>() {
                            @Override
                            public void onDone(JSendResponse result) {
                                if(result.getStatus().equals("success")) {
                                    Toast.makeText(getActivity(), result.getMessage(), Toast.LENGTH_SHORT).show();
                                    nameEditText.setText(null);
                                    userEditText.setText(null);
                                    viewPager.setCurrentItem(0);
                                } else {
                                    Toast.makeText(getActivity(), result.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        }).fail(new FailCallback<Exception>() {
                            @Override
                            public void onFail(Exception result) {
                                Toast.makeText(getActivity(), "Oops! Network problem.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Couldn't get location", Toast.LENGTH_SHORT).show();
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
