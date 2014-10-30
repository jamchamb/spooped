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
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import me.spoop.spoopdroid.fragments.CreateGhostFragment;
import me.spoop.spoopdroid.items.LocationProvider;
import me.spoop.spoopdroid.util.LocationUtils;


public class MainActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationProvider {

    private static final String TAG = "MainActivity";

    private static final String STATE_RESOLVING_ERROR = "resolving_error";

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private boolean mResolvingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CreateGhostFragment())
                    .commit();
        } else {
            mResolvingError = savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);
        }

        // Start up location services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Launch ghost detecting service in background
        startService(new Intent(this, SpoopService.class));
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
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
    public Location getLastLocation() {
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return mLastLocation;
        } else {
            return null;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected");
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(BuildConfig.DEBUG) Log.d(TAG, "Location: " + mLastLocation.toString());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.w(TAG, "Google API client connection suspended - cause " + cause);
        if(!mGoogleApiClient.isConnecting()) mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mResolvingError) {
            return;
        } else if (result.hasResolution()) {
            try {
                mResolvingError = true;
                result.startResolutionForResult(this, LocationUtils.REQUEST_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, e.getMessage());
                mGoogleApiClient.connect(); // try to connect again
            }
        } else {
            LocationUtils.showErrorDialog(this, result.getErrorCode());
            mResolvingError = true;
        }
    }

    public void onDialogDismissed() {
        mResolvingError = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Decide what to do based on the original request code
        switch (requestCode) {
            case LocationUtils.REQUEST_RESOLVE_ERROR:
                mResolvingError = false;
                switch (resultCode) {
                    case Activity.RESULT_OK :
                        // If the result code is Activity.RESULT_OK, try the request again
                        if(!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                            Log.d(TAG, "Location services: Result OK, reconnecting...");
                            mGoogleApiClient.connect();
                        }
                        break;
                }
                break;

            default:
                Log.d(TAG, "Location services connection resolution: result code " + resultCode);
        }
    }

}
