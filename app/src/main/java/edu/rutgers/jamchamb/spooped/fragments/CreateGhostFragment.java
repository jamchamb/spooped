package edu.rutgers.jamchamb.spooped.fragments;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;

import java.util.ArrayList;

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

        ArrayList<String> ghostList = new ArrayList<String>();
        ghostList.add("ghost_one_teal");
        ghostList.add("ghost_two_purple");
        ghostList.add("ghost_three_yellow");
        ghostList.add("ghost_four_orange");

        final ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewPager);
        final EditText nameEditText = (EditText) v.findViewById(R.id.nameEditText);
        final EditText userEditText = (EditText) v.findViewById(R.id.userEditText);
        final Button createButton = (Button) v.findViewById(R.id.createButton);


        final GhostPagerAdapter pagerAdapter = new GhostPagerAdapter(getFragmentManager(), ghostList);
        viewPager.setAdapter(pagerAdapter);

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
                        newGhost.setDrawable(pagerAdapter.getGhostName(viewPager.getCurrentItem()));
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
                                Log.w(TAG, result.getMessage());
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

    public class GhostPagerAdapter extends FragmentStatePagerAdapter {

        private ArrayList<String> mGhostList;

        public GhostPagerAdapter(FragmentManager fm, ArrayList<String> ghostList) {
            super(fm);
            if(ghostList != null) mGhostList = ghostList;
            else mGhostList = new ArrayList<String>();
        }

        public String getGhostName(int position) {
            return mGhostList.get(position);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new GhostImageFragment();
            Bundle args = new Bundle();

            String ghosty = mGhostList.get(i);
            if(ghosty != null) {
                args.putString("ghost", ghosty);
            }

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return mGhostList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Ghost " + position;
        }
    }

    public static class GhostImageFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            View rootView = inflater.inflate(R.layout.ghost_view, container, false);
            Bundle args = getArguments();

            ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);

            if(args.getString("ghost") != null) {
                int id = getResources().getIdentifier(args.getString("ghost"), "drawable", "edu.rutgers.jamchamb.spooped");
                if (id != 0) {
                    Drawable drawable = getResources().getDrawable(id);
                    imageView.setImageDrawable(drawable);
                }
            }

            return rootView;
        }
    }

}
