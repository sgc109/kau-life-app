package com.lifekau.android.lifekau.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LMSPortalManager;
import com.lifekau.android.lifekau.manager.LoginManager;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int UNEXPECTED_ERROR = -100;

    private UserLoginTask mAuthTask = null;
    private LMSPortalManager mLMSPortalManager = LMSPortalManager.getInstance();

    private AutoCompleteTextView mIdView;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;
    private View mLoginFormView;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mIdView = findViewById(R.id.id);
        populateAutoComplete();

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mIdView.setSelection(0);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        mPasswordView.setSelection(0);

        Button midSignInButton = findViewById(R.id.id_sign_in_button);
        midSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                attemptLogin();
            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("로그인 중입니다");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mIdView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid id, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mIdView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String id = mIdView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid id/pw
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.prompt_input_password));
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError("");
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(id)) {
            mIdView.setError(getString(R.string.prompt_input_id));
            focusView = mIdView;
            cancel = true;
        } else if (!isIdValid(id)) {
            mIdView.setError("");
            focusView = mIdView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
//            focusView.requestFocus();
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            focusView.startAnimation(shake);
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(getApplication(), this, id, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isIdValid(String id) {
        //TODO: Replace this with your own logic
        return true;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
//        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
//            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
//
//            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
//                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
//                @Override
//                public void onAnimationEnd(Animator animation) {
//                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//                }
//            });
//
//            if (show) mProgressDialog.show();
//            else mProgressDialog.dismiss();
//        } else {
        if (show) mProgressDialog.show();
        else mProgressDialog.dismiss();
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
//        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only id addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary id addresses first. Note that there won't be
                // a primary id address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> ids = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ids.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addidsToAutoComplete(ids);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addidsToAutoComplete(List<String> idAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, idAddressCollection);

        mIdView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    public static class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private WeakReference<LoginActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;
        private final String mid;
        private final String mPassword;

        UserLoginTask(Application application, LoginActivity loginActivity, String id, String password) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(loginActivity);
            mid = id;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            if(activityReference == null) return UNEXPECTED_ERROR;
            if(applicationWeakReference == null) return UNEXPECTED_ERROR;
            LoginActivity activitiy = activityReference.get();
            Resources resources = activitiy.getResources();
            try {
                // Simulate network access.
                Integer result = activitiy.mLMSPortalManager.pullSession(applicationWeakReference.get(), mid, mPassword);
                if (result != resources.getInteger(R.integer.no_error)) return result;
                result = activitiy.mLMSPortalManager.pullStudentId(applicationWeakReference.get());
                if (result != resources.getInteger(R.integer.no_error)) return result;
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return UNEXPECTED_ERROR;
            }
            return resources.getInteger(R.integer.no_error);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(activityReference == null) return;
            if(applicationWeakReference == null) return;
            LoginActivity activitiy = activityReference.get();
            Resources resources = activitiy.getResources();
            activitiy.mAuthTask = null;
            activitiy.showProgress(false);
            if (result == resources.getInteger(R.integer.no_error)) {
                LoginManager loginManager = LoginManager.get(activityReference.get());
                loginManager.setUserId(mid);
                loginManager.setPassword(mPassword);
                loginManager.setStudentId(activitiy.mLMSPortalManager.getStudentId());
                Intent intent = HomeActivity.newIntent(activityReference.get());
                activitiy.startActivity(intent);
            } else if(result == resources.getInteger(R.integer.session_error)){
                activitiy.mPasswordView.setError(activitiy.getString(R.string.error_incorrect_password));
                activitiy.mPasswordView.requestFocus();
            }
            else{
                activitiy.showErrorMessage();
            }
        }

        @Override
        protected void onCancelled() {
            if(activityReference == null) return;
            if(applicationWeakReference == null) return;
            LoginActivity activitiy = activityReference.get();
            activitiy.mAuthTask = null;
            activitiy.showProgress(false);
        }
    }

    public void showErrorMessage() {
        Toast toast = Toast.makeText(getApplicationContext(), "오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgress(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProgressDialog.dismiss();
    }
}

