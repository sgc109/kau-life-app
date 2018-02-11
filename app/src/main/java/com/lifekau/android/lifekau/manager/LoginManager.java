package com.lifekau.android.lifekau.manager;

import android.content.Context;

/**
 * Created by sgc109 on 2018-02-09.
 */

public class LoginManager {
    private static LoginManager sLoginManager;

    private String userId;
    private String password;

    private String studentId = "2012122327";
    private Context mContext;

    private LoginManager(Context context){
        mContext = context;
    }

    public static synchronized LoginManager get(Context context){
        if(sLoginManager == null){
            sLoginManager = new LoginManager(context);
        }
        return sLoginManager;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

}
