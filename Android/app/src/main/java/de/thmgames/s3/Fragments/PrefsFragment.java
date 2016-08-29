package de.thmgames.s3.Fragments;


import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseException;
import com.parse.SaveCallback;

import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;
import de.thmgames.s3.Controller.Installations;
import de.thmgames.s3.Otto.Events.ShowSnackBarEvent;
import de.thmgames.s3.Otto.OttoEventBusProvider;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.AndroidUtils;
import de.thmgames.s3.Utils.LayoutUtils;
import de.thmgames.s3.Utils.LogUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onStart() {
        OttoEventBusProvider.getInstance().unregister(this);
        OttoEventBusProvider.getInstanceForUIThread().unregister(this);
        super.onStart();
    }

    @Override
    public void onStop() {
        OttoEventBusProvider.getInstance().unregister(this);
        OttoEventBusProvider.getInstanceForUIThread().unregister(this);
        super.onStop();
    }

    public void showSnackBar(String text) {
        if (getActivity() == null) return;
        OttoEventBusProvider.getInstanceForUIThread().post(new ShowSnackBarEvent(text));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        int topPadding = !AndroidUtils.isLollipopOrHigher() ? (int) getActivity().getResources().getDimension(R.dimen.pref_paddingTop) : 0;
        if (v != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !LayoutUtils.isInLandscape(PrefsFragment.this.getActivity())) {
                v.setPadding(0, topPadding, 0, LayoutUtils.getNavigationBarHeight(PrefsFragment.this.getActivity()));
            } else {
                v.setPadding(0, topPadding, 0, 0);
            }
        }

        return v;
    }

    private SharedPreferences mPrefs;
    private CheckBoxPreference  pushPref;
    private Preference feedbackPref;
    private Preference openSourcePref;

    @Override
    public void onResume() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pushPref = (CheckBoxPreference) findPreference(getString(R.string.key_push_sp));
        if (pushPref != null) {
            pushPref.setOnPreferenceChangeListener(this);
        }
        feedbackPref = findPreference(getString(R.string.key_feedback_prefs));
        if (feedbackPref != null) {
            feedbackPref.setOnPreferenceClickListener(this);
        }
        openSourcePref = findPreference(getString(R.string.key_thirdparties_prefs));
        if (openSourcePref != null) {
            openSourcePref.setOnPreferenceClickListener(this);
        }
        super.onResume();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (getActivity() == null) return false;
        if(preference==feedbackPref){
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("mailto:" + getActivity().getString(R.string.developer_mail)));
            i.putExtra(Intent.EXTRA_SUBJECT, getActivity().getString(R.string.app_name) + " " + getActivity().getString(R.string.feedback));
            startActivity(Intent.createChooser(i, getActivity().getString(R.string.select_mail_app)));
            return true;
        }else if(preference==openSourcePref){
            new LicensesDialog.Builder(getActivity()).setNotices(R.raw.licenses).setIncludeOwnLicense(true).setTitle(R.string.open_source_title).build().show();
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object tmpValue) {
        if (getActivity() == null || preference != pushPref || !(tmpValue instanceof Boolean)) return false;
        final Boolean newValue = (Boolean) tmpValue;
        preference.setEnabled(false);
        try {
            Installations.setPushForBroadcastChannelEnabled(newValue, new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    preference.setEnabled(true);
                    boolean temporaryValue = true;
                    if (newValue) {
                        if (e == null) {
                            LogUtils.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                        } else {
                            LogUtils.e("com.parse.push", "failed to subscribe for push", e);
                            temporaryValue = false;
                            showSnackBar(getString(R.string.error_activating_push));
                        }
                    } else {
                        if (e == null) {
                            temporaryValue = false;
                            LogUtils.d("com.parse.push", "successfully unsubscribed to the broadcast channel.");
                        } else {
                            LogUtils.e("com.parse.push", "failed to unsubscribe for push", e);
                            showSnackBar(getString(R.string.error_unsubscribing_push));
                        }
                    }
                    preference.getEditor().putBoolean(getString(R.string.key_push_sp), temporaryValue);
                    pushPref.setChecked(temporaryValue);
                }
            });
        }catch(IllegalArgumentException e){
            showSnackBar(newValue? getString(R.string.error_activating_push):getString(R.string.error_unsubscribing_push));
        }
        return false;
    }
}
