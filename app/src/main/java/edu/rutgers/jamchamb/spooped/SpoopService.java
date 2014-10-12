package edu.rutgers.jamchamb.spooped;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.util.ArrayList;
import java.util.List;

public class SpoopService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {
    private final static String TAG = "SpoopService";

    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private Location mLastLocation;
    private WindowManager windowManager;
    private ImageView mSpoopyGhostView;

    public SpoopService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up location client and request parameters
        mLocationClient = new LocationClient(this, this, this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(10 * 1000);

        // Connect to location services & start getting location updates
        mLocationClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLocationClient.disconnect();
        if(mSpoopyGhostView != null) windowManager.removeView(mSpoopyGhostView);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Location services connected");
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "Location services disconnected");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed: " + location.toString());
        //Toast.makeText(this, "Loc: " + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();

        List<Ghost> ghostList = new ArrayList<Ghost>();

        // Retrieve the ghosties here.. just a test one for now
        Location testSpot = new Location("ghosts");
        testSpot.setLatitude(40.502084);
        testSpot.setLongitude(-74.452370);

        Ghost testSpoop = new Ghost();
        testSpoop.setId("test1234");
        testSpoop.setName("Spoopy");
        testSpoop.setUser("pyrocow");
        testSpoop.setLocation(testSpot);

        ghostList.add(testSpoop);

        // Check the ghosties
        for(Ghost ghost: ghostList) {
            if(location.distanceTo(ghost.getLocation()) <= 50) {
                Log.d(TAG, "Within 50 meters, spooping...");
                showGhost(R.drawable.ghost_spoopy);
                Toast.makeText(this, ghost.getName() + " by " + ghost.getUser(), Toast.LENGTH_SHORT).show();
                break;
            }
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "Location services failed to connect");
        Toast.makeText(this, "Location services failed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Haunt the user's screen. Spoopy!
     * @param resId Drawable resource ID for ghost to display
     */
    private void showGhost(int resId) {
        // Display the ghost
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        if(mSpoopyGhostView != null) windowManager.removeView(mSpoopyGhostView);
        mSpoopyGhostView = new ImageView(this);
        mSpoopyGhostView.setImageResource(resId);

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

        windowManager.addView(mSpoopyGhostView, params);

        // Make the ghost disappear if you touch it
        mSpoopyGhostView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.setVisibility(View.GONE);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

}
