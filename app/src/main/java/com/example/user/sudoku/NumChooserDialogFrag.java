package com.example.user.sudoku;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class NumChooserDialogFrag extends DialogFragment {

    private NumChooserDialogFragListener chooserListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        /*builder.setItems(R.array.sudoku_numbers, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                chooserListener.numSelected(Integer.toString(which));
            }
        });
        AlertDialog numChooserDialog = builder.create();
        numChooserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));


        return numChooserDialog;*/

        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.num_chooser_view, null));

        AlertDialog numChooserDialog = builder.create();
        numChooserDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));

        Button num1Button = numChooserDialog.findViewById(R.id.num1_button);
        num1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooserListener.numSelected("1");
            }
        });
        return numChooserDialog;
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
