package de.thmgames.s3.Views;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.squareup.picasso.Picasso;

import de.thmgames.s3.Controller.ViewLocalizer;
import de.thmgames.s3.Model.ParseModels.Questsystem.Job;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.R;

/**
 * Created by Benedikt on 08.12.2014.
 */
public class JobDetailsView extends CardView {

    private static final String TAG = JobDetailsView.class.getName();
    private Context mContext;
    private Job mJob;
    private JobMiniView mJobMiniView;
    private TextView mDescriptionView;

    public JobDetailsView(Context context) {
        super(context);
        setUp(context);
    }

    public JobDetailsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    public JobDetailsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUp(context);
    }

    private void setUp(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.job_details_view, this);
        mJobMiniView = (JobMiniView) this.findViewById(R.id.jobminiview);
        mDescriptionView= (TextView) this.findViewById(R.id.jobLongDesc);
        this.mContext = context;

    }

    public void setJob(final Job job) {
        job.fetchWhereAvailable(new GetCallback<Job>() {
            @Override
            public void done(Job parseObject, ParseException e) {
                mJob = parseObject;
                mJobMiniView.setJob(mJob);
                ViewLocalizer loc = new ViewLocalizer(mContext);
                loc.setLocalizedStringOnTextView(mJob.getDescription(), mDescriptionView);

            }
        });
    }

    public void setIsFinishedJob(){
        mJobMiniView.setIsFinishedJob();
    }

    public void setIsActiveJob(){
        mJobMiniView.setIsActiveJob();
    }

    public void setIsOnNextList(){
        mJobMiniView.setIsOnNextList();
    }
}
