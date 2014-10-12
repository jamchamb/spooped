package edu.rutgers.jamchamb.spooped;

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
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import org.jdeferred.DoneCallback;

import java.util.HashMap;
import java.util.List;

import edu.rutgers.jamchamb.spooped.api.SpiritRealm;

public class SpoopService extends Service implements GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationListener {
    private final static String TAG = "SpoopService";

    private SpiritRealm mSpiritRealm;
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    private WindowManager windowManager;
    private ImageView mSpoopyGhostView;
    private HashMap<String, Ghost> mGhostCollection = new HashMap<String, Ghost>();

    public SpoopService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up location client and request parameters
        mLocationClient = new LocationClient(this, this, this);
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(10 * 1000);

        // Connect to location services & start getting location updates
        mLocationClient.connect();

        mSpiritRealm = new SpiritRealm(this);
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
    public void onLocationChanged(final Location location) {
        Log.d(TAG, "Location changed: " + location.toString());

        final Context servContext = this;

        mSpiritRealm.getGhosts().done(new DoneCallback<List<Ghost>>() {
            @Override
            public void onDone(List<Ghost> ghostList) {
                for(Ghost ghost: ghostList) {
                    Log.d(TAG, "Ghost " + ghost.getId());
                    Log.d(TAG, ghost.getName() + " by " + ghost.getUser() + ", lon:" + ghost.getLocation().getLongitude() + " lat: " + ghost.getLocation().getLatitude());
                }

                // Check the ghosties
                for(Ghost ghost: ghostList) {
                    // If it's within range and hasn't been seen before, display it
                    if(mGhostCollection.get(ghost.getId()) == null && location.distanceTo(ghost.getLocation()) <= 50) {
                        Log.d(TAG, "Within 50 meters, spooping...");
                        showGhost(R.drawable.ghost_spoopy);
                        Toast.makeText(servContext, ghost.getName() + " by " + ghost.getUser(), Toast.LENGTH_SHORT).show();
                        mGhostCollection.put(ghost.getId(), ghost);
                        break;
                    }
                }

            }
        });

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.w(TAG, "Location services failed to connect");
        Toast.makeText(this, "Location services failed", Toast.LENGTH_LONG).show();
    }

    /**
     * Haunt the user's screen. Spoopy!
     * @param resId Drawable resource ID for ghost to display
     */
    private void showGhost(int resId) {
        // Display the ghost
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // If there's already a ghost displayed, don't do anything
        if(mSpoopyGhostView != null) return;

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
        vibrator.vibrate(new long[]{0l,300l,100l,300l}, -1);

        // Make the ghost disappear if you touch it
        mSpoopyGhostView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //v.setVisibility(View.GONE);
                        windowManager.removeView(mSpoopyGhostView);
                        mSpoopyGhostView = null;
                        return true;
                    default:
                        return false;
                }
            }
        });

    }

}
