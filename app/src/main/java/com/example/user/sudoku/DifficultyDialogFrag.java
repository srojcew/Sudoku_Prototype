package com.example.user.sudoku;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import com.example.user.sudoku.backend.TypeConstants;


public class DifficultyDialogFrag extends  DialogFragment {

    private DifficultyDialogListener difficultyListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setItems(R.array.difficulties, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                difficultyListener.difficultySelectedNowGenerate(which);
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            difficultyListener = (DifficultyDialogListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DifficultyDialogListener");
        }
    }

    public interface DifficultyDialogListener {
        public void difficultySelectedNowGenerate(int difficulty);
    }
}
