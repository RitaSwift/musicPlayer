package com.joy.player.fragment;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

/**
 */
public class AttachDialogFragment extends DialogFragment {

    public Activity mContext;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        this.mContext = activity;
    }


}
