package com.newtpond.testnavdrawer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Arrays;
import java.util.List;

import static com.newtpond.testnavdrawer.utils.NetworkAvailable.isNetworkAvailable;
import static com.newtpond.testnavdrawer.utils.NetworkAvailable.noNetworkAlert;

/* these are for the app key hash
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
*/

public class LoginActivity extends Activity {

    protected TextView mSignUpTextView;
    protected EditText mUsername;
    protected EditText mPassword;
    protected Button mLoginButton;
    protected Button mFbLoginButton;
    protected ProgressBar mAuthProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuthProgress = (ProgressBar)findViewById(R.id.progressIndicator);

        /* TODO: use this to generate hash for facebook app*/
        // - has to be run after package generated again as the hash changes ... and then deleted so it does not log
        //
        /*
        PackageInfo info;
        try {
            info = getPackageManager().getPackageInfo("com.newtpond.testnavdrawer", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md;
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                String something = new String(Base64.encode(md.digest(), 0));
                //String something = new String(Base64.encodeBytes(md.digest()));
                Log.e("hash key", something);
            }
        } catch (PackageManager.NameNotFoundException e1) {
            Log.e("name not found", e1.toString());
        } catch (NoSuchAlgorithmException e) {
            Log.e("no such an algorithm", e.toString());
        } catch (Exception e) {
            Log.e("exception", e.toString());
        }
        */

        if(getResources().getBoolean(R.bool.login_portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // sign up link
        mSignUpTextView = (TextView)findViewById(R.id.signupText);
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
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
                                if(!isNetworkAvailable(LoginActivity.this)) {
                                    noNetworkAlert(LoginActivity.this);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                    builder.setMessage(e.getMessage())
                                            .setTitle(R.string.login_error_title)
                                            .setPositiveButton(android.R.string.ok, null);
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                }
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
        // Associate the device with a user for targeted push notifications
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", ParseUser.getCurrentUser());
        installation.addAllUnique("channels", Arrays.asList("", "UsersChannel"));
        installation.saveInBackground();

        // subscribe to push notifications
        ParsePush.subscribeInBackground("", new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
                } else {
                    Log.e("com.parse.push", "failed to subscribe for push", e);
                }
            }
        });

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
        mAuthProgress.setVisibility(View.VISIBLE);
        List<String> permissions = Arrays.asList("public_profile");

        if(!isNetworkAvailable(LoginActivity.this)) {
            noNetworkAlert(this);
            mAuthProgress.setVisibility(View.INVISIBLE);
        } else {
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
                                ParseUser.getCurrentUser().put("displayName",
                                        user.getName());
                                ParseUser.getCurrentUser().put("facebookId",
                                        user.getId());
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
                            mAuthProgress.setVisibility(View.INVISIBLE);
                        }
                    });
            request.executeAsync();

        }
    }
}
