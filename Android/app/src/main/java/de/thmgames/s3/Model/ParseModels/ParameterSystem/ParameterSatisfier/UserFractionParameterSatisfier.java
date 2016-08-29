package de.thmgames.s3.Model.ParseModels.ParameterSystem.ParameterSatisfier;

import com.parse.GetCallback;
import com.parse.ParseException;

import de.thmgames.s3.Controller.Users;
import de.thmgames.s3.Model.ParseModels.Fraction;
import de.thmgames.s3.Model.ParseModels.User;

/**
 * Created by Benedikt on 08.02.2015.
 */
public class UserFractionParameterSatisfier extends AbstractParameterSatisfier {
    public Fraction fraction;
    public boolean valueSet = false;

    @Override
    public boolean needsInput() {
        return false;
    }

    @Override
    public String getTag(){
        return getClass().getName();
    }

    @Override
    public boolean isSatisfied() {
        return valueSet;
    } //fraction kann null sein nach Registrierung

    @Override
    public String getValue() {
        return fraction!=null ? fraction.getObjectId() : "";
    }

    @Override
    public void loadData() {
        if(provider!=null){
            provider.loadUserFraction(this);
            return;
        }
        User user = Users.getCurrentS3User();
        if(user ==null || user.hasFraction() || user.getFraction()==null){
            setValue(null);
        }else{
            user.getFraction().fetchIfNeededInBackground(new GetCallback<Fraction>() {
                @Override
                public void done(Fraction parseObject, ParseException e) {
                    if(e!=null){
                        setValue(parseObject);
                        return;
                    }
                    setValue(Users.getCurrentS3User().getFraction());
                }
            });
        }

    }

    public void setValue(Fraction fraction) {
        valueSet=true;
        this.fraction=fraction;
        notifyCallback();
    }

}
