package com.lifekau.android.lifekau.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.database.LectureBaseHelper;
import com.lifekau.android.lifekau.database.LectureCursorWrapper;
import com.lifekau.android.lifekau.database.LectureDbSchema;
import com.lifekau.android.lifekau.model.Lecture;

import java.util.ArrayList;
import java.util.List;

import static com.lifekau.android.lifekau.database.LectureDbSchema.*;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class LectureManager {
    private static LectureManager sLectureManager;

    private SQLiteDatabase mDatabase;
    private Context mContext;

    private LectureManager(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new LectureBaseHelper(mContext).getWritableDatabase();
    }

    public static synchronized LectureManager get(Context context) {
        if (sLectureManager == null) {
            sLectureManager = new LectureManager(context);
        }
        return sLectureManager;
    }

    public List<Lecture> getAllLectures() {
        List<Lecture> lectures = new ArrayList<>();

        LectureCursorWrapper cursor = queryLectures(
                null,
                null
        );
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                lectures.add(cursor.getLecture());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return lectures;
    }

    private ContentValues getContentValues(Lecture lecture){
        ContentValues values = new ContentValues();
        values.put(LectureTable.Cols.NAME, lecture.getName());
        return values;
    }

    public void addLecture(Lecture lecture){
        ContentValues values = getContentValues(lecture);
        mDatabase.insert(LectureTable.NAME, null, values);
    }

    public void addLectures(List<Lecture> lectures){
        mDatabase.beginTransaction();
        try {
            for (Lecture lecture : lectures) {
                addLecture(lecture);
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    private LectureCursorWrapper queryLectures(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                LectureTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                LectureTable.Cols.NAME
        );

        return new LectureCursorWrapper(cursor);
    }
}
