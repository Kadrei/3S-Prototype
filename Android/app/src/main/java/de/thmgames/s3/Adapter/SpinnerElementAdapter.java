package de.thmgames.s3.Adapter;

import android.app.Activity;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import de.thmgames.s3.Model.ISpinnerElement;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.LayoutUtils;
import de.thmgames.s3.Controller.ViewLocalizer;

/**
 * Created by Benedikt on 24.02.2015.
 */
public class SpinnerElementAdapter extends ArrayAdapter<ISpinnerElement> {
    private ArrayList<ISpinnerElement> mapElements=new ArrayList<>();
    private Activity context;
    private final int minHeight;
    private float tenDP;

    public SpinnerElementAdapter(Activity context, int resource, ArrayList<ISpinnerElement> mapElements) {
        super(context, resource, mapElements);
        this.context = context;
        this.mapElements = mapElements;
        int[] attrs = new int[] { android.R.attr.listPreferredItemHeightSmall, android.R.attr.listPreferredItemPaddingStart, android.R.attr.listPreferredItemPaddingEnd};
        TypedArray ta = context.obtainStyledAttributes(attrs);
        tenDP = LayoutUtils.dpToPx(10, context);
        minHeight = (int) ta.getDimension(0, tenDP);
        ta.recycle();
    }

    public void setMapElements(ArrayList<ISpinnerElement> elements) {
        this.mapElements=elements;
        notifyDataSetChanged();
    }

    @Override
    public int getCount(){
        return mapElements.size();
    }

    @Override
    public ISpinnerElement getItem(int position){
        return mapElements.get(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return mapElements.get(position).hasParent();
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.spinner_list_item, parent, false);
        }else{
            ViewLocalizer localizer = (ViewLocalizer) convertView.getTag();
            localizer.cancel();
        }
        ISpinnerElement element =  mapElements.get(position);
        TextView mTextView = (TextView) convertView;
        if(element.hasParent()){
            mTextView.setTextColor(context.getResources().getColor(R.color.black));
            mTextView.setTextSize(18f);
            mTextView.setMinHeight(minHeight);

        }else{
            mTextView.setTextColor(context.getResources().getColor(R.color.accent));
            mTextView.setTextSize(12f);
            mTextView.setHeight(LayoutUtils.dpToPx(0, context));
        }
        ViewLocalizer localizer = new ViewLocalizer(context);
        localizer.setLocalizedStringOnTextView(element.getTitle() ,mTextView);
        convertView.setTag(localizer);
        return convertView;
    }

}
