package de.thmgames.s3.Controller;

import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;

import java.lang.ref.WeakReference;
import java.util.Locale;

import de.thmgames.s3.Model.ParseModels.LocalizedString;
import de.thmgames.s3.Utils.LogUtils;
import de.thmgames.s3.Utils.ParseErrorUtils;

/**
 * Created by Benedikt on 24.02.2015.
 */
public class ViewLocalizer {
    private static Locale defaultLocale = Locale.getDefault();
    private boolean cancelled=false;
    private Context ctx;

    public ViewLocalizer(Context ctx){
        this.ctx = ctx;
    }

    public void setLocalizedStringOnTextView(final LocalizedString string, final TextView textView){
        if(string == null) return;
        if(!string.isDataAvailable()) textView.setText("loading...");
        string.fetchWhereAvailable(new GetCallback<LocalizedString>() {
            @Override
            public void done(LocalizedString localizedString, ParseException e) {
                if(cancelled) return;
                if(e!=null){
                    LogUtils.e("setLocalizedStringOnTextView()", "Error while loading localized String", e);
                    textView.setText(ParseErrorUtils.getErrorMessageFor(ctx, e));
                }
                textView.setText(localizedString.getMessageForLocale(defaultLocale));
            }
        });

    }

    public void setLocalizedStringOnTextView(final LocalizedString string, final TextView textView, final String beforeString, final String appentString){
        if(string == null) return;
        if(!string.isDataAvailable()) textView.setText("loading...");
        string.fetchWhereAvailable(new GetCallback<LocalizedString>() {
            @Override
            public void done(LocalizedString localizedString, ParseException e) {
                if(cancelled) return;
                if(e!=null){
                    LogUtils.e("setLocalizedStringOnTextView()", "Error while loading localized String",e);
                    textView.setText(ParseErrorUtils.getErrorMessageFor(ctx,e));
                }
                textView.setText(beforeString+localizedString.getMessageForLocale(defaultLocale)+appentString);
            }
        });

    }

    public void setLocalizedStringOnTextView(String string, TextView textView){
        textView.setText(string);
    }

    public void cancel(){
        this.cancelled =true;
    }

    public void setLocalizedStringOnTextView(LocalizedString string, final Toolbar mToolbar) {
        if(string == null) return;
        if(!string.isDataAvailable()) mToolbar.setTitle("Loading Title...");
        string.fetchWhereAvailable(new GetCallback<LocalizedString>() {
            @Override
            public void done(LocalizedString localizedString, ParseException e) {
                if(cancelled) return;
                if(e!=null){
                    LogUtils.e("setLocalizedStringOnTextView()", "Error while loading localized String",e);
                    mToolbar.setTitle(ParseErrorUtils.getErrorMessageFor(ctx, e));
                }
                mToolbar.setTitle(localizedString.getMessageForLocale(defaultLocale));
            }
        });
    }

}
