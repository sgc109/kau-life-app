package com.lifekau.android.lifekau.database;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class AlarmDbSchema {
    public static final class AlarmTable {
        public static final String NAME = "alarms";

        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String CONTENT = "content";
            public static final String TYPE = "type";
            public static final String DATE = "date";
        }
    }
}
