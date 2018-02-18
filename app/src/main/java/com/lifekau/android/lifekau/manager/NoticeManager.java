package com.lifekau.android.lifekau.manager;

import android.util.Log;

import com.lifekau.android.lifekau.model.Notice;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Hashtable;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoticeManager {

    private final static int TOTAL_NOTICE_NUM = 5;
    private final static String[] COMMUNITY_KEY = {"B0146", "B0147", "B0230", "B0259", "B0148"};
    private final static String[] URL_TYPE = {"/page/kauspace/", "/page/kau_media/"};
    private final static String[] NOTICE_LIST = {"general_list", "academicinfo_list", "scholarship_list", "career_list", "event_list"};

    private boolean[] mAllPageFetched;
    private int[] mLatestPageNum;
    private int[] mLoadedListNum;
    private Hashtable<Integer, Notice>[] mNoticeListMap;
    private ArrayList<Notice>[] mImportantNoticeList;

    private NoticeManager() {
        mAllPageFetched = new boolean[TOTAL_NOTICE_NUM];
        mLatestPageNum = new int[TOTAL_NOTICE_NUM];
        mLoadedListNum = new int[TOTAL_NOTICE_NUM];
        mNoticeListMap = new Hashtable[TOTAL_NOTICE_NUM];
        mImportantNoticeList = new ArrayList[TOTAL_NOTICE_NUM];
        for (int i = 0; i < TOTAL_NOTICE_NUM; i++) mNoticeListMap[i] = new Hashtable<>();
        for (int i = 0; i < TOTAL_NOTICE_NUM; i++) mImportantNoticeList[i] = new ArrayList<>();
    }

    private static class LazyHolder {
        private static NoticeManager INSTANCE = new NoticeManager();
    }

    public static NoticeManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public int getNoticeList(int noticeType) {
        if (getNoticeList(noticeType, mLoadedListNum[noticeType] + 1) == 0) {
            mLoadedListNum[noticeType]++;
            return 0;
        }
        return -1;
    }

    public int getNoticeList(int noticeType, int listNum) {
        Hashtable<Integer, Notice> currNoticeList = null;
        for (int i = 0; i < TOTAL_NOTICE_NUM; i++) {
            if (noticeType != i) continue;
            currNoticeList = mNoticeListMap[i];
            break;
        }
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("communityKey", COMMUNITY_KEY[noticeType])
                .add("pageNum", String.valueOf(listNum))
                .add("pageSize", "10")
                .add("act", "LIST")
                .add("boardId", "")
                .add("branch_session", "")
                .add("only_reply", "")
                .add("mbo_mother_page", URL_TYPE[noticeType != 3 ? 0 : 1] + NOTICE_LIST[noticeType])
                .add("board_table_name", "WCM_BOARD_" + COMMUNITY_KEY[noticeType])
                .add("sort_type", "DESC")
                .add("sort_column", "")
                .add("memoTable", "WCM_BOARD_MEMO" + COMMUNITY_KEY[noticeType])
                .add("login_id", "")
                .add("searchType", "TITLE")
                .add("searchWord", "")
                .add("chg_page_size", "10")
                .build();
        Request request = new Request.Builder()
                .url("http://www.kau.ac.kr" + URL_TYPE[noticeType != 3 ? 0 : 1] + NOTICE_LIST[noticeType] + ".jsp")
                .post(body)
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
            Document doc = Jsoup.parse(res.body().string());
            Elements postElements = doc.getElementsByAttributeValue("id", "board_form").select("tbody").select("tr");
            int postElementsSize = postElements.size();
            if (postElementsSize == 1 && postElements.get(0).select("td").get(0).text().equals("등록된 글이 없습니다.")) {
                mAllPageFetched[noticeType] = true;
                return 0;
            }
            for (int i = 0; i < postElementsSize; i++) {
                Elements pageDataElements = postElements.get(i).select("td");
                Notice insertedNotice = new Notice();
                insertedNotice.postNum = Integer.valueOf(pageDataElements.get(0).text().equals("") ? "0" : pageDataElements.get(0).text());
                insertedNotice.postDetailNum = Integer.valueOf(pageDataElements.get(1).select("a").attr("href").replaceAll("[^0-9]", ""));
                insertedNotice.postTitle = pageDataElements.get(1).text();
                insertedNotice.writer = pageDataElements.get(2).text();
                insertedNotice.RegistrationDate = pageDataElements.get(3).text();
                mLatestPageNum[noticeType] = Math.max(mLatestPageNum[noticeType], insertedNotice.postNum);
                if (insertedNotice.postNum == 0)
                    mImportantNoticeList[noticeType].add(insertedNotice);
                else currNoticeList.put(insertedNotice.postNum, insertedNotice);
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public void reset(int noticeType) {
        mLatestPageNum[noticeType] = 0;
        mLoadedListNum[noticeType] = 0;
        mImportantNoticeList[noticeType].clear();
        mNoticeListMap[noticeType].clear();
        mAllPageFetched[noticeType] = false;
    }

    public int getListCount(int noticeType) {
        return mNoticeListMap[noticeType].size() + mImportantNoticeList[noticeType].size() + (mAllPageFetched[noticeType] ? 0 : 1);
    }

    public boolean getAllPageFetched(int noticeType) {
        return mAllPageFetched[noticeType];
    }

    public Notice getNotice(int noticeType, int index) {
        if (index < mImportantNoticeList[noticeType].size())
            return mImportantNoticeList[noticeType].get(index);
        return mNoticeListMap[noticeType].get(mLatestPageNum[noticeType] - index + mImportantNoticeList[noticeType].size());
    }

    public String getURL(int noticeType) {
        return "http://www.kau.ac.kr" + URL_TYPE[noticeType != 3 ? 0 : 1] + NOTICE_LIST[noticeType] + ".jsp";
    }

    public String getPOST(int noticeType, int index) {
        int detailNum;
        if (index < mImportantNoticeList[noticeType].size())
            detailNum = mImportantNoticeList[noticeType].get(index).postDetailNum;
        else
            detailNum = mNoticeListMap[noticeType].get(mLatestPageNum[noticeType] - index + mImportantNoticeList[noticeType].size()).postDetailNum;
        String[] parameters = {"communityKey", "pageNum", "pageSize", "act", "boardId", "branch_session",
                "only_reply", "mbo_mother_page", "board_table_name", "sort_type", "sort_column", "memoTable",
                "login_id", "searchType", "searchWord", "chg_page_size"};
        String[] values = {COMMUNITY_KEY[noticeType], "", "10", "VIEW", String.valueOf(detailNum), "",
                "", URL_TYPE[noticeType != 3 ? 0 : 1] + NOTICE_LIST[noticeType], "WCM_BOARD_" + COMMUNITY_KEY[noticeType], "DESC", "",
                "WCM_BOARD_MEMO" + COMMUNITY_KEY[noticeType], "", "TITLE", "", "10"};
        String postData = parameters[0] + "=" + values[0];
        StringBuffer stringBuffer = new StringBuffer(postData);
        for (int i = 1; i < parameters.length; i++) {
            stringBuffer.append("&");
            stringBuffer.append(parameters[i]);
            stringBuffer.append("=");
            stringBuffer.append(values[i]);
        }
        return stringBuffer.toString();
    }
}
