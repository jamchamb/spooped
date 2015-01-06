package me.spoop.spoopdroid;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.List;

import me.spoop.spoopdroid.api.SpoopedAPI;
import me.spoop.spoopdroid.items.Ghost;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SpoopService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private final static String TAG = "SpoopService";

    private final static int NEARBY_METERS = 25;

    private SpoopedAPI mSpiritRealm;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private WindowManager windowManager;
    private ViewGroup mViewGroup;
    private HashMap<String, Ghost> mGhostCollection = new HashMap<>();

    public SpoopService() {}

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up location client and request parameters
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(10 * 60 * 1000);
        mLocationRequest.setFastestInterval(60 * 1000);

        // Connect to location services & start getting location updates
        mGoogleApiClient.connect();

        // Set up API
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Config.BASE_URL)
                .build();

        mSpiritRealm = restAdapter.create(SpoopedAPI.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mGoogleApiClient.disconnect();
        if(mViewGroup != null) windowManager.removeView(mViewGroup);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Location services connected");
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.w(TAG, "Google API client connection suspended - cause " + cause);
        if(!mGoogleApiClient.isConnecting()) mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(final Location location) {
        Log.d(TAG, "Location changed: " + location.toString());

        mSpiritRealm.getGhosts(Double.toString(location.getLongitude()), Double.toString(location.getLatitude()), new Callback<List<Ghost>>() {
            @Override
            public void success(List<Ghost> ghosts, Response response) {
                Log.i(TAG, "Got " + ghosts.size() + " ghosts in range");

                // Check the ghosties. Display ones that haven't been seen before
                for(Ghost ghost: ghosts) {
                    if(!mGhostCollection.containsKey(ghost.getId()) && location.distanceTo(ghost.getLocation()) <= NEARBY_METERS) {
                        Log.d(TAG, "Ghost within " + NEARBY_METERS + " meters; spooping!");
                        showGhost(ghost);
                        mGhostCollection.put(ghost.getId(), ghost);
                        break;
                    }
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, error.getMessage());
            }
        });

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Location services failed to connect. Error code " + connectionResult.getErrorCode());
    }

    /**
     * Haunt the user's screen. Spoopy!
     * @param ghost Ghost to display
     */
    private void showGhost(final Ghost ghost) {
        // Pick ghost to display
        int resId = getResources().getIdentifier(ghost.getDrawable(), "drawable", Config.PACKAGE_NAME);
        if(resId == 0) resId = R.drawable.ghost_one_teal;

        // Display the ghost
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // If there's already a ghost displayed, don't do anything
        if(mViewGroup != null) return;

        ImageView spoopyGhostView = new ImageView(this);
        spoopyGhostView.setImageResource(resId);
        spoopyGhostView.setAlpha(0.85f);

        // Make it spoopy and floaty
        Animation floatAnimation = AnimationUtils.loadAnimation(spoopyGhostView.getContext(), R.anim.ghost_float);
        spoopyGhostView.startAnimation(floatAnimation);

        // Wraps the ghost view so that animations work
        mViewGroup = new LinearLayout(this);
        mViewGroup.addView(spoopyGhostView);

        // Layout parameters for window manager
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
        params.x = 0;
        params.y = 0;

        windowManager.addView(mViewGroup, params);
        vibrator.vibrate(new long[]{0l,300l,100l,300l}, -1);

        // Make the ghost disappear if you touch it
        spoopyGhostView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        Toast.makeText(v.getContext(), String.format(getString(R.string.spoop_message), ghost.getName(), ghost.getUser()), Toast.LENGTH_SHORT).show();
                        windowManager.removeView(mViewGroup);
                        mViewGroup = null;
                        return true;
                    default:
                        return false;
                }
            }
        });

   }

}
