package de.thmgames.s3.Views;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import de.thmgames.s3.Controller.ViewLocalizer;
import de.thmgames.s3.Model.ParseModels.Questsystem.Job;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.R;

/**
 * Created by Benedikt on 25.10.2014.
 */
public class JobMiniView extends RelativeLayout {

    private static final String TAG = JobMiniView.class.getName();
    private Context mContext;
    private Job mJob;
    private CircularImageView mJobLogoView;
    private TextView mJobTitleView;
    private TextView mJobDescView;
    private CheckBox mJobDoneBox;

    public JobMiniView(Context context) {
        super(context);
        setUp(context);
    }

    public JobMiniView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    public JobMiniView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public JobMiniView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setUp(context);
    }

    private void setUp(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        inflater.inflate(R.layout.job_mini_layout, this);
        mJobLogoView = (CircularImageView) findViewById(R.id.jobimage);
        mJobTitleView = (TextView) findViewById(R.id.jobtitle);
        mJobDescView = (TextView) findViewById(R.id.jobdesc);
        mJobDoneBox = (CheckBox) findViewById(R.id.jobdonebox);
        mJobDoneBox.setClickable(false);
        this.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.top_padding));
    }

    private ViewLocalizer localizer;
    public void setJob(final Job job) {
        if(job==null) return;
        job.fetchIfNeededInBackground(new GetCallback<Job>() {
            @Override
            public void done(Job parseObject, ParseException e) {
                mJob = parseObject;
                mJobDoneBox.setVisibility(View.VISIBLE);
                Picasso.with(mContext).cancelRequest(mJobLogoView);
                if (mJob.hasImage()) {
                    Picasso.with(mContext).load(mJob.getImage().getUrl()).placeholder(R.drawable.s3logoneuweb).into(mJobLogoView);
                }
                if(localizer!=null){
                    localizer.cancel();
                }
                localizer = new ViewLocalizer(mContext);
                localizer.setLocalizedStringOnTextView(mJob.getName(),mJobTitleView);
                localizer.setLocalizedStringOnTextView(mJob.getShortDescription(),mJobDescView);
            }
        });
    }

    public void setIsFinishedJob(){
        mJobDoneBox.setChecked(true);
        mJobTitleView.setTypeface(null, Typeface.NORMAL);
        mJobDescView.setTypeface(null, Typeface.NORMAL);
    }

    public void setIsActiveJob(){
        mJobTitleView.setTypeface(null, Typeface.BOLD);
        mJobDescView.setTypeface(null, Typeface.BOLD);
    }

    public void setIsOnNextList(){
        mJobTitleView.setTypeface(null, Typeface.NORMAL);
        mJobDescView.setTypeface(null, Typeface.NORMAL);
    }


}
