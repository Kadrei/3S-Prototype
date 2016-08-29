package de.thmgames.s3.Fragments.DialogFragments;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import de.thmgames.s3.BuildConfig;
import de.thmgames.s3.Controller.Users;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.Otto.OttoEventBusProvider;

/**
 * Created by Benedikt on 09.02.2015.
 */
abstract public class BaseDialogFragment extends DialogFragment {

    public final static String TAG = BaseDialogFragment.class.getName();
    protected User mCurrentUser;
    protected Picasso mPicasso;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCurrentUser = Users.getCurrentS3User();
        mPicasso = Picasso.with(this.getActivity());
        mPicasso.setIndicatorsEnabled(BuildConfig.DEBUG);
        mPicasso.setLoggingEnabled(BuildConfig.DEBUG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        OttoEventBusProvider.getInstanceForUIThread().register(this);
        OttoEventBusProvider.getInstance().register(this);
        super.onStart();
        // safety check
        if (getDialog() == null) {
            return;
        }

        int dialogWidth = LinearLayout.LayoutParams.MATCH_PARENT;
        int dialogHeight = LinearLayout.LayoutParams.WRAP_CONTENT;

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);

    }



    @Override
    public void onStop() {
        OttoEventBusProvider.getInstance().unregister(this);
        OttoEventBusProvider.getInstanceForUIThread().unregister(this);
        super.onStop();
    }

}
