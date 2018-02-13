package com.lifekau.android.lifekau.manager;

import android.util.Log;

import com.lifekau.android.lifekau.model.Notice;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map;

public class NoticeManager {

    private final static int TOTAL_NOTICE_NUM = 5;
    private final static String[] COMMUNITY_KEY = {"B0146", "B0147", "B0230", "B0259", "B0148"};
    private final static String[] URL_TYPE = {"/page/kauspace/", "/page/kau_media/"};
    private final static String[] NOTICE_LIST = {"general_list", "academicinfo_list", "scholarship_list", "career_list", "event_list"};

    private int[] mLatestPageNum;
    private Hashtable<Integer, Notice>[] mNoticeListMap;
    private ArrayList<Notice>[] mImportantNoticeList;

    private NoticeManager() {
        mLatestPageNum = new int[TOTAL_NOTICE_NUM];
        mNoticeListMap = new Hashtable[TOTAL_NOTICE_NUM];
        mImportantNoticeList = new ArrayList[TOTAL_NOTICE_NUM];
        for(int i = 0; i < TOTAL_NOTICE_NUM; i++) mNoticeListMap[i] = new Hashtable<>();
        for(int i = 0; i < TOTAL_NOTICE_NUM; i++) mImportantNoticeList[i] = new ArrayList<>();
    }

    private static class LazyHolder {
        private static NoticeManager INSTANCE = new NoticeManager();
    }

    public static NoticeManager getInstance(){
        return LazyHolder.INSTANCE;
    }

    public void getNotice(int noticeType, int pageNum) {
        Hashtable<Integer, Notice> currNoticeList = null;
        for(int i = 0; i < TOTAL_NOTICE_NUM; i++){
            if(noticeType != i) continue;
            currNoticeList = mNoticeListMap[i];
            break;
        }
        try {
            Connection.Response res = Jsoup.connect("http://www.kau.ac.kr" + URL_TYPE[noticeType != 3 ? 0 : 1] + NOTICE_LIST[noticeType] + ".jsp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .referrer("http://www.kau.ac.kr" + URL_TYPE[noticeType != 3 ? 0 : 1] + NOTICE_LIST[noticeType] + ".jsp")
                    .data("communityKey", COMMUNITY_KEY[noticeType])
                    .data("pageNum", String.valueOf(pageNum))
                    .data("pageSize", "10")
                    .data("act", "LIST")
                    .data("boardId", "")
                    .data("branch_session", "")
                    .data("only_reply", "")
                    .data("mbo_mother_page", URL_TYPE[noticeType != 3 ? 0 : 1] + NOTICE_LIST[noticeType])
                    .data("board_table_name", "WCM_BOARD_" + COMMUNITY_KEY[noticeType])
                    .data("sort_type", "DESC")
                    .data("sort_column", "")
                    .data("memoTable", "WCM_BOARD_MEMO" + COMMUNITY_KEY[noticeType])
                    .data("login_id", "")
                    .data("searchType", "TITLE")
                    .data("searchWord", "")
                    .data("chg_page_size", "10")
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            Elements postElements = doc.getElementsByAttributeValue("id", "board_form").select("tbody").select("tr");
            int postElementsSize = postElements.size();
            for (int i = 0; i < postElementsSize; i++) {
                Elements pageDataElements = postElements.get(i).select("td");
                Notice insertedNotice = new Notice();
                insertedNotice.postNum = Integer.valueOf(pageDataElements.get(0).text().equals("") ? "0" : pageDataElements.get(0).text());
                insertedNotice.postDetailNum = Integer.valueOf(pageDataElements.get(1).select("a").attr("href").replaceAll("[^0-9]", ""));
                insertedNotice.postTitle = pageDataElements.get(1).text();
                insertedNotice.writer = pageDataElements.get(2).text();
                insertedNotice.RegistrationDate = pageDataElements.get(3).text();
                mLatestPageNum[noticeType] = Math.max(mLatestPageNum[noticeType], insertedNotice.postNum);
                if(insertedNotice.postNum == 0) mImportantNoticeList[noticeType].add(insertedNotice);
                else currNoticeList.put(insertedNotice.postNum, insertedNotice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getListCount(int noticeType){
        return mNoticeListMap[noticeType].size() + mImportantNoticeList[noticeType].size();
    }
    
    public String getListPageTitle(int noticeType, int index){
        if(index < mImportantNoticeList[noticeType].size()) return mImportantNoticeList[noticeType].get(index).postTitle;
        return mNoticeListMap[noticeType].get(mLatestPageNum[noticeType] - index + mImportantNoticeList[noticeType].size()).postTitle;
    }

    public String getListPageWriter(int noticeType, int index){
        if(index < mImportantNoticeList[noticeType].size()) return mImportantNoticeList[noticeType].get(index).writer;
        return mNoticeListMap[noticeType].get(mLatestPageNum[noticeType] - index + mImportantNoticeList[noticeType].size()).writer;
    }

    public String getListPageRegistrationDate(int noticeType, int index){
        if(index < mImportantNoticeList[noticeType].size()) return mImportantNoticeList[noticeType].get(index).RegistrationDate;
        return mNoticeListMap[noticeType].get(mLatestPageNum[noticeType] - index + mImportantNoticeList[noticeType].size()).RegistrationDate;
    }

    public int getListPageDetailNum(int noticeType, int index){
        if(index < mImportantNoticeList[noticeType].size()) return mImportantNoticeList[noticeType].get(index).postDetailNum;
        return mNoticeListMap[noticeType].get(mLatestPageNum[noticeType] - index + mImportantNoticeList[noticeType].size()).postDetailNum;
    }

    public String getMatchingCharSet(String charset) {
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
