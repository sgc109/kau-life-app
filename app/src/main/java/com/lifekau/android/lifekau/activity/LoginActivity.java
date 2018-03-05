package com.lifekau.android.lifekau.activity;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.util.Log;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.lifekau.android.lifekau.AdvancedEncryptionStandard;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LMSPortalManager;
import com.lifekau.android.lifekau.manager.LoginManager;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginActivity extends AppCompatActivity {

    private static final String SAVE_GUID = "shared_preferences_globally_unique_identifier";
    private static final String SAVE_ID = "shared_preferences_save_id";
    private static final String SAVE_PASSWORD = "shared_preferences_save_password";
    private static final String SAVE_CHECKED_AUTO_LOGIN = "shared_preferences_save_checked_auto_login";

    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int UNEXPECTED_ERROR = -100;

    private UserLoginTask mAuthTask = null;
    private LMSPortalManager mLMSPortalManager = LMSPortalManager.getInstance();

    private AutoCompleteTextView mIdView;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;
    private View mLoginFormView;
    private CheckBox mAutoLoginCheckBox;

    public LoginActivity() {
    }

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginFormView = findViewById(R.id.login_form);

        mIdView = findViewById(R.id.id);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mIdView.setSelection(0);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin(mIdView.getText().toString(), mPasswordView.getText().toString());
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
                attemptLogin(mIdView.getText().toString(), mPasswordView.getText().toString());
            }
        });

        mAutoLoginCheckBox = findViewById(R.id.auto_login_check_box);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("로그인 중입니다");
        mProgressDialog.setCancelable(false);
        mProgressDialog.setCanceledOnTouchOutside(false);

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid id, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(String id, String password) {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mIdView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.

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
            Resources resources = applicationWeakReference.get().getResources();
            LoginActivity activitiy = activityReference.get();
            if (activityReference == null || activitiy.isFinishing())
                return resources.getInteger(R.integer.unexpected_error);
            Integer result = activitiy.mLMSPortalManager.pullSession(activitiy, mid, mPassword);
            if (result != resources.getInteger(R.integer.no_error)) return result;
            result = activitiy.mLMSPortalManager.pullStudentId(activitiy);
            if (result != resources.getInteger(R.integer.no_error)) return result;
            return resources.getInteger(R.integer.no_error);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (activityReference == null) return;
            if (applicationWeakReference == null) return;
            LoginActivity activitiy = activityReference.get();
            Resources resources = activitiy.getResources();
            activitiy.mAuthTask = null;
            activitiy.showProgress(false);
            if (result == resources.getInteger(R.integer.no_error)) {
                if (activitiy.mAutoLoginCheckBox.isChecked()) {
                    SharedPreferences sharedPref = activitiy.getPreferences(Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    String uniqueID = sharedPref.getString(SAVE_GUID, null);
                    if (uniqueID == null) {
                        uniqueID = UUID.randomUUID().toString();
                        editor.putString(SAVE_GUID, uniqueID);
                    }
                    AdvancedEncryptionStandard advancedEncryptionStandard = new AdvancedEncryptionStandard();
                    editor.putString(SAVE_ID, advancedEncryptionStandard.encrypt(mid, uniqueID));
                    editor.putString(SAVE_PASSWORD, advancedEncryptionStandard.encrypt(mPassword, uniqueID));
                    editor.putBoolean(SAVE_CHECKED_AUTO_LOGIN, true);
                    editor.apply();
                }
                LoginManager loginManager = LoginManager.get(activityReference.get());
                loginManager.setUserId(mid);
                loginManager.setPassword(mPassword);
                loginManager.setStudentId(activitiy.mLMSPortalManager.getStudentId());
                Intent intent = HomeActivity.newIntent(activityReference.get());
                activitiy.startActivity(intent);
                activitiy.finish();
            } else if (result == resources.getInteger(R.integer.session_error)) {
                activitiy.mPasswordView.setError(activitiy.getString(R.string.error_incorrect_password));
                activitiy.mPasswordView.requestFocus();
            } else {
                activitiy.showErrorMessage();
            }
        }

        @Override
        protected void onCancelled() {
            if (activityReference == null) return;
            if (applicationWeakReference == null) return;
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
        AdvancedEncryptionStandard advancedEncryptionStandard = new AdvancedEncryptionStandard();
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String uniqueID = sharedPref.getString(SAVE_GUID, null);
        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();
            editor.putString(SAVE_GUID, uniqueID);
        }
        String id = advancedEncryptionStandard.decrypt(sharedPref.getString(SAVE_ID, ""), uniqueID);
        String password = advancedEncryptionStandard.decrypt(sharedPref.getString(SAVE_PASSWORD, ""), uniqueID);
        boolean checked = sharedPref.getBoolean(SAVE_CHECKED_AUTO_LOGIN, false);
        if (checked) {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
            attemptLogin(id, password);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mProgressDialog.dismiss();
    }
}

