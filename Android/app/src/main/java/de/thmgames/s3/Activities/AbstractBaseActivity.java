package de.thmgames.s3.Activities;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.parse.ConfigCallback;
import com.parse.GetCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.SaveCallback;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IllegalFormatCodePointException;
import java.util.Locale;

import de.thmgames.s3.BuildConfig;
import de.thmgames.s3.Controller.BeaconController;
import de.thmgames.s3.Controller.Installations;
import de.thmgames.s3.Controller.Users;
import de.thmgames.s3.Controller.WebElements;
import de.thmgames.s3.Fragments.DialogFragments.LocationActionFragment;
import de.thmgames.s3.Fragments.DialogFragments.ParameterLoadingFragment;
import de.thmgames.s3.Listener.DialogFragmentDismissListener;
import de.thmgames.s3.Model.ParseModels.ActionSystem.Action;
import de.thmgames.s3.Model.ParseModels.Config;
import de.thmgames.s3.Model.ParseModels.LocalizedString;
import de.thmgames.s3.Model.ParseModels.LocationSystem.Location;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.Model.ParseModels.WebElement;
import de.thmgames.s3.Otto.Events.ActionReceivedEvent;
import de.thmgames.s3.Otto.Events.BeaconInRangeEvent;
import de.thmgames.s3.Otto.Events.LocationClickedEvent;
import de.thmgames.s3.Otto.Events.ShowSnackBarEvent;
import de.thmgames.s3.Otto.OttoEventBusProvider;
import de.thmgames.s3.R;
import de.thmgames.s3.Receiver.Broadcast.BluetoothStateReceiver;
import de.thmgames.s3.Utils.AndroidUtils;
import de.thmgames.s3.Utils.LayoutUtils;
import de.thmgames.s3.Utils.LogUtils;
import de.thmgames.s3.Views.Widgets.SnackBar;
import uk.me.lewisdeane.ldialogs.CustomDialog;


public abstract class AbstractBaseActivity extends ActionBarActivity implements BluetoothStateReceiver.BluetoothStateChangeListener,
        SnackBar.EventListener, DialogFragmentDismissListener,
        Action.ActionResolver {
    protected static final String TAG = AbstractBaseActivity.class.getName();

    protected Toolbar mToolbar;
    protected Drawable mActionBarBackgroundDrawable;
    protected HashMap<String, Boolean> mDialogFragmentsVisibilityStates = new HashMap<>();

    protected User mCurrentUser;

    protected BeaconController mBeaconController;
    protected static final String KEY_BLUETOOTH_CANCELLED = "bluetooth.cancelled";
    protected static final String KEY_WLAN_DIALOG_SHOWN = "wlan.dialog.shown";
    protected static final String KEY_PUSH_DIALOG_SHOWN = "push.dialog.shown";
    protected BluetoothStateReceiver mBluetoothStateReceiver;
    protected boolean mWlanDialogShown = false;
    protected boolean mPushDialogShown = false;
    protected boolean mBluetoothCancelled = false;
    protected boolean lookForBeacons = false;
    protected boolean needInternet = false;
    private boolean askForPush = false;
    protected Collection<Beacon> mBeacons = new ArrayList<>();
    protected Picasso mPicasso;
    protected static final float HEADER_ASPECT_RATIO = 1.7777777f;//16:9
    protected boolean checkUserState = false;
    protected ActivityEventHandler mEventHandler;
    protected User.USERSTATE neededUserState;
    private SharedPreferences mPrefs;

    protected int shortAnimTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restoreSavedInstanceState(savedInstanceState);
        loadConfig();
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mPicasso = Picasso.with(this);
        mPicasso.setIndicatorsEnabled(BuildConfig.DEBUG);
        mPicasso.setLoggingEnabled(BuildConfig.DEBUG);
        mEventHandler = new ActivityEventHandler();
    }

    @Override
    protected void onResume() {
        if(!(checkUserState&& !checkUserStateIsNeededUserState())){ //Against Window Leaks
            if (checkUserState && neededUserState != null) checkUserStateIsNeededUserState();
            if (lookForBeacons) enableOrAskForBluetoothIfNeeded();
            if (needInternet) enableOrAskForNetworkIfNeeded();
            if(askForPush && !mPrefs.contains(KEY_PUSH_DIALOG_SHOWN)) showPushDialog();
        }
        mCurrentUser = Users.getCurrentS3User();
        updateUser();
        LogUtils.enableGalgo(this);
        mToolbar.setPadding(0, LayoutUtils.getPaddingTopWithoutActionBar(this), 0, 0);
        mEventHandler.register();
        super.onResume();
    }

    @Override
    protected void onStart() {
        if (lookForBeacons) {
            mBeaconController = BeaconController.getInstance(this.getApplicationContext());
            startBluetoothStateReceiver();
        }
        super.onStart();
    }



    @Override
    protected void onPause() {
        mEventHandler.unregister();
        LogUtils.disableGalgo(this);
        if (lookForBeacons) stopBeaconListener();

        super.onPause();
    }

    @Override
    public void onStop() {
        mPicasso.cancelTag(TAG);
        if (lookForBeacons) stopBluetoothStateReceiver();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean(KEY_BLUETOOTH_CANCELLED, mBluetoothCancelled);
        savedInstanceState.putBoolean(KEY_WLAN_DIALOG_SHOWN, mWlanDialogShown);
    }

    protected void setNeededUserState(User.USERSTATE neededUserState){
        this.neededUserState= neededUserState;
    }

    protected boolean checkUserStateIsNeededUserState(){
        if(neededUserState==getUserState()){
            onCorrectUserState();
            return true;
        }
        switch(getUserState()){
            case NOTLOGGEDIN:
                startActivity(SignUpOrInActivity.getIntent(this, false));
                this.finish();
                return false;
            case MISSING_FRACTION:
                startActivity(FractionChooserActivity.getIntent(this));
                this.finish();
                return false;
            case LOGGEDIN:
            default:
                startActivity(MainActivity.getIntent(this));
                this.finish();
                return false;
        }
    }

    protected abstract void onCorrectUserState();

    protected void onUserUpdated(User user) {
        mCurrentUser = user;
    }

    protected void updateUser() {
        if (mCurrentUser != null) {
            mCurrentUser.fetchInBackground(new GetCallback<User>() {
                @Override
                public void done(User parseObject, ParseException e) {
                    onUserUpdated(parseObject);
                }
            });
        }
    }

    protected void askForPushEnablingIfNotAskedBefore(boolean b){
        askForPush=b;
    }

    protected void lookForBeacons(boolean b) {
        lookForBeacons = b;
    }

    protected void needInternet(boolean b) {
        needInternet = b;
    }

    protected User.USERSTATE getUserState() {
        return Users.getUserStateFromUser(Users.getCurrentS3User());
    }

    protected void checkUserState(boolean check) {
        this.checkUserState = check;
    }

    protected ParseConfig mConfig;
    protected boolean configLoaded = false;

    protected void onConfigLoaded(ParseConfig config) {
        if (config == null) return;
        LogUtils.i(TAG, "Got Config");
        configLoaded = true;
        mConfig = config;
        if (mBeaconController != null) {
            mBeaconController.addUUIDs(config.getList(Config.CONFIG_PROXIMITY_UUIDS, new ArrayList<String>()));
            mBeaconController.addBeaconLayouts(config.getList(Config.CONFIG_BEACONLAYOUTS, new ArrayList<String>()));
            if (lookForBeacons) mBeaconController.notifyBeaconControllerIsSetup();
        }
    }


    protected void loadConfig() {
        if (configLoaded && mConfig != null) {
            onConfigLoaded(mConfig);
            return;
        }

        ParseConfig.getInBackground(new ConfigCallback() {
            @Override
            public void done(ParseConfig config, ParseException e) {
                if (e != null) {
                    LogUtils.e(TAG, "Error while downloading Config", e);
                    showSnackBar(getString(R.string.error_downloading_config));
                }
                onConfigLoaded(config);
            }
        });
    }

    protected void startBluetoothStateReceiver() {
        LogUtils.i(TAG, "start BluetoothStateReceiver");
        if (mBluetoothStateReceiver == null) mBluetoothStateReceiver = new BluetoothStateReceiver();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothStateReceiver, filter);
        mBluetoothStateReceiver.setBluetoothStateChangeListener(this);
    }

    protected void stopBluetoothStateReceiver() {
        LogUtils.i(TAG, "stop BluetoothStateReceiver");
        if (mBluetoothStateReceiver == null) return;
        unregisterReceiver(mBluetoothStateReceiver);
        mBluetoothStateReceiver.setBluetoothStateChangeListener(null);
    }

    protected void enableOrAskForBluetoothIfNeeded() {
        if (AndroidUtils.isBluetoothAvailable(this)) {
            startBeaconListener();
        } else {
            if (!mBluetoothCancelled) showBluetoothDialog();
        }
    }

    protected void enableOrAskForNetworkIfNeeded() {
        if (!AndroidUtils.isWifiConnected(this)) {
            if (!mWlanDialogShown) showWifiDialog();
        }
    }

    protected void restoreSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mBluetoothCancelled = savedInstanceState.getBoolean(KEY_BLUETOOTH_CANCELLED, false);
            mWlanDialogShown = savedInstanceState.getBoolean(KEY_WLAN_DIALOG_SHOWN, false);
        }
    }

    public void onBeaconInRangeEvent(final BeaconInRangeEvent e) {
        onBeaconInRange(e.beacons);
    }

    public void onShowSnackBarEvent(ShowSnackBarEvent e) {
        showSnackBar(e.message);
    }

    private boolean bluetoothDialogShowing = false;

    protected void showBluetoothDialog() {
        if (bluetoothDialogShowing) return;
        CustomDialog.Builder builder = new CustomDialog.Builder(this, getString(R.string.dialog_bluetooth_title), getString(R.string.activate));
        builder.content(getString(R.string.activate_bluetooth_dialog_text));
        builder.negativeText(getString(R.string.cancel));
        builder.negativeColor(Color.RED);
        builder.positiveColor(getResources().getColor(R.color.bluetooth));
        final CustomDialog customDialog = builder.build();
        customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                bluetoothDialogShowing = false;
            }
        });
        customDialog.setClickListener(new CustomDialog.ClickListener() {
            @Override
            public void onConfirmClick() {
                customDialog.dismiss();
                AndroidUtils.enableBluetooth(true);
                mBeaconController.startSearchingIfNeeded();
            }

            @Override
            public void onCancelClick() {
                customDialog.dismiss();
                mBluetoothCancelled = true;
            }
        });
        customDialog.show();
        bluetoothDialogShowing = true;
    }

    private boolean wifiDialogShowing = false;

    protected void showWifiDialog() {
        if (wifiDialogShowing) return;
        CustomDialog.Builder builder = new CustomDialog.Builder(this, getString(R.string.missing_wlan_connection), getString(R.string.activate));
        builder.content(getString(R.string.activate_wlan_dialog_text));
        builder.negativeText(getString(R.string.cancel));
        builder.negativeColor(Color.RED);
        builder.positiveColor(getResources().getColor(R.color.black));
        final CustomDialog customDialog = builder.build();
        customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                wifiDialogShowing = false;
            }
        });
        customDialog.setClickListener(new CustomDialog.ClickListener() {
            @Override
            public void onConfirmClick() {
                customDialog.dismiss();
                AndroidUtils.enableWifi(true, AbstractBaseActivity.this);
                mWlanDialogShown = true;
                recreate();
            }

            @Override
            public void onCancelClick() {
                customDialog.dismiss();
                mWlanDialogShown = true;
            }
        });
        customDialog.show();
        wifiDialogShowing = true;
    }

    protected void showPushDialog() {
        if (mPushDialogShown) return;
        CustomDialog.Builder builder = new CustomDialog.Builder(this, "Pushnachrichten", getString(R.string.activate));
        builder.content("Wollen Sie Push aktivieren, um über Neuigkeiten rund um 3S informiert zu werden?");
        builder.negativeText(getString(R.string.cancel));
        builder.negativeColor(Color.RED);
        builder.positiveColor(getResources().getColor(R.color.black));
        final CustomDialog customDialog = builder.build();
        customDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mPushDialogShown = false;
            }
        });
        customDialog.setClickListener(new CustomDialog.ClickListener() {
            @Override
            public void onConfirmClick() {
                mPrefs.edit().putBoolean(getString(R.string.key_push_sp), true).putBoolean(KEY_PUSH_DIALOG_SHOWN, true).apply();
                customDialog.dismiss();
                try {
                    Installations.setPushForBroadcastChannelEnabled(true, new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                LogUtils.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                            } else {
                                LogUtils.e("com.parse.push", "failed to subscribe for push", e);
                                showSnackBar(getString(R.string.error_activating_push));
                            }
                        }
                    });
                }catch (IllegalArgumentException e){
                    showSnackBar(getString(R.string.error_activating_push));
                }
                mPushDialogShown = true;
            }

            @Override
            public void onCancelClick() {
                mPrefs.edit().putBoolean(getString(R.string.key_push_sp), false).putBoolean(KEY_PUSH_DIALOG_SHOWN, true).apply();
                customDialog.dismiss();
                try {
                Installations.setPushForBroadcastChannelEnabled(false, new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            LogUtils.d("com.parse.push", "successfully unsubscribed to the broadcast channel.");
                        } else {
                            LogUtils.e("com.parse.push", "failed to unsubscribe for push", e);
                            showSnackBar(getString(R.string.error_unsubscribing_push));
                        }
                    }
                });
                }catch (IllegalArgumentException e){
                    showSnackBar(getString(R.string.error_activating_push));
                }
                mPushDialogShown = true;
            }
        });
        customDialog.show();
        mPushDialogShown = true;

    }

    public void onDismissFragmentDialog(String fragmentTag) {
        mDialogFragmentsVisibilityStates.put(fragmentTag, false);
        updateUser();
    }

    protected final String LocationDialogPrefixTag = "locationdialog_";
    public void showLocationDialog(final Location l) {
        if (l == null || (mDialogFragmentsVisibilityStates.containsKey(LocationActionFragment.TAG) && mDialogFragmentsVisibilityStates.get(LocationActionFragment.TAG)))
            return;
        boolean inRange = false;
        for (Beacon bcn : mBeacons) {
            if (l.isLocationForBeacon(bcn)) {
                inRange = true;
                break;
            }
        }
        mDialogFragmentsVisibilityStates.put(LocationActionFragment.TAG, true);
        LocationActionFragment newFragment = LocationActionFragment.newInstance(l.getObjectId(), inRange, LocationActionFragment.FRAGMENT_CHOOSEMODE);
        newFragment.show(getFragmentManager(), "locationdialog_" + l.getObjectId());
    }

    public void showParameterLoadingDialogForWebElement(WebElement e) {
        if(e==null) return;
        String dialogTag =ParameterLoadingFragment.TAG+e.getObjectId();
        if ((mDialogFragmentsVisibilityStates.containsKey(dialogTag) && mDialogFragmentsVisibilityStates.get(dialogTag)))
            return;
        mDialogFragmentsVisibilityStates.put(dialogTag, true);
        ParameterLoadingFragment newFragment = ParameterLoadingFragment.newInstanceForWebElement(e.getObjectId(), true);
        newFragment.show(getFragmentManager(), "storyloadingdialog_" + e.getObjectId());
    }

    protected final String ParameterLoadingDialogForJobPrefixTag = "jobloadingdialog_";
    public void showParameterLoadingDialogForJob(UserQuestRelation rel) {
        if(rel.getActiveJob()==null) return;
        String dialogTag =ParameterLoadingFragment.TAG+rel.getActiveJob().getObjectId();
        if ((mDialogFragmentsVisibilityStates.containsKey(dialogTag) && mDialogFragmentsVisibilityStates.get(dialogTag)))
            return;
        mDialogFragmentsVisibilityStates.put(dialogTag, true);
        ParameterLoadingFragment newFragment = ParameterLoadingFragment.newInstanceForJob(rel.getActiveJob().getObjectId(),rel.getQuestId(), true);
        newFragment.show(getFragmentManager(), ParameterLoadingDialogForJobPrefixTag + rel.getActiveJob().getObjectId());
    }


    @Override
    public void onShow(int height){
        this.onSnackbarShown(height);
    }

    @Override
    public void onDismiss(int height){
        this.onSnackbarDismissed(height);
    }

    abstract public void onSnackbarShown(int height);

    abstract public void onSnackbarDismissed(int height);


    protected void onBeaconInRange(Collection<Beacon> beacons) {
        this.mBeacons = beacons;
    }


    public void showSnackBar(String text) {
        if (this.isFinishing()) return;
        SnackBar.with(getApplicationContext())
                .text(text)
                .withPadding(0, 0, 0, (LayoutUtils.getPaddingBottom(this)))
                .eventListener(this)
                .show(this);
    }

    @Override
    public void onTurningOff() {
    }

    @Override
    public void onTurningOn() {
    }

    @Override
    public void onOn() {
        if (lookForBeacons) startBeaconListener();
    }

    @Override
    public void onOff() {
        if (lookForBeacons) stopBeaconListener();
    }

    protected void startBeaconListener() {
        mBeacons = new ArrayList<>();
        mBeaconController.startSearchingIfNeeded();
    }

    protected void stopBeaconListener() {
        mBeaconController.stopSearching();
        mBeacons = new ArrayList<>();
    }

    public void tryFinishActiveJobInQuestUserRel(final UserQuestRelation rel) {
        if (!rel.hasActiveJob()) return;
        showParameterLoadingDialogForJob(rel);
    }

    public void loadStoryElement(WebElement e) {
        showParameterLoadingDialogForWebElement(e);
    }

    protected Locale mCurrentLocale = Locale.getDefault();

    protected volatile int loadingCounter = 0;
    public void showLoadingProgress(boolean show){
        if(show){
            loadingCounter++;
        }else{
            loadingCounter--;
            if(loadingCounter<0) loadingCounter=0;
        }
        if(loadingCounter==0){
            hideLoadingAnimation();
        }else{
            showLoadingAnimation();
        }
    }

    protected abstract void showLoadingAnimation();

    protected abstract void hideLoadingAnimation();

    public void onActionReceived(ActionReceivedEvent actionevent){
        actionevent.action.resolveAction(this);
    }

    public void onActionInvalid(String information){
        LogUtils.e(TAG, "Invalid Action received with information: " + information);
    }

    public void onActionShowMessageDialog(String messageId){
        LogUtils.i(TAG, "Show Message Dialog for " + messageId);
        LocalizedString.createWithoutData(LocalizedString.class, messageId).fetchWhereAvailable(new GetCallback<LocalizedString>() {
            @Override
            public void done(LocalizedString string, ParseException e) {
                if(e==null) {
                    CustomDialog.Builder builder = new CustomDialog.Builder(AbstractBaseActivity.this, "", getString(R.string.ok));
                    builder.content(string.getMessageForLocale(mCurrentLocale));
                    CustomDialog customDialog = builder.build();
                    customDialog.show();
                }else{
                    LogUtils.e(TAG, "error while loading Message after action",e);
                    showSnackBar(getString(R.string.error_loading_message));
                }
            }
        });
    }

    public void onActionShowWebElement(String storyId){
        LogUtils.i(TAG, "Show Web Element for " + storyId);
        WebElements.findStoryForIdInBackground(storyId,new GetCallback<WebElement>() {
            @Override
            public void done(WebElement webElement, ParseException e) {
                if(e==null){
                    loadStoryElement(webElement);
                }else{
                    LogUtils.e(TAG, "error while loading webelement after action",e);
                    showSnackBar(getString(R.string.error_loading_webelement));
                }
            }
        });
    }

    public class ActivityEventHandler {
        public void register(){
            OttoEventBusProvider.getInstance().register(this);
            OttoEventBusProvider.getInstanceForUIThread().register(this);
        }

        public void unregister(){
            OttoEventBusProvider.getInstance().unregister(this);
            OttoEventBusProvider.getInstanceForUIThread().unregister(this);
        }

        @Subscribe
        public void onBeaconInRangeEvent(BeaconInRangeEvent e) {
            AbstractBaseActivity.this.onBeaconInRange(e.beacons);
        }


        @Subscribe
        public void onShowSnackBarEvent(ShowSnackBarEvent e) {
            AbstractBaseActivity.this.showSnackBar(e.message);
        }

        @Subscribe
        public void onLocationClickedListener(LocationClickedEvent e) {
            AbstractBaseActivity.this.showLocationDialog(e.location);
        }

        @Subscribe
        public void onActionReceived(ActionReceivedEvent action) { AbstractBaseActivity.this.onActionReceived(action); }
    }

}
