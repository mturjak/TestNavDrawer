package com.newtpond.testnavdrawer.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.newtpond.testnavdrawer.R;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment {

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EditProfileFragment() {
    }

    protected ParseUser mCurrentUser;
    protected EditText mFirstName;
    protected EditText mLastName;
    protected EditText mHometown;
    protected EditText mWebsite;
    protected EditText mEmail;
    protected Button mSaveButton;

    protected String mFacebookId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        View rootView = getView();

        mEmail = (EditText) rootView.findViewById(R.id.emailField);
        mFirstName = (EditText) rootView.findViewById(R.id.nameField);
        mLastName = (EditText) rootView.findViewById(R.id.lastNameField);
        mHometown = (EditText) rootView.findViewById(R.id.hometown);
        mWebsite = (EditText) rootView.findViewById(R.id.website);
        mSaveButton = (Button) rootView.findViewById(R.id.saveButton);

        mCurrentUser = ParseUser.getCurrentUser();
        mFacebookId = mCurrentUser.getString("facebookId");

        ((TextView) rootView.findViewById(R.id.usernameField))
                .setText(mCurrentUser.getUsername());

        mEmail.setText(mCurrentUser.getEmail());
        mFirstName.setText(mCurrentUser.getString("firstName"));
        mLastName.setText(mCurrentUser.getString("lastName"));
        mHometown.setText(mCurrentUser.getString("hometown"));
        mWebsite.setText(mCurrentUser.getString("website"));

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = mFirstName.getText().toString();
                String lastName = mLastName.getText().toString();
                String hometown = mHometown.getText().toString();
                String website = mWebsite.getText().toString();
                String email = mEmail.getText().toString();

                email = email.trim();
                firstName = firstName.trim();
                lastName = lastName.trim();
                hometown = hometown.trim();
                website = website.trim();


                if (email.isEmpty() && mFacebookId == "0") {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.edit_profile_error_message_no_email)
                            .setTitle(R.string.edit_profile_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    mCurrentUser.setEmail(email);
                    mCurrentUser.put("firstName", firstName);
                    mCurrentUser.put("lastName", lastName);
                    mCurrentUser.put("hometown", hometown);
                    mCurrentUser.put("website", website);

                    mCurrentUser.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setMessage(e.getMessage())
                                        .setTitle(R.string.edit_profile_error_title)
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }
            }
        });
    }
}
