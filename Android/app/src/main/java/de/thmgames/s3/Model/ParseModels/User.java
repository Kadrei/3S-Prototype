package de.thmgames.s3.Model.ParseModels;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * Created by Benedikt on 23.10.2014.
 */
@ParseClassName(User.PARSE_KEY)
public class User extends ParseUser {
    public final static String PARSE_KEY = "_User";

    public final static String USER_ENERGY = "energy";
    public final static String USER_POINTS = "points";
    public final static String USER_FRACTION = Fraction.PARSE_KEY;

    public final static String USER_IMG = "image";

    public final static String USER_STUDIENGANG = "studiengang";
    public final static String USER_FACHSEMESTER = "fachsemester";
    public final static String USER_GESCHLECHT = "geschlecht";

    private final static String defaultString ="";
    private final static int defaultInt=0;


    public enum USERSTATE {
        MISSING_FRACTION, NOTLOGGEDIN, LOGGEDIN
    }

    public User setFraction(Fraction fraction) {
        put(USER_FRACTION, fraction);
        return this;
    }

    public boolean hasFraction() {
        return has(USER_FRACTION) && getFraction()!=null;
    }

    public Fraction getFraction() {
        return (Fraction) getParseObject(USER_FRACTION);
    }


    public int getEnergy() {
        return has(USER_ENERGY)? getInt(USER_ENERGY) : defaultInt;
    }

    public User setEnergy(int energy){
        put(USER_ENERGY, energy);
        return this;
    }
    public User incEnergy(int amount) {
        increment(USER_ENERGY, amount);
        return this;
    }

    public int getUserPoints() {
        return has(USER_POINTS)? getInt(USER_POINTS) : defaultInt;
    }

    public User setUserPoints(int value) {
        put(USER_POINTS, value);
        return this;
    }

    public User incPoints(int amount) {
        increment(USER_POINTS, amount);
        return this;
    }

    public boolean hasImage() {
        return has(USER_IMG) && getImage()!=null;
    }

    public ParseFile getImage() {
        return getParseFile(USER_IMG);
    }

    public User setImage(ParseFile mImage) {
        put(USER_IMG, mImage);
        return this;
    }

    public String getStudiengang() {
        return has(USER_STUDIENGANG)? getString(USER_STUDIENGANG) : defaultString;
    }

    public User setStudiengang(String studiengang) {
        put(USER_STUDIENGANG, studiengang);
        return this;
    }

    public User setGeschlecht(String geschlecht) {
        put(USER_GESCHLECHT, geschlecht);
        return this;
    }

    public String getGeschlecht() {
        return has(USER_GESCHLECHT)? getString(USER_GESCHLECHT) : defaultString;
    }

    public String getFachsemester() {
        return has(USER_FACHSEMESTER)? getString(USER_FACHSEMESTER) : defaultString;
    }

    public User setFachsemester(String fachsemester) {
        put(USER_FACHSEMESTER, fachsemester);
        return this;
    }

    public void setProfileImage(byte[] imageData, final SaveCallback callback) {
        final ParseFile file = new ParseFile("3S_Profile_Picture.jpg", imageData);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    callback.done(e);
                }
                User.this.setImage(file).saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        callback.done(e);
                    }
                });
            }
        });
    }

}
