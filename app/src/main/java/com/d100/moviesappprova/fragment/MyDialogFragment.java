package com.d100.moviesappprova.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MyDialogFragment extends DialogFragment {
    String mTitolo, mMessaggio;
    long mId;
    boolean mIsDelete;
    DialogFragmentInterface mListener;

    public MyDialogFragment(String aTitolo, String aMessaggio, long aId, boolean aIsDelete) {
        mTitolo = aTitolo;
        mMessaggio = aMessaggio;
        mId = aId;
        mIsDelete = aIsDelete;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder vBuilder = new AlertDialog.Builder(getActivity());
        vBuilder.setTitle(mTitolo);
        vBuilder.setMessage(mMessaggio);
        vBuilder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onResponse(true, mId, mIsDelete);
            }
        });
        vBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onResponse(false, mId, mIsDelete);
            }
        });

        return vBuilder.create();
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        if(activity instanceof DialogFragmentInterface) {
            mListener = (DialogFragmentInterface) activity;
        } else {
            mListener = null;
        }
    }

    public interface DialogFragmentInterface {
        void onResponse(boolean aResponse, long aId, boolean aIsDelete);
    }
}
