package me.spoop.spoopdroid.fragments;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import me.spoop.spoopdroid.Config;
import me.spoop.spoopdroid.R;
import me.spoop.spoopdroid.api.SpoopedAPI;
import me.spoop.spoopdroid.items.Ghost;
import me.spoop.spoopdroid.items.JSendResponse;
import me.spoop.spoopdroid.items.LocationProvider;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Ghost creation screen
 */
public class CreateGhostFragment extends Fragment {

    private static final String TAG = "CreateGhostFragment";

    private LocationProvider mLocationProvider;
    private SpoopedAPI mSpiritRealm;

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
        final View v = inflater.inflate(R.layout.fragment_create_ghost, container, false);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Config.BASE_URL)
                .build();

        mSpiritRealm = restAdapter.create(SpoopedAPI.class);

        final ArrayList<String> ghostList = new ArrayList<>();
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
                if(nameEditText.getText().toString().trim().isEmpty() || userEditText.getText().toString().trim().isEmpty()) {
                    Toast.makeText(getActivity(), R.string.create_missing_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get location and submit ghost
                if(mLocationProvider != null) {
                    Location lastLocation = mLocationProvider.getLastLocation();
                    if(lastLocation != null) {
                        Ghost newGhost = new Ghost();
                        newGhost.setName(nameEditText.getText().toString().trim());
                        newGhost.setUser(userEditText.getText().toString().trim());
                        newGhost.setDrawable(pagerAdapter.getGhostName(viewPager.getCurrentItem()));
                        newGhost.setLocation(lastLocation);

                        mSpiritRealm.submitGhost(newGhost, new Callback<JSendResponse>() {
                            @Override
                            public void success(JSendResponse result, Response response) {
                                if (result.succeeded()) {
                                    Toast.makeText(getActivity(), result.getMessage(), Toast.LENGTH_SHORT).show();
                                    nameEditText.setText(null);
                                    userEditText.setText(null);
                                    viewPager.setCurrentItem(0);
                                } else {
                                    Toast.makeText(getActivity(), result.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(getActivity(), R.string.error_network, Toast.LENGTH_SHORT).show();
                                Log.w(TAG, error.getMessage());
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), R.string.error_location, Toast.LENGTH_SHORT).show();
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

        public GhostPagerAdapter(FragmentManager fm, @NonNull ArrayList<String> ghostList) {
            super(fm);
            mGhostList = ghostList;
        }

        public String getGhostName(int position) {
            return mGhostList.get(position);
        }

        @Override
        public Fragment getItem(int i) {
            return GhostImageFragment.newInstance(mGhostList.get(i));
        }

        @Override
        public int getCount() {
            return mGhostList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return String.format(getString(R.string.create_frag_title), position);
        }

    }

    public static class GhostImageFragment extends Fragment {

        private static final String ARG_GHOST_TAG = "ghost";

        public GhostImageFragment() {}

        public static GhostImageFragment newInstance(String ghostName) {
            GhostImageFragment fragment = new GhostImageFragment();
            Bundle args = new Bundle();
            args.putString(ARG_GHOST_TAG, ghostName);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            // The last two arguments ensure LayoutParams are inflated
            // properly.
            final View rootView = inflater.inflate(R.layout.ghost_view, container, false);
            final Bundle args = getArguments();

            ImageView imageView = (ImageView) rootView.findViewById(R.id.imageView);

            if(args.getString(ARG_GHOST_TAG) != null) {
                int id = getResources().getIdentifier(args.getString(ARG_GHOST_TAG), "drawable", Config.PACKAGE_NAME);
                if (id != 0) {
                    Drawable drawable = getResources().getDrawable(id);
                    imageView.setImageDrawable(drawable);

                    Animation floatAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.ghost_float);
                    imageView.startAnimation(floatAnimation);
                }
            }

            return rootView;
        }

    }

}
