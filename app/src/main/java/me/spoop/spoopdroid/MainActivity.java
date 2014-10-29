package me.spoop.spoopdroid;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import me.spoop.spoopdroid.fragments.CreateGhostFragment;
import me.spoop.spoopdroid.items.LocationProvider;
import me.spoop.spoopdroid.util.LocationUtils;


public class MainActivity extends FragmentActivity implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener,
        LocationProvider {

    private static final String TAG = "MainActivity";

    private LocationClient mLocationClient;
    private Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CreateGhostFragment())
                    .commit();
        }

        mLocationClient = new LocationClient(this, this, this);

        startService(new Intent(this, SpoopService.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    public void onStop() {
        mLocationClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :
                switch (resultCode) {
                    case Activity.RESULT_OK :
                        // If the result code is Activity.RESULT_OK, try the request again
                        mLocationClient.connect();
                        Log.d(TAG, "Location services connection resolution: Result OK");
                        break;
                }
                break;
            default:
                Log.d(TAG, "Location services connection resolution: result code " + resultCode);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected");
        mLastLocation = mLocationClient.getLastLocation();
        if(BuildConfig.DEBUG) Log.d(TAG, "Location: " + mLastLocation.toString());
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "Location services disconnected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if(connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        } else {
            LocationUtils.showErrorDialog(this, connectionResult.getErrorCode());
        }
    }

    @Override
    public Location getLastLocation() {
        if(mLocationClient != null && mLocationClient.isConnected()) {
            mLastLocation = mLocationClient.getLastLocation();
            return mLastLocation;
        } else {
            return null;
        }
    }

}
