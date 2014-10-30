package me.spoop.spoopdroid.fragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import me.spoop.spoopdroid.MainActivity;

public class ErrorDialogFragment extends DialogFragment {
    private Dialog mDialog;

    public ErrorDialogFragment() {super();}

    /** Set the dialog to display */
    public void setDialog(Dialog dialog) {
        mDialog = dialog;
    }

    /** Return a Dialog to the DialogFragment. */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return mDialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        ((MainActivity)getActivity()).onDialogDismissed();
    }
}
