package com.lifekau.android.lifekau;

import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

//TODO: 코드 리팩토링이 필요하다.

public class LibraryInfomation {

    private static String[] ENCODE_TYPE = {"EUC-KR", "KSC5601", "X-WINDOWS-949", "ISO-8859-1", "UTF-8"};
    private static int TOTAL_READING_ROOM_NUM = 5;
    private static int TOTAL_STDUYING_ROOM_NUM = 6;
    private int[] mReadingRoomAvailableSeat;
    private int[] mReadingRoomTotalSeat;
    private String[] mReadingRoomName;
    private String[] mStudyRoomName;
    private boolean[][] mReadingRoomStatus;
    private boolean[][] mStudyRoomStatus;

    LibraryInfomation() {
        mReadingRoomAvailableSeat = new int[TOTAL_READING_ROOM_NUM + 1];
        mReadingRoomTotalSeat = new int[TOTAL_READING_ROOM_NUM + 1];
        mReadingRoomName = new String[TOTAL_READING_ROOM_NUM + 1];
        mReadingRoomStatus = new boolean[TOTAL_READING_ROOM_NUM + 1][300];
        mStudyRoomName = new String[TOTAL_STDUYING_ROOM_NUM + 1];
        mStudyRoomStatus = new boolean[TOTAL_STDUYING_ROOM_NUM + 1][50];
    }

    public Integer getReadingRoomStatus() {
        try {
            Response res = Jsoup.connect("http://lib.kau.ac.kr/HAULMS/haulms/RoomRsv.csp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .data("HLOC", "HAULMS", "COUNT", "1qa9R5zy00", "Kor", "1")
                    .execute();
            Map<String, String> cookies = res.cookies();
            res = Jsoup.connect("http://ebook.kau.ac.kr:81/domian5.asp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(cookies)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharset(res.charset())));
            for(int i = 1; i <= TOTAL_READING_ROOM_NUM; i++) {
                String[] strings = doc.select("tr").get(i + 2).text().split("\\s+");
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

    public Integer getReadingRoomDetailStatus(Integer index){
        if(index > TOTAL_READING_ROOM_NUM) return -1;
        if(index <= 0) return -1;
        try {
            Response res = Jsoup.connect("http://lib.kau.ac.kr/HAULMS/haulms/RoomRsv.csp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .data("HLOC", "HAULMS", "COUNT", "1qa9R5zy00", "Kor", "1")
                    .execute();
            Map<String, String> cookies = res.cookies();
            res = Jsoup.connect("http://ebook.kau.ac.kr:81/roomview5.asp?room_no=" + String.valueOf(index))
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(cookies)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharset(res.charset())));
            List<String> avail = doc.select("div").select("td").eachAttr("bgcolor");
            List<String> seatNum = doc.select("div").select("td").select("font").eachText();
            for(int i = 0; i < avail.size(); i++){
                mReadingRoomStatus[index][Integer.parseInt(seatNum.get(i))] = avail.get(i).equals("red");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public String getReadingRoomName(Integer index) {
        if(index > TOTAL_READING_ROOM_NUM) return "not available";
        if(index <= 0) return "not available";
        return mReadingRoomName[index];
    }

    public Integer getReadingRoomAvailableSeat(Integer index){
        if(index > TOTAL_READING_ROOM_NUM) return -1;
        if(index <= 0) return -1;
        return mReadingRoomAvailableSeat[index];
    }

    public Integer getReadingRoomUsedSeat(Integer index){
        return mReadingRoomTotalSeat[index] - mReadingRoomAvailableSeat[index];
    }

    public Integer getReadingRoomTotalSeat(Integer index){
        if(index > TOTAL_READING_ROOM_NUM) return -1;
        if(index <= 0) return -1;
        return mReadingRoomTotalSeat[index];
    }

    public String getReadingRoomSummary(Integer index){
        if(index > TOTAL_READING_ROOM_NUM) return "not available";
        if(index <= 0) return "not available";
        return getReadingRoomName(index) + "(" + String.valueOf(getReadingRoomAvailableSeat(index)) + " / " + String.valueOf(getReadingRoomTotalSeat(index)) + ")";
    }

    public Integer getStudyRoomStatus(){
        try {
            Response res = Jsoup.connect("http://lib.kau.ac.kr/haulms/haulms/SRResv.csp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharset(res.charset())));
            List<String> strings = doc.select("#roomSTD-A").eachText();
            for(int i = 0, size = strings.size(); i < size; i++) mStudyRoomName[i + 1] = strings.get(i);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public Integer getStudyRoomDetailStatus(Integer index){
        final String[] STUDY_ROOM_CODE = {"", "STD-A", "STD-B1", "STD-B2", "STD-C1", "STD-C2", "STD-C3"};
        if(index > TOTAL_STDUYING_ROOM_NUM) return -1;
        if(index <= 0) return -1;
        try {
            Response res = Jsoup.connect("http://lib.kau.ac.kr/haulms/haulms/SRResv.csp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .execute();
            Map<String, String> cookies = res.cookies();
            res = Jsoup.connect("http://lib.kau.ac.kr/haulms/haulms/kauclSRResvResult.csp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(cookies)
                    .data("jc", "getTimes", "rc", STUDY_ROOM_CODE[index], "uid", "GUEST")
                    .method(Connection.Method.POST)
                    .execute();
            String[] strings = new String(res.bodyAsBytes(), getMatchingCharset(res.charset())).split("\u0011");
            for(int i = 3; i < strings.length; i++){
                String[] infomation = strings[i].split("\\^");
                mStudyRoomStatus[index][Integer.parseInt(infomation[0])] = infomation[2].equalsIgnoreCase("+");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    private String getMatchingCharset(String charset){
        final String[] ENCODE_TYPE = {"EUC-KR", "KSC5601", "X-WINDOWS-949", "ISO-8859-1", "UTF-8"};
        String res = ENCODE_TYPE[0];
        for(String encodeType : ENCODE_TYPE) {
            if (encodeType.equalsIgnoreCase(charset)) {
                res = encodeType;
                break;
            }
        }
        return res;
    }
}