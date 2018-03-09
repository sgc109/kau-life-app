package com.lifekau.android.lifekau.manager;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.util.Log;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.LibraryListActivity;
import com.lifekau.android.lifekau.model.AccumulatedGrade;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.SocketTimeoutException;
import java.util.Calendar;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LibraryManager {

    private static final String[] STUDY_ROOM_CODE = {"STD-A", "STD-B1", "STD-B2", "STD-C1", "STD-C2", "STD-C3"};

    private static final int TOTAL_READING_ROOM_NUM = 5;
    private static final int TOTAL_READING_ROOM_SEAT_NUM = 300;
    private static final int TOTAL_STDUYING_ROOM_NUM = 6;
    private static final int TOTAL_STDUYING_ROOM_STATUS_NUM = 100;

    private int[] mReadingRoomAvailableSeat;
    private int[] mReadingRoomTotalSeat;
    private String[] mReadingRoomName;
    private String[] mStudyRoomName;
    private boolean[][] mStudyRoomDetailStatus;
    private OkHttpClient mClient;

    private LibraryManager() {
        mReadingRoomAvailableSeat = new int[TOTAL_READING_ROOM_NUM + 1];
        mReadingRoomTotalSeat = new int[TOTAL_READING_ROOM_NUM + 1];
        mReadingRoomName = new String[TOTAL_READING_ROOM_NUM + 1];
        mStudyRoomName = new String[TOTAL_STDUYING_ROOM_NUM + 1];
        mStudyRoomDetailStatus = new boolean[TOTAL_STDUYING_ROOM_NUM + 1][TOTAL_STDUYING_ROOM_STATUS_NUM];
        mClient = new OkHttpClient();
    }

    private static class LazyHolder {
        private static LibraryManager INSTANCE = new LibraryManager();
    }

    public static LibraryManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public int pullReadingRoomStatus(Context context) {
        Resources resources = context.getResources();
        Request request = new Request.Builder()
                .url("http://ebook.kau.ac.kr:81/domian5.asp")
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accpet_encoding))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .build();
        Call call = mClient.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) {
                return resources.getInteger(R.integer.server_error);
            }
            Document doc = Jsoup.parse(new String(res.body().bytes(), "EUC-KR"));
            for (int i = 0; i < TOTAL_READING_ROOM_NUM; i++) {
                String[] strings = doc.select("tr").get(i + 3).text().split("\\s+");
                mReadingRoomName[i] = strings[1];
                mReadingRoomTotalSeat[i] = Integer.parseInt(strings[2]);
                mReadingRoomAvailableSeat[i] = Integer.parseInt(strings[4]);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.session_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        return resources.getInteger(R.integer.no_error);
    }

    public String getReadingRoomName(int roomNum) {
        return mReadingRoomName[roomNum];
    }

    public int getReadingRoomAvailableSeat(int roomNum) {
        return mReadingRoomAvailableSeat[roomNum];
    }

    public int getReadingRoomUsedSeat(int roomNum) {
        return mReadingRoomTotalSeat[roomNum] - mReadingRoomAvailableSeat[roomNum];
    }

    public int getReadingRoomTotalSeat(int roomNum) {
        return mReadingRoomTotalSeat[roomNum];
    }

    public int pullStudyRoomDetailStatus(Context context, int roomNum) {
        Resources resources = context.getResources();
        try{
            Connection.Response res = Jsoup.connect("http://lib.kau.ac.kr/haulms/haulms/kauclSRResvResult.csp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .data("jc", "getTimes", "rc", STUDY_ROOM_CODE[roomNum], "uid", "GUEST")
                    .method(Connection.Method.POST)
                    .timeout(2000)
                    .execute();
            String[] strings = res.body().split("\u0011");
            for (int i = 3; i < strings.length; i++) {
                String[] infomation = strings[i].split("\\^");
                mStudyRoomDetailStatus[roomNum][Integer.parseInt(infomation[0])] = infomation[2].equalsIgnoreCase("+");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        catch(Error e){
            return resources.getInteger(R.integer.network_error);
        }
        return resources.getInteger(R.integer.no_error);
    }

    public String getStudyRoomName(int roomNum) {
        return mStudyRoomName[roomNum];
    }

    public boolean[] getStudyRoomDetailStatusArray(int roomNum) {
        return mStudyRoomDetailStatus[roomNum];
    }

    public boolean isStudyRoomAvailableNow(int roomNum) {
        Calendar currTime = Calendar.getInstance();
        int currHour = currTime.get(Calendar.HOUR_OF_DAY);
        currHour = currHour < 0 ? 0 : currHour;
        return mStudyRoomDetailStatus[roomNum][currHour];
    }

    public void showStudyRoomStatus(Context context, int roomNum) {
        Activity activity = (Activity) context;
        TimePickerDialog timePickerDialog = new TimePickerDialog();
        Calendar now = Calendar.getInstance();
        timePickerDialog.initialize(
                null,
                now.get(Calendar.HOUR_OF_DAY),
                now.get(Calendar.MINUTE),
                now.get(Calendar.SECOND),
                true,
                getStudyRoomDetailStatusArray(roomNum)
        );
        timePickerDialog.show(activity.getFragmentManager(), "study_room_dialog");
    }
}