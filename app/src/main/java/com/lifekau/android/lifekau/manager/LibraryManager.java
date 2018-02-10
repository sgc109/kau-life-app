package com.lifekau.android.lifekau.manager;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Calendar;
import java.util.Map;

public class LibraryManager {

    private static final int TOTAL_READING_ROOM_NUM = 5;
    private static final int TOTAL_READING_ROOM_SEAT_NUM = 300;
    private static final int TOTAL_STDUYING_ROOM_NUM = 6;
    private static final int TOTAL_STDUYING_ROOM_STATUS_NUM = 50;

    private int[] mReadingRoomAvailableSeat;
    private int[] mReadingRoomTotalSeat;
    private String[] mReadingRoomName;
    private boolean[][] mReadingRoomDetailStatus;
    private Point[][] mReadingRoomPoint;
    private String[] mStudyRoomName;
    private boolean[][] mStudyRoomDetailStatus;

    private LibraryManager() {
        mReadingRoomAvailableSeat = new int[TOTAL_READING_ROOM_NUM + 1];
        mReadingRoomTotalSeat = new int[TOTAL_READING_ROOM_NUM + 1];
        mReadingRoomName = new String[TOTAL_READING_ROOM_NUM + 1];
        mReadingRoomDetailStatus = new boolean[TOTAL_READING_ROOM_NUM + 1][TOTAL_READING_ROOM_SEAT_NUM];
        mReadingRoomPoint = new Point[TOTAL_READING_ROOM_NUM + 1][TOTAL_READING_ROOM_SEAT_NUM];
        mStudyRoomName = new String[TOTAL_STDUYING_ROOM_NUM + 1];
        mStudyRoomDetailStatus = new boolean[TOTAL_STDUYING_ROOM_NUM + 1][TOTAL_STDUYING_ROOM_STATUS_NUM];
    }

    private static class LazyHolder{
        private static LibraryManager INSTANCE = new LibraryManager();
    }

    public static LibraryManager getInstance(){
        return LazyHolder.INSTANCE;
    }

    public int getReadingRoomStatus(Context context) {
        Resources resources = context.getResources();
        try {
            Connection.Response res = Jsoup.connect("http://lib.kau.ac.kr/HAULMS/haulms/RoomRsv.csp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .data("HLOC", "HAULMS", "COUNT", "1qa9R5zy00", "Kor", "1")
                    .timeout(3000)
                    .execute();
            res = Jsoup.connect("http://ebook.kau.ac.kr:81/domian5.asp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(res.cookies())
                    .timeout(3000)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharset(res.charset())));
            for (int i = 0; i < TOTAL_READING_ROOM_NUM; i++) {
                String[] strings = doc.select("tr").get(i + 3).text().split("\\s+");
                mReadingRoomName[i] = strings[1];
                mReadingRoomTotalSeat[i] = Integer.parseInt(strings[2]);
                mReadingRoomAvailableSeat[i] = Integer.parseInt(strings[4]);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int getReadingRoomDetailStatus(Context context, int roomNum) {
        try {
            Connection.Response res = Jsoup.connect("http://ebook.kau.ac.kr:81/roomview5.asp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .data("room_no", String.valueOf(roomNum + 1))
                    .timeout(3000)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharset(res.charset())));
            Elements seatElements = doc.select("div");
            Elements styleElements = doc.select("div").select("td");
            for (int i = 2; i < seatElements.size(); i++) {
                int seatNum = Integer.parseInt(seatElements.get(i).text());
                mReadingRoomDetailStatus[roomNum][seatNum] = styleElements.get(i - 2).attr("bgcolor").equals("gray");
                String[] strings = seatElements.get(i).attr("style").split("\\s+");
                String string = strings[1].split(":")[1].split("px")[0];
                mReadingRoomPoint[roomNum][seatNum] = new Point();
                mReadingRoomPoint[roomNum][seatNum].x = Integer.parseInt(string);
                string = strings[2].split(":")[1].split("px")[0];
                mReadingRoomPoint[roomNum][seatNum].y = Integer.parseInt(string);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public Point getReadingRoomSeatPoint(int roomNum, int seatNum) {
        return mReadingRoomPoint[roomNum][seatNum];
    }

    public String getReadingRoomName(int roomNum) {
        return mReadingRoomName[roomNum];
    }

    public int getReadingRoomAvailableSeat(int roomNum) {
        return mReadingRoomAvailableSeat[roomNum];
    }

    public Boolean getReadingRoomDetailStatus(int roomNum, int seatNum) {
        return mReadingRoomDetailStatus[roomNum][seatNum];
    }

    public int getReadingRoomUsedSeat(int roomNum) {
        return mReadingRoomTotalSeat[roomNum] - mReadingRoomAvailableSeat[roomNum];
    }

    public int getReadingRoomTotalSeat(int roomNum) {
        return mReadingRoomTotalSeat[roomNum];
    }

    public String getReadingRoomSummary(int roomNum) {
        return "(" + String.valueOf(getReadingRoomAvailableSeat(roomNum)) + " / " + String.valueOf(getReadingRoomTotalSeat(roomNum)) + ")";
    }

    public int getStudyRoomStatus(Context context) {
        try {
            Connection.Response res = Jsoup.connect("http://lib.kau.ac.kr/haulms/haulms/SRResv.csp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .timeout(3000)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharset(res.charset())));
            Elements studyRoomName = doc.getElementsByAttributeValue("class", "clsRoomBox");
            for (int i = 0, size = studyRoomName.size(); i < size; i++)
                mStudyRoomName[i] = studyRoomName.get(i).text();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int getStudyRoomDetailStatus(Context context, int roomNum) {
         String[] STUDY_ROOM_CODE = {"STD-A", "STD-B1", "STD-B2", "STD-C1", "STD-C2", "STD-C3"};
        try {
            Connection.Response res = Jsoup.connect("http://lib.kau.ac.kr/haulms/haulms/SRResv.csp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .timeout(3000)
                    .execute();
            Map<String, String> cookies = res.cookies();
            res = Jsoup.connect("http://lib.kau.ac.kr/haulms/haulms/kauclSRResvResult.csp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(cookies)
                    .data("jc", "getTimes", "rc", STUDY_ROOM_CODE[roomNum], "uid", "GUEST")
                    .method(Connection.Method.POST)
                    .timeout(3000)
                    .execute();
            String[] strings = new String(res.bodyAsBytes(), getMatchingCharset(res.charset())).split("\u0011");
            for (int i = 3; i < strings.length; i++) {
                String[] infomation = strings[i].split("\\^");
                mStudyRoomDetailStatus[roomNum][Integer.parseInt(infomation[0])] = infomation[2].equalsIgnoreCase("+");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public String getStudyRoomName(int roomNum) {
        return mStudyRoomName[roomNum];
    }

    public Boolean getStudyRoomDetailStatus(int roomNum, int time) {
        return mStudyRoomDetailStatus[roomNum][time];
    }

    public String getStudyRoomSummary(int roomNum) {
        Calendar currTime = Calendar.getInstance();
        int currHour = currTime.get(Calendar.HOUR_OF_DAY);
        currHour = currHour < 0 ? 0 : currHour;
        return "(" + (mStudyRoomDetailStatus[roomNum][currHour] ? "이용 가능" : "이용 불가") + ")";
    }

    private String getMatchingCharset(String charset) {
        final String[] ENCODE_TYPE = {"EUC-KR", "KSC5601", "X-WINDOWS-949", "ISO-8859-1", "UTF-8"};
        String res = ENCODE_TYPE[0];
        for (String encodeType : ENCODE_TYPE) {
            if (encodeType.equalsIgnoreCase(charset)) {
                res = encodeType;
                break;
            }
        }
        return res;
    }
}