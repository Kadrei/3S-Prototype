package de.thmgames.s3.Fragments.DialogFragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.squareup.otto.Subscribe;

import org.altbeacon.beacon.Beacon;
import org.apache.http.util.EncodingUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.thmgames.s3.Activities.StoryActivity;
import de.thmgames.s3.Controller.Jobs;
import de.thmgames.s3.Controller.Locations;
import de.thmgames.s3.Controller.ViewLocalizer;
import de.thmgames.s3.Controller.WebElements;
import de.thmgames.s3.Listener.DialogFragmentDismissListener;
import de.thmgames.s3.Model.ParseModels.Cloud.Responses.JobFinishedResponse;
import de.thmgames.s3.Model.ParseModels.Fraction;
import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.Parameter;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterDataProvider;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.ApiVersionParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.LocaleParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.NearLocationParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.TimestampParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserCodeParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserEnergyParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserFractionParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UserPointsParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier.UsernameParameterSatisfier;
import de.thmgames.s3.Model.ParseModels.ParameterSystem.ParametersDataLoader;
import de.thmgames.s3.Model.ParseModels.Questsystem.Job;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.Model.ParseModels.WebElement;
import de.thmgames.s3.Otto.Events.ActionReceivedEvent;
import de.thmgames.s3.Otto.Events.BeaconInRangeEvent;
import de.thmgames.s3.Otto.OttoEventBusProvider;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.LayoutUtils;
import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 09.02.2015.
 */
public class ParameterLoadingFragment extends BaseDialogFragment implements ParameterDataProvider, ParametersDataLoader.ParameterDataLoadedCallback, ParametersDataLoader.ParameterLoadingProgressListener, ViewTreeObserver.OnGlobalLayoutListener {

    public final static String TAG = ParameterLoadingFragment.class.getName();

    public final static String FRAGMENT_MODE = "PARAMETER_MODE";
    public final static String FRAGMENT_OBJECTID = "OBJECTID";
    public final static String FRAGMENT_QUESTID="QUESTID";
    public final static String FRAGMENT_IS_DIALOG="IS_DIALOG";
    public final static int MODE_JOB = 0;
    public final static int MODE_STORY_INTERN = 1;
    public final static int MODE_STORY_EXTERN = 2;
    private int curMode = -1;
    private String objectID;
    private String questID;

    private Animation mRotationAnimation;

    private ImageView mLoadingIndicator;
    private TextView mProgressIndicator;
    private RelativeLayout mInputWrapper;
    private TextView mCodeInputMessageTextField;
    private EditText mUserInputField;
    private ImageButton mSendButton;
    private LinearLayout mLoadingscreen;
    private WebView mWebview;
    private TextView mLoadingTextView;
    private View wrapper;
    protected static final float DIALOG_RATIO = 1.333333f;//4:3;

    private boolean askingForInput = false;
    private ArrayDeque<UserCodeParameterSatisfier> mUsercodeSatisfiers;

    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    public static ParameterLoadingFragment newInstanceForWebElement(String objectID, boolean inDialog) {
        ParameterLoadingFragment f = new ParameterLoadingFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_MODE, inDialog ? MODE_STORY_EXTERN : MODE_STORY_INTERN);
        args.putString(FRAGMENT_OBJECTID, objectID);
        args.putBoolean(FRAGMENT_IS_DIALOG,inDialog);
        f.setArguments(args);

        return f;
    }

    public static ParameterLoadingFragment newInstanceForJob(String jobID, String questID, boolean inDialog) {
        ParameterLoadingFragment f = new ParameterLoadingFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt(FRAGMENT_MODE, MODE_JOB);
        args.putString(FRAGMENT_OBJECTID, jobID);
        args.putString(FRAGMENT_QUESTID, questID);
        args.putBoolean(FRAGMENT_IS_DIALOG,inDialog);
        f.setArguments(args);

        return f;
    }

    private ParametersDataLoader parameterLoader;
    private boolean isDialog=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        curMode = getArguments().getInt(FRAGMENT_MODE, MODE_JOB);
        objectID = getArguments().getString(FRAGMENT_OBJECTID, "");
        questID = getArguments().getString(FRAGMENT_QUESTID,"");
        isDialog=getArguments().getBoolean(FRAGMENT_IS_DIALOG,false);
        if (curMode != MODE_JOB && curMode != MODE_STORY_EXTERN && curMode != MODE_STORY_INTERN)
            this.dismiss();
        mUsercodeSatisfiers = new ArrayDeque<>();
        parameterLoader = new ParametersDataLoader().withCallback(this).withDataProvider(this).withProgressListener(this);
        apiVersion = getResources().getInteger(R.integer.api_version);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        wrapper = inflater.inflate(R.layout.dialog_loading_parameter, container, false);
        if(isInDialog()) {
            ViewTreeObserver vto = wrapper.getViewTreeObserver();
            if (vto.isAlive()) {
                vto.addOnGlobalLayoutListener(this);
            }
        }
        mLoadingscreen = (LinearLayout) wrapper.findViewById(R.id.loadingscreen);
        mWebview = (WebView) wrapper.findViewById(R.id.webView);
        mWebview.getSettings().setJavaScriptEnabled(true);
        mLoadingIndicator = (ImageView) mLoadingscreen.findViewById(R.id.loadingIndicator);
        mProgressIndicator = (TextView) mLoadingscreen.findViewById(R.id.progressIndicatorText);
        mInputWrapper = (RelativeLayout) mLoadingscreen.findViewById(R.id.inputWrapper);
        mInputWrapper.setVisibility(View.GONE);
        mCodeInputMessageTextField = (TextView) mLoadingscreen.findViewById(R.id.codeInputText);
        mUserInputField = (EditText) mLoadingscreen.findViewById(R.id.userInputField);
        mSendButton = (ImageButton) mLoadingscreen.findViewById(R.id.sendButton);
        mLoadingTextView = (TextView) mLoadingscreen.findViewById(R.id.loadingTextView);
        mLoadingscreen.setVisibility(isInDialog()? View.VISIBLE: View.INVISIBLE);
        mLoadingTextView.setText(curMode == MODE_JOB ? getString(R.string.loading_job_infos) : getString(R.string.loading_story_infos));
        switch (curMode) {
            case MODE_JOB:
                loadJob(objectID);
                break;
            case MODE_STORY_EXTERN:
            case MODE_STORY_INTERN:
                loadWebElement(objectID);
                break;
        }
        return wrapper;
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        parameterLoader.cancel();
        final Activity activity = getActivity();
        if (activity != null && activity instanceof DialogFragmentDismissListener) {
            ((DialogFragmentDismissListener) activity).onDismissFragmentDialog(TAG+objectID);
        }
        super.onDismiss(dialog);
    }

    private int apiVersion;
    @Override
    public void onStart() {
        super.onStart();
        // safety check
        if (getActivity() == null) {
            return;
        }

        if(!isInDialog()){
            mLoadingscreen.setPadding(0,0,0, LayoutUtils.getPaddingBottom(getActivity()));
            mWebview.setPadding(0,0,0, LayoutUtils.getPaddingBottom(getActivity()));
            mWebview.setClipToPadding(false);
        }
        if(mRotationAnimation!=null) mRotationAnimation = AnimationUtils.loadAnimation(this.getActivity(), R.anim.infinite_rotation);
        mLoadingIndicator.setAnimation(mRotationAnimation);

    }

    public void setDialogRatio(float ratio){
        int mPhotoHeightPixels = (int) (wrapper.getWidth() / ratio);
        ViewGroup.LayoutParams lp;
        lp = wrapper.getLayoutParams();
        if (lp.height != mPhotoHeightPixels) {
            lp.height = mPhotoHeightPixels;
            wrapper.setLayoutParams(lp);
        }
    }

    private boolean isInDialog(){
        return isDialog;
    }

    private void loadJob(String objectID) {
        Jobs.findJobForIdInBackground(objectID, new GetCallback<Job>() {
            @Override
            public void done(Job job, ParseException e) {
                if(e==null) {
                    parameterLoader.forJob(job).load();
                    mLoadingscreen.setVisibility(parameterLoader.needsInput() || isInDialog() ? View.VISIBLE:View.INVISIBLE);
                }else{
                    LogUtils.e(TAG, "Error while loading Job",e);
                    mLoadingTextView.setText(getString(R.string.error_loading_informations));
                    mLoadingscreen.setVisibility(View.VISIBLE);
                    mLoadingTextView.setVisibility(View.VISIBLE);
                }
            }
        }, Job.JOB_PARAMETERS, Job.JOB_SHORT_DESCRIPTION);
    }

    private void loadWebElement(String objectID) {
        WebElements.findStoryForIdInBackground(objectID, new GetCallback<WebElement>() {
            @Override
            public void done(WebElement webElement, ParseException e) {
                if(e==null){
                    parameterLoader.forStoryElement(webElement).load();
                    mLoadingscreen.setVisibility(parameterLoader.needsInput() || isInDialog()? View.VISIBLE:View.INVISIBLE);
                }else{
                    LogUtils.e(TAG, "Error while loading WebElement",e);
                    mLoadingscreen.setVisibility(View.VISIBLE);
                    mLoadingTextView.setText(getString(R.string.error_loading_informations));
                    mLoadingTextView.setVisibility(View.VISIBLE);
                }

            }
        }, WebElement.STORYELEMENT_PARAMETER, Parameter.PARAMETER_USERMESSAGE);
    }

    private void setProgressText(int actual, int max) {
        double progress;
        if (max == 0) {
            progress = 100;
        } else {
            progress = ((double) actual / (double) max) * 100;
        }
        mProgressIndicator.setText("(" + actual + "|" + max + ") " + (int) progress + "%");
    }

    @Override
    public void onProgressChanged(int actual, int max) {
        setProgressText(actual, max);
    }


    private ViewLocalizer mLocalizer;

    public void loadUserInput(final UserCodeParameterSatisfier userCodeParameterSatisfier) {
        if (askingForInput) {
            mUsercodeSatisfiers.add(userCodeParameterSatisfier);
            return;
        }
        mInputWrapper.setVisibility(View.VISIBLE);
        if (userCodeParameterSatisfier.hasLocalizedMessage()) {
            if (mLocalizer != null) mLocalizer.cancel();
            mLocalizer = new ViewLocalizer(getActivity());
            mLocalizer.setLocalizedStringOnTextView(userCodeParameterSatisfier.getLocalizedMessage(), mCodeInputMessageTextField);
        } else {
            mCodeInputMessageTextField.setText(getString(R.string.please_enter_code));
        }
        mUserInputField.setText("");
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userCodeParameterSatisfier.setData(mUserInputField.getText().toString());
                if (mUsercodeSatisfiers.isEmpty()) {
                    mInputWrapper.setVisibility(View.GONE);
                } else {
                    loadUserInput(mUsercodeSatisfiers.removeFirst());
                }
                askingForInput = false;
            }
        });
        askingForInput = true;
    }


    @Override
    public void onAllDataLoadedForStoryElement(String url, String postParams) {
        if (curMode == MODE_STORY_EXTERN) {
            startActivity(StoryActivity.getIntent(url, postParams, this.getActivity()));
            this.dismiss();
        }else{
            mWebview.setVisibility(View.VISIBLE);
            mLoadingscreen.setVisibility(View.INVISIBLE);
            if(postParams!=null && !postParams.isEmpty()){
                mWebview.postUrl(url, EncodingUtils.getBytes(postParams, "BASE64"));
            }else{
                mWebview.loadUrl(url);
            }
        }

    }

    public void onAllDataLoadedForJob(final Job job, final HashMap<String, String> functionParams) {
        Jobs.finishJob(questID, job, functionParams, new JobFinishedResponse.JobFinishedCallback() {
            @Override
            public void done(JobFinishedResponse response) {
                if (!ParameterLoadingFragment.this.isAdded()) {
                    parameterLoader.cancel();
                    return;
                }
                if (response.hasError()) {
                    LogUtils.e(TAG, "Job finished failed with", response.getError());
                } else {
                    LogUtils.i(TAG, "Job finished");
                    if (response.hasAction()) {
                        OttoEventBusProvider.getInstanceForUIThread().post(new ActionReceivedEvent(response.getAction()));
                    } else {
                        LogUtils.i(TAG, "no action found");
                    }
                }
                ParameterLoadingFragment.this.dismiss();
            }
        });

    }

    Collection<Beacon> mBeacons = new ArrayList<>();

    @Override
    public void onGlobalLayout() {
        setDialogRatio(DIALOG_RATIO);
    }

    @Subscribe
    public void onBeaconInRangeEvent(BeaconInRangeEvent e) {
        this.mBeacons = e.beacons;
    }

    public void loadNextLocations(final NearLocationParameterSatisfier nearLocationParameterSatisfier) {
        Locations.findAllLocationsWithBeaconsInBackground(mBeacons, new FindCallback<Location>() {
            @Override
            public void done(List<Location> locations, ParseException e) {
                if (!ParameterLoadingFragment.this.isAdded()) {
                    parameterLoader.cancel();
                    return;
                }
                if (e == null) {
                    nearLocationParameterSatisfier.setData(locations);
                } else {
                    nearLocationParameterSatisfier.setData(new ArrayList<Location>());
                }
            }
        });
    }

    public void loadUserFraction(final UserFractionParameterSatisfier parameterSatisfier) {
        if(!mCurrentUser.hasFraction() || mCurrentUser.getFraction()==null){
            parameterSatisfier.setValue(null);
        }else {
            mCurrentUser.getFraction().fetchIfNeededInBackground(new GetCallback<Fraction>() {
                @Override
                public void done(Fraction fraction, ParseException e) {
                    if (!ParameterLoadingFragment.this.isAdded()) {
                        parameterLoader.cancel();
                        return;
                    }
                    if (e != null) {
                        parameterSatisfier.setValue(fraction);
                    } else {
                        parameterSatisfier.setValue(mCurrentUser.getFraction());
                    }
                }
            });
        }
    }

    public void loadTimestamp(TimestampParameterSatisfier timestampParameterSatisfier) {
        timestampParameterSatisfier.setValue(new Date());
    }

    public void loadUsername(final UsernameParameterSatisfier parameterSatisfier) {
        mCurrentUser.fetchIfNeededInBackground(new GetCallback<User>() {
            @Override
            public void done(User u, ParseException e) {
                if (e != null) {
                    parameterSatisfier.setValue(u.getUsername());
                } else {
                    parameterSatisfier.setValue(mCurrentUser.getUsername());
                }
            }
        });
    }

    public void loadUserEnergy(UserEnergyParameterSatisfier userEnergyParameterSatisfier) {
        userEnergyParameterSatisfier.setData(mCurrentUser.getEnergy());
    }

    public void loadUserPoints(UserPointsParameterSatisfier userPointsParameterSatisfier) {
        userPointsParameterSatisfier.setData(mCurrentUser.getUserPoints());
    }

    public void loadLocale(LocaleParameterSatisfier localeParameterSatisfier) {
        localeParameterSatisfier.setData(Locale.getDefault());
    }

    public void loadApiVersion(ApiVersionParameterSatisfier apiVersionParameterSatisfier) {
        apiVersionParameterSatisfier.setData(apiVersion);
    }



}
