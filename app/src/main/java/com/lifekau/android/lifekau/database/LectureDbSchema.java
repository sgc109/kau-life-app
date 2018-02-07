package com.lifekau.android.lifekau.database;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class LectureDbSchema {
    public static class LectureTable {
        public static final String NAME = "lectures";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String NAME = "name";
        }
    }
}
