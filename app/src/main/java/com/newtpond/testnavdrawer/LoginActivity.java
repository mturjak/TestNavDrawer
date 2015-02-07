package com.newtpond.testnavdrawer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookRequestError;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.List;

public class LoginActivity extends Activity {

    protected TextView mSignUpTextView;
    protected EditText mUsername;
    protected EditText mPassword;
    protected Button mLoginButton;
    protected Button mFbLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(getResources().getBoolean(R.bool.login_portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // sign up link
        mSignUpTextView = (TextView)findViewById(R.id.signupText);
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        mUsername = (EditText)findViewById(R.id.usernameField);
        mPassword = (EditText)findViewById(R.id.passwordField);
        mLoginButton = (Button)findViewById(R.id.loginButton);
        mFbLoginButton = (Button)findViewById(R.id.fbLoginButton);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();

                username = username.trim();
                password = password.trim();

                if (username.isEmpty() || password.isEmpty()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                    builder.setMessage(R.string.login_error_message)
                            .setTitle(R.string.login_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    // Login
                    // setSupportProgressBarIndeterminateVisibility(true);

                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        public void done(ParseUser user, ParseException e) {
                            // setSupportProgressBarIndeterminateVisibility(false);

                            if (user != null) {
                                // start main activity
                                finishActivity();
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setMessage(e.getMessage())
                                        .setTitle(R.string.login_error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            }
        });

        mFbLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFbLoginButtonClicked();
            }
        });
    }

    private void finishActivity() {
        // Start an intent for the dispatch activity
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    /**
     * Facebook login related methods
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.finishAuthentication(requestCode, resultCode, data);
    }

    private void onFbLoginButtonClicked() {
        List<String> permissions = Arrays.asList("public_profile");
        ParseFacebookUtils.logIn(permissions, this, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException err) {
                if (user != null) {
                    if (user.isNew()) {
                        // set favorites as null, or mark it as empty somehow
                        makeMeRequest();
                    } else {
                        finishActivity();
                    }
                }
            }
        });
    }

    private void makeMeRequest() {
        Session session = ParseFacebookUtils.getSession();
        if (session != null && session.isOpened()) {
            Request request = Request.newMeRequest(
                    ParseFacebookUtils.getSession(),
                    new Request.GraphUserCallback() {
                        @Override
                        public void onCompleted(GraphUser user,
                                                Response response) {
                            if (user != null) {
                                ParseUser.getCurrentUser().put("firstName",
                                        user.getFirstName());
                                ParseUser.getCurrentUser().saveInBackground();
                                finishActivity();
                            } else if (response.getError() != null) {
                                if ((response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_RETRY)
                                        || (response.getError().getCategory() == FacebookRequestError.Category.AUTHENTICATION_REOPEN_SESSION)) {
                                    // TODO: make dialog error instead of toast
                                    Toast.makeText(getApplicationContext(),
                                            R.string.session_invalid_error,
                                            Toast.LENGTH_LONG).show();

                                } else {
                                    // TODO: make dialog error instead of toast
                                    Toast.makeText(getApplicationContext(),
                                            R.string.logn_generic_error,
                                            Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    });
            request.executeAsync();

        }
    }
}
