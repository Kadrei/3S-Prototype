package de.thmgames.s3.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.squareup.otto.Subscribe;

import org.apache.http.util.EncodingUtils;

import de.thmgames.s3.Model.ParseModels.ActionSystem.Action;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.Otto.Events.ActionReceivedEvent;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.LayoutUtils;
import de.thmgames.s3.Utils.LogUtils;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class StoryActivity extends AbstractBaseActivity {
    private final static String TAG = FractionChooserActivity.class.getName();
    private final static String INTENT_URL="storyurl";
    private final static String INTENT_POST_PARAMETERS="postParam";


    public static Intent getIntent(String url, String postParam, Context ctx) {
        Intent i = new Intent(ctx, StoryActivity.class);
        i.putExtra(INTENT_URL, url);
        i.putExtra(INTENT_POST_PARAMETERS, postParam);
        return i;
    }

    private WebView mStoryView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        lookForBeacons(false);
        needInternet(true);
        askForPushEnablingIfNotAskedBefore(false);
        setNeededUserState(User.USERSTATE.LOGGEDIN);
        checkUserState(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setPadding(0,LayoutUtils.getPaddingTopWithoutActionBar(this),0, 0);
        mActionBarBackgroundDrawable = mToolbar.getBackground();
        mActionBarBackgroundDrawable.setAlpha(255);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        LinearLayout mStoryViewWrapper = (LinearLayout) this.findViewById(R.id.storyViewWrapper);
        mStoryViewWrapper.setPadding(0,0,0, LayoutUtils.getPaddingBottom(this));
        mStoryView = (WebView) this.findViewById(R.id.storyView);
        mStoryView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                StoryActivity.this.setTitle(view.getTitle());
            }
        });
        loadData();
    }

    @Override
    protected void onCorrectUserState() {

    }

    public void loadData(){
        if(getIntent()==null) this.finish();
        Intent i = getIntent();
        if(!i.hasExtra(INTENT_URL)) this.finish();
        String url = i.getStringExtra(INTENT_URL);
        if(i.hasExtra(INTENT_POST_PARAMETERS)){
            LogUtils.i(TAG, "loading url with post params:"+i.getStringExtra(INTENT_POST_PARAMETERS));
            mStoryView.postUrl(url, EncodingUtils.getBytes(i.getStringExtra(INTENT_POST_PARAMETERS), "BASE64"));
        }else{
            mStoryView.loadUrl(url);
        }
    }


    @Override
    public void onSnackbarShown(int height) {

    }

    @Override
    public void onSnackbarDismissed(int height) {

    }

    @Override
    public void showLoadingProgress(boolean show) {

    }

    @Override
    protected void showLoadingAnimation() {

    }

    @Override
    protected void hideLoadingAnimation() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
