package de.thmgames.s3.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseConfig;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import de.thmgames.s3.Controller.Users;
import de.thmgames.s3.Model.ParseModels.User;
import de.thmgames.s3.R;
import de.thmgames.s3.Utils.LogUtils;
import de.thmgames.s3.Utils.ParseErrorUtils;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * A login screen that offers login via email/password.
 */
public class SignUpOrInActivity extends AbstractBaseActivity {

    private final static String TAG = SignUpOrInActivity.class.getName();
    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mFachsemesterView;
    private EditText mStudiengangView;
    private Spinner mGeschlechtView;
    private TextView mGeschlechtTextView;
    private SmoothProgressBar mProgressLinearView;
    private View mLoginFormView;
    private Button mMainButton;
    private Button mAlternativeButton;

    private boolean isLoginProcedure = false;

    private static final String INTENT_IS_LOGIN = "login";

    public static Intent getIntent(Context ctx, boolean isLogin) {
        Intent i = new Intent(ctx, SignUpOrInActivity.class);
        i.putExtra(INTENT_IS_LOGIN, isLogin);
        return i;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        needInternet(true);
        lookForBeacons(false);
        askForPushEnablingIfNotAskedBefore(false);
        setNeededUserState(User.USERSTATE.NOTLOGGEDIN);
        checkUserState(true);
        setContentView(R.layout.activity_sign_up_or_in);

        mToolbar = (Toolbar) findViewById(R.id.signintoolbar);
        mActionBarBackgroundDrawable = mToolbar.getBackground();
        setSupportActionBar(mToolbar);
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);

        mFachsemesterView = (EditText) findViewById(R.id.fachsemester);
        mStudiengangView = (EditText) findViewById(R.id.studiengang);
        mGeschlechtTextView = (TextView) findViewById(R.id.geschlechtText);
        mGeschlechtView = (Spinner) findViewById(R.id.geschlechterSpinner);

        mMainButton = (Button) findViewById(R.id.main);
        mMainButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        ViewCompat.setElevation(mMainButton, 5f);

        mAlternativeButton = (Button) findViewById(R.id.alternative);
        mAlternativeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setIsLoginProcedure(!isLoginProcedure);
            }
        });
        ViewCompat.setElevation(mAlternativeButton, 5f);

        mLoginFormView = findViewById(R.id.login_form);
        mProgressLinearView = (SmoothProgressBar) findViewById(R.id.loading_progress_linear);
        if (getIntent() != null) {
            setIsLoginProcedure(getIntent().getBooleanExtra(INTENT_IS_LOGIN, false));
        }
        showLoadingProgress(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActionBarBackgroundDrawable.setAlpha(255);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        finish();
        System.exit(0);
    }

    @Override
    protected void onConfigLoaded(ParseConfig config) {
        super.onConfigLoaded(config);
        showLoadingProgress(false);
    }


    @Override
    public void onSnackbarShown(int height) {}

    @Override
    public void onSnackbarDismissed(int height) {}

    @Override
    protected void onCorrectUserState() {}

    public void setIsLoginProcedure(boolean isLoginProcedure) {
        this.isLoginProcedure = isLoginProcedure;

        getSupportActionBar().setTitle(isLoginProcedure ? getString(R.string.login) : getString(R.string.register));
        mMainButton.setText(isLoginProcedure ? getString(R.string.do_login) : getString(R.string.register));
        mAlternativeButton.setText(isLoginProcedure ? getString(R.string.to_registration) : getString(R.string.to_login));

        mGeschlechtTextView.setVisibility(isLoginProcedure ? View.GONE : View.VISIBLE);
        mFachsemesterView.setVisibility(isLoginProcedure ? View.GONE : View.VISIBLE);
        mStudiengangView.setVisibility(isLoginProcedure ? View.GONE : View.VISIBLE);
        mGeschlechtView.setVisibility(isLoginProcedure ? View.GONE : View.VISIBLE);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String studiengang = mStudiengangView.getText().toString();
        String fachsemester = mFachsemesterView.getText().toString();
        String geschlecht = mGeschlechtView.getSelectedItem().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username) || !isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showLoadingProgress(true);

            if (isLoginProcedure) {
                Users.tryLogin(username, password, new LogInCallback() {
                    public void done(ParseUser user, ParseException e) {
                        if (user != null) {
                            startActivity(MainActivity.getIntent(SignUpOrInActivity.this));
                        } else {
                            mPasswordView.setError(ParseErrorUtils.getErrorMessageFor(SignUpOrInActivity.this, e));
                            mUsernameView.setError(ParseErrorUtils.getErrorMessageFor(SignUpOrInActivity.this, e));
                            LogUtils.e(TAG, "SignUp didnt succeed", e);
                        }
                        showLoadingProgress(false);
                    }
                });
            } else {
                Users.trySignUp(username, password, studiengang, fachsemester, geschlecht, new SignUpCallback() {
                    public void done(ParseException e) {
                        if (e == null) {
                            startActivity(FractionChooserActivity.getIntent(SignUpOrInActivity.this));
                        } else {
                            mPasswordView.setError(ParseErrorUtils.getErrorMessageFor(SignUpOrInActivity.this, e));
                            mUsernameView.setError(ParseErrorUtils.getErrorMessageFor(SignUpOrInActivity.this, e));
                            LogUtils.e(TAG, "Register didnt succeed", e);
                        }
                        showLoadingProgress(false);
                    }
                });
            }
        }
    }

    private boolean isUsernameValid(String username) {
        return username.length() < 21;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }


    @Override
    protected void showLoadingAnimation() {
        mLoginFormView.setVisibility(View.GONE );
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(View.GONE);
            }
        });


        mProgressLinearView.setVisibility( View.VISIBLE);
        mProgressLinearView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressLinearView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void hideLoadingAnimation() {
        mLoginFormView.setVisibility(View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(View.VISIBLE);
            }
        });


        mProgressLinearView.setVisibility(View.GONE);
        mProgressLinearView.animate().setDuration(shortAnimTime).alpha(0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressLinearView.setVisibility(View.GONE);
            }
        });
    }

}



