package com.lifekau.android.lifekau.activity;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lifekau.android.lifekau.AdvancedEncryptionStandard;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.LMSPortalManager;
import com.lifekau.android.lifekau.manager.LoginManager;

import java.lang.ref.WeakReference;
import java.util.UUID;

import static android.os.SystemClock.sleep;


public class LoginActivity extends AppCompatActivity {

    private static final String SAVE_GUID = "shared_preferences_globally_unique_identifier";
    private static final String SAVE_ID = "shared_preferences_save_id";
    private static final String SAVE_PASSWORD = "shared_preferences_save_password";
    public static final String SAVE_AUTO_LOGIN = "shared_preferences_save_auto_login";

    private static final int REQUEST_READ_CONTACTS = 0;
    private static final int UNEXPECTED_ERROR = -100;

    private UserLoginTask mAuthTask = null;
    private LMSPortalManager mLMSPortalManager = LMSPortalManager.getInstance();

    private AutoCompleteTextView mIdView;
    private EditText mPasswordView;
    private ProgressDialog mProgressDialog;
    private View mLoginFormView;
    public static String mSharedPreferenceApp;

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

        mSharedPreferenceApp = getString(R.string.shared_preference_app);
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

        Button mIdSignInButton = findViewById(R.id.id_sign_in_button);
        mIdSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
                attemptLogin(mIdView.getText().toString(), mPasswordView.getText().toString());
            }
        });

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

        mIdView.setError(null);
        mPasswordView.setError(null);

        boolean cancel = false;
        View focusView = null;

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
            Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
            focusView.startAnimation(shake);
        } else {
            showProgress(true);
            mAuthTask = new UserLoginTask(getApplication(), this, id, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isIdValid(String id) {
        return true;
    }

    private boolean isPasswordValid(String password) {
        return true;
//        return password.length() > 4;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (show) mProgressDialog.show();
        else mProgressDialog.dismiss();
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    public static class UserLoginTask extends AsyncTask<Void, Void, Integer> {

        private WeakReference<LoginActivity> activityReference;
        private WeakReference<Application> applicationWeakReference;
        private final String mId;
        private final String mPassword;

        UserLoginTask(Application application, LoginActivity loginActivity, String id, String password) {
            applicationWeakReference = new WeakReference<>(application);
            activityReference = new WeakReference<>(loginActivity);
            mId = id;
            mPassword = password;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            Resources resources = applicationWeakReference.get().getResources();
            LoginActivity activity = activityReference.get();
            if (activityReference == null || activity.isFinishing())
                return resources.getInteger(R.integer.unexpected_error);
            int result;
            int count = 0;
            while ((result = activity.mLMSPortalManager.pullSession(applicationWeakReference.get(), mId, mPassword)) != resources.getInteger(R.integer.no_error)) {
                sleep(2000);
                count++;
                if (count == resources.getInteger(R.integer.maximum_retry_num)) return result;
            }
            while ((result = activity.mLMSPortalManager.pullStudentId(applicationWeakReference.get())) != resources.getInteger(R.integer.no_error)) {
                sleep(2000);
                count++;
                if (count == resources.getInteger(R.integer.maximum_retry_num)) return result;
            }
            return resources.getInteger(R.integer.no_error);
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (activityReference == null) return;
            if (applicationWeakReference == null) return;
            LoginActivity activity = activityReference.get();
            Resources resources = activity.getResources();
            activity.mAuthTask = null;

            if (result == resources.getInteger(R.integer.no_error)) {
                SharedPreferences sharedPref = activity.getSharedPreferences(activity.mSharedPreferenceApp, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                String uniqueID = sharedPref.getString(SAVE_GUID, null);
                if (uniqueID == null) {
                    uniqueID = UUID.randomUUID().toString();
                    editor.putString(SAVE_GUID, uniqueID);
                }
                AdvancedEncryptionStandard advancedEncryptionStandard = new AdvancedEncryptionStandard();
                editor.putString(SAVE_ID, advancedEncryptionStandard.encrypt(mId, uniqueID));
                editor.putString(SAVE_PASSWORD, advancedEncryptionStandard.encrypt(mPassword, uniqueID));
                editor.putBoolean(SAVE_AUTO_LOGIN, true);
                editor.apply();
                LoginManager loginManager = LoginManager.get(activityReference.get());
                loginManager.setUserId(mId);
                loginManager.setPassword(mPassword);
                loginManager.setStudentId(activity.mLMSPortalManager.getStudentId());
                Intent intent = HomeActivity.newIntent(activityReference.get());
                activity.startActivity(intent);
                activity.finish();
            } else if (result == resources.getInteger(R.integer.session_error)) {
                activity.mPasswordView.setError(activity.getString(R.string.error_incorrect_password));
                activity.mPasswordView.requestFocus();
            } else {
                activity.showErrorMessage();
            }

//            activity.showProgress(false);
        }

        @Override
        protected void onCancelled() {
            if (activityReference == null) return;
            if (applicationWeakReference == null) return;
            LoginActivity activity = activityReference.get();
            activity.mAuthTask = null;
            activity.showProgress(false);
        }
    }

    public void showErrorMessage() {
        Toast toast = Toast.makeText(getApplicationContext(), "네트워크 오류가 발생하였습니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showProgress(false);
        AdvancedEncryptionStandard advancedEncryptionStandard = new AdvancedEncryptionStandard();
        SharedPreferences sharedPref = getSharedPreferences(mSharedPreferenceApp, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        String uniqueID = sharedPref.getString(SAVE_GUID, null);
        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();
            editor.putString(SAVE_GUID, uniqueID);
            editor.apply();
        }
        String id = advancedEncryptionStandard.decrypt(sharedPref.getString(SAVE_ID, ""), uniqueID);
        String password = advancedEncryptionStandard.decrypt(sharedPref.getString(SAVE_PASSWORD, ""), uniqueID);
        boolean autoLogin = sharedPref.getBoolean(SAVE_AUTO_LOGIN, false);
        if (autoLogin) {
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

