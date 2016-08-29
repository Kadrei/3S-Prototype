package de.thmgames.s3.Fragments;

import android.app.Activity;
import android.app.Fragment;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import de.thmgames.s3.BuildConfig;
import de.thmgames.s3.Otto.Events.LoadEvent;
import de.thmgames.s3.Otto.OttoEventBusProvider;
import de.thmgames.s3.Otto.Events.ShowSnackBarEvent;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.LogUtils;

public abstract class BaseFragment extends Fragment {

    private static final String TAG =BaseFragment.class.getName() ;
    protected Picasso mPicasso;
    protected int shortAnimTime;
    private FragmentEventHandler eventHandler;

    @Override
    public void onAttach (Activity activity){
        super.onAttach(activity);
        loadingCounter=0;
        if(this.getActivity()!=null)
        mPicasso = Picasso.with(activity);
        mPicasso.setIndicatorsEnabled(BuildConfig.DEBUG);
        mPicasso.setLoggingEnabled(BuildConfig.DEBUG);
        shortAnimTime  = getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    @Override
    public void onStart(){
        if(eventHandler==null) eventHandler=new FragmentEventHandler();
        eventHandler.register();
        super.onStart();
    }

    @Override
    public void onStop(){
        loadingCounter=0;
        eventHandler.unregister();
        super.onStop();
    }

    public void onLoadEvent(LoadEvent e){
        showLoadingProgress(e.finished);
    }

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
        LogUtils.e(TAG, "loading" + loadingCounter);
    }

    protected abstract void showLoadingAnimation();

    protected abstract void hideLoadingAnimation();

    public void showSnackBar(String text){
        if(getActivity()==null) return;
        OttoEventBusProvider.getInstanceForUIThread().post(new ShowSnackBarEvent(text));
    }

    public int getAPIVersion() {
        return getResources().getInteger(R.integer.api_version);
    }

    public class FragmentEventHandler{
        @Subscribe
        public void onLoadEvent(LoadEvent e){
            BaseFragment.this.onLoadEvent(e);
        }

        public void register(){
            OttoEventBusProvider.getInstance().register(this);
            OttoEventBusProvider.getInstanceForUIThread().register(this);
        }

        public void unregister(){
            OttoEventBusProvider.getInstance().unregister(this);
            OttoEventBusProvider.getInstanceForUIThread().unregister(this);
        }
    }
}
