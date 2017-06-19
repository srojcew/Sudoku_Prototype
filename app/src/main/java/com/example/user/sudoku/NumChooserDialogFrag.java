package com.example.user.sudoku;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;

public class NumChooserDialogFrag extends DialogFragment {

    private NumChooserDialogFragListener chooserListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.sudoku_numbers, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chooserListener.numSelected(Integer.toString(which + 1));
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            chooserListener = (NumChooserDialogFragListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement NumChooserDialogFragListener");
        }
    }

    public interface NumChooserDialogFragListener {
        public void numSelected(String number);
    }

}
