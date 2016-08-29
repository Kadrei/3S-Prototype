package de.thmgames.s3.Controller;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import de.thmgames.s3.Model.ParseModels.User;

/**
 * Created by Benedikt on 23.10.2014.
 */
public abstract class Users {

    public static boolean hasCurrentUser() {
        return User.getCurrentUser() != null;
    }

    public static User getCurrentS3User() {
        return (User) ParseUser.getCurrentUser();
    }

    public static User.USERSTATE getUserStateFromUser(User user) {
        if (user != null) {
            if (user.hasFraction()) {
                return User.USERSTATE.LOGGEDIN;
            } else {
                return User.USERSTATE.MISSING_FRACTION;
            }
        }
        return User.USERSTATE.NOTLOGGEDIN;
    }


    public static void trySignUp(String username, String password, String studiengang, String fachsemester, String geschlecht, SignUpCallback callback) {
        if (Users.hasCurrentUser()) return;
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFachsemester(fachsemester);
        user.setGeschlecht(geschlecht);
        user.setStudiengang(studiengang);
        user.signUpInBackground(callback);
    }

    public static void tryLogin(String username, String password, LogInCallback callback) {
        if (Users.hasCurrentUser()) return;
        ParseUser.logInInBackground(username, password, callback);
    }

    public static void findUsersOrderedByPoints(FindCallback<User> callback) {
        ParseQuery<User> query = ParseQuery.getQuery(User.class);
        query.orderByDescending(User.USER_POINTS);
        query.setLimit(100);
        query.findInBackground(callback);
    }

    public static void setProfileImage(final User user, byte[] imageData, final SaveCallback callback) {
        final ParseFile file = new ParseFile("3S_Profile_Picture.jpg", imageData);
        file.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    callback.done(e);
                }
                user.setImage(file).saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        callback.done(e);
                    }
                });
            }
        });
    }
}
