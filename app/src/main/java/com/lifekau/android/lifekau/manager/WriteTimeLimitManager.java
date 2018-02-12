package com.lifekau.android.lifekau.manager;

import android.content.Context;

/**
 * Created by sgc109 on 2018-02-12.
 */

public class WriteTimeLimitManager {
    private static WriteTimeLimitManager sWriteTimeLimitManager;
    private Context mContext;
    private WriteTimeLimitManager(Context context){
        mContext = context;
        mPostLastWriteTime = 0;
        mCommentLastWriteTime = 0;
    }
    private long mPostLastWriteTime;
    private long mCommentLastWriteTime;
}
