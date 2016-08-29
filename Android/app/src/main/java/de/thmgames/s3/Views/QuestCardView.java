package de.thmgames.s3.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.SaveCallback;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.thmgames.s3.Controller.ViewLocalizer;
import de.thmgames.s3.Model.ParseModels.Questsystem.Job;
import de.thmgames.s3.Model.ParseModels.Questsystem.Quest;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.R;
import fr.castorflex.android.circularprogressbar.CircularProgressBar;

/**
 * Created by Benedikt on 24.10.2014.
 */
public class QuestCardView extends FrameLayout implements View.OnClickListener {

    private static final String TAG = QuestCardView.class.getName();
    private Context mContext;
    private UserQuestRelation mQuestRel;
    private CircularImageView mQuestLogoView;
    private TextView mQuestTitleView;
    private ImageButton mOptionButton;
    private LinearLayout mJobWrapper;
    private CircularProgressBar mProgress;
    private LinearLayout mQuestWrapper;

    public QuestCardView(Context context) {
        super(context);
        setUp(context);
    }

    public QuestCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp(context);
    }

    public QuestCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    private void setUp(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        this.mContext = context;
        inflater.inflate(R.layout.quest_card_layout, this);
        mProgress = (CircularProgressBar) findViewById(R.id.cardLoadProgress);
        mQuestWrapper = (LinearLayout) findViewById(R.id.questwrapper);
        mQuestLogoView = (CircularImageView) findViewById(R.id.questimage);
        mQuestTitleView = (TextView) findViewById(R.id.questtitle);
        mOptionButton = (ImageButton) findViewById(R.id.optionbutton);
        mJobWrapper = (LinearLayout) findViewById(R.id.questjobwrapper);
    }

    private Quest mQuest;
    private ViewLocalizer mViewLocalizer;
    public void setUserQuestRelation(UserQuestRelation questRel) {
        mQuestRel = questRel;
        questRel.getQuest().fetchIfNeededInBackground(new GetCallback<Quest>() {
            @Override
            public void done(Quest quest, ParseException e) {
                mQuest = quest;

                if (!mQuest.isValidFor(new Date())) {
                    QuestCardView.this.setVisibility(View.GONE);
                    return;
                }

                Picasso.with(mContext).cancelRequest(mQuestLogoView);
                mProgress.setVisibility(View.GONE);
                mOptionButton.setVisibility(mQuestRel.hasActiveJob() ? VISIBLE : GONE);
                mQuestWrapper.setVisibility(View.VISIBLE);
                if(mViewLocalizer!=null){
                    mViewLocalizer.cancel();
                }
                mViewLocalizer = new ViewLocalizer(mContext);
                mViewLocalizer.setLocalizedStringOnTextView(mQuest.getName(), mQuestTitleView);

                if (mQuest.hasMiniImage()) {
                    Picasso.with(mContext).load(mQuest.getMiniImage().getUrl()).placeholder(R.drawable.placeholder_quest_mini).into(mQuestLogoView);
                }

                mOptionButton.setOnClickListener(QuestCardView.this);

                mJobWrapper.removeAllViews();
                boolean hasFinishedJobs=false;
                boolean hasActiveJob=false;
                if (mQuestRel.getFinishedJobs() != null) {
                    for (Job job : mQuestRel.getFinishedJobs()) {
                        JobMiniView jobView = new JobMiniView(mContext);
                        jobView.setJob(job);
                        jobView.setIsFinishedJob();
                        mJobWrapper.addView(jobView);
                        hasFinishedJobs=true;
                    }
                }

                if (mQuestRel.getActiveJob() != null) {
                    JobMiniView activeJobView = new JobMiniView(mContext);
                    activeJobView.setJob(mQuestRel.getActiveJob());
                    activeJobView.setIsActiveJob();
                    mJobWrapper.addView(activeJobView);
                    hasActiveJob=true;
                }

                if (mQuestRel.getNextJobs() != null) {
                    for (Job job : mQuestRel.getNextJobs()) {
                        JobMiniView nextJobView = new JobMiniView(mContext);
                        nextJobView.setJob(job);
                        nextJobView.setIsOnNextList();
                        mJobWrapper.addView(nextJobView);
                    }
                }
                if(hasFinishedJobs && hasActiveJob){
                    mOptionButton.setVisibility(View.VISIBLE);
                }else{
                    mOptionButton.setVisibility(View.GONE);
                }

                mJobWrapper.setVisibility(View.VISIBLE);

                show();
            }
        });

    }

    private boolean showCancelled = true;

    public void showCancelled(boolean filter) {
        showCancelled = filter;
    }

    private void show() {
       this.setVisibility(!showCancelled && mQuestRel.isCancelled() ? View.GONE : View.VISIBLE);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.optionbutton) {
            PopupMenu popupMenu = new PopupMenu(mContext, v);
            popupMenu.inflate(R.menu.questcard_menu);
            MenuItem item = popupMenu.getMenu().getItem(0);
            item.setTitle(mQuestRel.isCancelled() ? mContext.getString(R.string.uncancel) : mContext.getString(R.string.cancel));
            popupMenu.show();
            this.setClickable(false);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.quest_cancel) {
                        mQuestRel.cancel(!mQuestRel.isCancelled()).saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                show();
                            }
                        });
                    }
                    return false;
                }
            });
        }
    }
}
