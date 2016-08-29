package de.thmgames.s3.Views;


/*
* Copyright (C) 2012 Surviving with Android (http://www.survivingwithandroid.com)
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
import java.util.List;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;

import de.thmgames.s3.Adapter.QuestListAdapter;
import de.thmgames.s3.Model.ParseModels.Questsystem.UserQuestRelation;

public class EndlessQuestListView extends ListView implements OnScrollListener {
    private View footer;
    private boolean isLoading;
    private EndlessListener listener;
    private QuestListAdapter adapter;
    private boolean hasAllData=false;

    public EndlessQuestListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setOnScrollListener(this);
    }
    public EndlessQuestListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setOnScrollListener(this);
    }
    public EndlessQuestListView(Context context) {
        super(context);
        this.setOnScrollListener(this);
    }

    public void setListener(EndlessListener listener) {
        this.listener = listener;
    }

    public void setHasAllData(boolean hasAllData){
        this.hasAllData=hasAllData;
    }

    public boolean getHasAllData(){
        return hasAllData;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        if (getAdapter() == null)
            return ;
        if (getAdapter().getCount() == 0)
            return ;
        if(hasAllData){
            isLoading=false;
            this.removeFooterView(footer);
            return;
        }
        int l = visibleItemCount + firstVisibleItem;
        if (l >= totalItemCount && !isLoading) {
            // It is time to add new data. We call the listener
            this.addFooterView(footer);
            isLoading = true;
            listener.loadData();
        }
    }
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}

    public void setLoadingView(int resId) {
        LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        footer = (View) inflater.inflate(resId, null);
        this.addFooterView(footer);
    }

    public void setAdapter(QuestListAdapter adapter) {
        super.setAdapter(adapter);
        this.adapter = adapter;
        this.removeFooterView(footer);
    }


    public void setDataFromQuestUserRel(List<UserQuestRelation> data) {
        this.removeFooterView(footer);
        adapter.setData(data);
        adapter.notifyDataSetChanged();
        isLoading = false;
    }


    public void addNewDataFromQuestUserRel(List<UserQuestRelation> data) {
        this.removeFooterView(footer);
        if(data==null || adapter==null) return;
        adapter.addData(data);
        adapter.notifyDataSetChanged();
        isLoading = false;
    }

    public EndlessListener getListener() {
        return listener;
    }

    public static interface EndlessListener {
        public void loadData() ;
    }
}