package com.lifekau.android.lifekau.manager;

import android.content.Context;
import android.content.SharedPreferences;

import com.lifekau.android.lifekau.AdvancedEncryptionStandard;

/**
 * Created by sgc109 on 2018-02-09.
 */

public class LoginManager {

    private static final String SAVE_GUID = "shared_preferences_globally_unique_identifier";
    private static final String SAVE_ID = "shared_preferences_save_id";
    private static final String SAVE_PASSWORD = "shared_preferences_save_password";
    private static final String SAVE_STUDENT_ID = "shared_preferences_save_student_id";

    private static LoginManager sLoginManager;
    private SharedPreferences mSharedPref;

    private Context mContext;

    private LoginManager(Context context) {
        mContext = context;
        mSharedPref = context.getSharedPreferences("LifeKAU", Context.MODE_PRIVATE);
    }

    public static synchronized LoginManager get(Context context) {
        if (sLoginManager == null) {
            sLoginManager = new LoginManager(context);
        }
        return sLoginManager;
    }

    public String getStudentId() {
        String uniqueId = mSharedPref.getString(SAVE_GUID, null);
        return AdvancedEncryptionStandard.decrypt(mSharedPref.getString(SAVE_STUDENT_ID, null), uniqueId);
    }

    public String getUserId() {
        String uniqueId = mSharedPref.getString(SAVE_GUID, null);
        return AdvancedEncryptionStandard.decrypt(mSharedPref.getString(SAVE_ID, null), uniqueId);
    }

    public String getPassword() {
        String uniqueId = mSharedPref.getString(SAVE_GUID, null);
        return AdvancedEncryptionStandard.decrypt(mSharedPref.getString(SAVE_PASSWORD, null), uniqueId);
    }

}
