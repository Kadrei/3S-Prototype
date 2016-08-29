package de.thmgames.s3.Utils;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Benedikt on 22.02.2015.
 */
public class ParseQueryExtension<T extends ParseObject> extends ParseQuery<T> {
    public ParseQueryExtension(Class<T> subclass) {
        super(subclass);
    }

    public ParseQueryExtension(String theClassName) {
        super(theClassName);
    }

    public void fetchWhereAvailableInBackground(final FindCallback<T> callback){
        ParseQuery<T> localQuery = this.fromLocalDatastore();
        localQuery.findInBackground(new FindCallback<T>() {
            @Override
            public void done(List<T> ts, ParseException e) {
                if(e!=null || ts==null || ts.isEmpty()){
                    ParseQueryExtension.this.findInBackground(callback);
                }else{
                    callback.done(ts, e);
                }
            }
        });
    }
}
