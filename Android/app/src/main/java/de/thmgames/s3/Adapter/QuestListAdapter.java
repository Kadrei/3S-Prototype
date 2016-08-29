package de.thmgames.s3.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;
import de.thmgames.s3.Views.QuestCardView;

/**
 * Created by Benedikt on 25.10.2014.
 */
public class QuestListAdapter extends BaseAdapter {
    private Context context;
    private List<UserQuestRelation> questRels = new ArrayList<UserQuestRelation>();
    private boolean showCancelled = false;


    public QuestListAdapter(Context context, List<UserQuestRelation> questRels) {
        this.context = context;
        this.questRels = questRels;
    }

    public void showCancelled(boolean show){
        showCancelled =show;
    }

    public QuestListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public boolean isEnabled(int position)
    {
        return true;
    }

    public void setData(List<UserQuestRelation> quests){
        this.questRels =quests;
        this.notifyDataSetChanged();
    }

    public void removeData(UserQuestRelation questRel){
        questRels.remove(questRel);
        this.notifyDataSetChanged();
    }

    public void addData(List<UserQuestRelation> questRels){
        this.questRels.addAll(questRels);
        this.notifyDataSetChanged();
    }

    public void addData(UserQuestRelation questRels, int pos){
        this.questRels.add(pos, questRels);
        this.notifyDataSetChanged();
    }

    @Override
    public boolean isEmpty(){
        return questRels==null || questRels.isEmpty();
    }

    @Override
    public long getItemId(int position) {
        return questRels.get(position).hashCode();
    }

    @Override
    public int getCount() {
        return questRels.size();
    }

    @Override
    public UserQuestRelation getItem(int position) {
        return questRels.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new QuestCardView(context);
        }
        ((QuestCardView) convertView).setUserQuestRelation(questRels.get(position));
        ((QuestCardView) convertView).showCancelled(showCancelled);
        return convertView;
    }

}
