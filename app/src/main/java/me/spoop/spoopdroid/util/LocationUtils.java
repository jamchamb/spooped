package me.spoop.spoopdroid.util;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import me.spoop.spoopdroid.fragments.ErrorDialogFragment;

public class LocationUtils {
    public final static int REQUEST_RESOLVE_ERROR = 1001;

    public static boolean servicesConnected(Activity activity) {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d("Location Updates", "Google Play services is available.");
            // Continue
            return true;
            // Google Play services was not available for some reason.
            // resultCode holds the error code.
        } else {
            showErrorDialog(activity, resultCode);
            return false;
        }
    }

    public static void showErrorDialog(Activity activity, int resultCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                resultCode,
                activity,
                LocationUtils.REQUEST_RESOLVE_ERROR);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {
            // Create a new DialogFragment for the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();
            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);
            // Show the error dialog in the DialogFragment
            errorFragment.show(activity.getFragmentManager(), "Location Updates");
        }
    }

}
