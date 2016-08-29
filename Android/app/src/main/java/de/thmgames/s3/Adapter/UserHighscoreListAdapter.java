package de.thmgames.s3.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.thmgames.s3.Controller.ViewLocalizer;
import de.thmgames.s3.Model.ParseModels.Fraction;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.R;

/**
 * Created by Benedikt on 28.10.2014.
 */
public class UserHighscoreListAdapter extends BaseAdapter {
    private Context context;
    private List<User> userdata = new ArrayList<User>();

    public UserHighscoreListAdapter(Context context, List<User> highscorelist) {
        this.context = context;
        this.userdata = highscorelist;
    }

    public UserHighscoreListAdapter(Context context) {
        this.context = context;
    }

    public void setData(List<User> data) {
        this.userdata = data;
        this.notifyDataSetChanged();
    }

    private class UserHighscoreViewHolder {
        TextView username;
        TextView fraction;
        TextView points;
        ViewLocalizer localizer;
        CircularImageView userImage;
        CircularImageView placeBadge;
    }

    @Override
    public boolean isEmpty(){
        return userdata==null || userdata.isEmpty();
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    @Override
    public int getCount() {
        return userdata.size();
    }

    @Override
    public Object getItem(int position) {
        return userdata.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final UserHighscoreViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.highscore_user_row, parent, false);
            holder = new UserHighscoreViewHolder();
            holder.fraction = (TextView) convertView.findViewById(R.id.userfraction);
            holder.placeBadge = (CircularImageView) convertView.findViewById(R.id.badge);
            holder.userImage = (CircularImageView) convertView.findViewById(R.id.userimage);
            holder.points = (TextView) convertView.findViewById(R.id.userpoints);
            holder.username = (TextView) convertView.findViewById(R.id.username);
            convertView.setTag(holder);
        } else {
            holder = (UserHighscoreViewHolder) convertView.getTag();
            holder.localizer.cancel();
        }

        holder.username.setText(userdata.get(position).getUsername());
        holder.points.setText(userdata.get(position).getUserPoints() + " "+context.getString(R.string.points));
        Picasso.with(context).cancelRequest(holder.userImage);
        if (userdata.get(position).hasImage() && holder.userImage!=null) {
            User user = userdata.get(position);
            if(user!=null){
                ParseFile image = user.getImage();
                if(image!=null){
                    String url = image.getUrl();
                    if(url!=null){
                        Picasso.with(context).load(url).into(holder.userImage);
                    }
                }
            }


        }
        holder.localizer = new ViewLocalizer(context);
        if(userdata.get(position).hasFraction()){
            userdata.get(position).getFraction().fetchIfNeededInBackground(new GetCallback<Fraction>() {
                @Override
                public void done(Fraction fraction, ParseException e) {
                    holder.localizer.setLocalizedStringOnTextView(fraction.getName(), holder.fraction);
                }
            });
        }

        return convertView;
    }

}