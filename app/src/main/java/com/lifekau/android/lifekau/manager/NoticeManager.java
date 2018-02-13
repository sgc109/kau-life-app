package com.lifekau.android.lifekau.manager;

import com.lifekau.android.lifekau.model.Notice;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class NoticeManager {

    private final static String[] COMMUNITY_KEY = {"B0146", "B0147", "B0230", "B0259", "B0148"};
    private final static String[] NOTICE_LIST = {"general_list", "academicinfo_list", "scholarship_list", "career_list", "event_list"};

    private ArrayList<Notice> mGeneralNoticeList;
    private ArrayList<Notice> mBachelorNoticeList;
    private ArrayList<Notice> mScholarshipLoanNoticeList;
    private ArrayList<Notice> mEmploymentNoticeList;
    private ArrayList<Notice> mEventNoticeList;

    private NoticeManager() {
        mGeneralNoticeList = new ArrayList<>();
        mBachelorNoticeList = new ArrayList<>();
        mScholarshipLoanNoticeList = new ArrayList<>();
        mEmploymentNoticeList = new ArrayList<>();
        mEventNoticeList = new ArrayList<>();
    }

    private static class LazyHolder {
        private static NoticeManager INSTANCE = new NoticeManager();
    }

    public static NoticeManager getInstance(){
        return LazyHolder.INSTANCE;
    }

    public void getNotice(int listIndex, int pageNum) {
        ArrayList<Notice> currNoticeList = mGeneralNoticeList;
        if(listIndex == 1) currNoticeList = mGeneralNoticeList;
        if(listIndex == 2) currNoticeList = mBachelorNoticeList;
        if(listIndex == 3) currNoticeList = mScholarshipLoanNoticeList;
        if(listIndex == 4) currNoticeList = mEmploymentNoticeList;
        if(listIndex == 5) currNoticeList = mEventNoticeList;
        try {
            Connection.Response res = Jsoup.connect("http://www.kau.ac.kr/page/kauspace/" + NOTICE_LIST[listIndex] + ".jsp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .referrer("http://www.kau.ac.kr/page/kauspace/" + NOTICE_LIST[listIndex] + ".jsp")
                    .data("communityKey", COMMUNITY_KEY[listIndex])
                    .data("pageNum", String.valueOf(pageNum))
                    .data("pageSize", "10")
                    .data("act", "LIST")
                    .data("boardId", "")
                    .data("branch_session", "")
                    .data("only_reply", "")
                    .data("mbo_mother_page", "/page/kauspace/" + NOTICE_LIST[listIndex])
                    .data("board_table_name", "WCM_BOARD_" + COMMUNITY_KEY[listIndex])
                    .data("sort_type", "DESC")
                    .data("sort_column", "")
                    .data("memoTable", "WCM_BOARD_MEMO" + COMMUNITY_KEY[listIndex])
                    .data("login_id", "")
                    .data("searchType", "TITLE")
                    .data("searchWord", "")
                    .data("chg_page_size", "10")
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            Elements elements = doc.getElementsByAttributeValue("id", "board_form").select("tbody").select("tr");
            int elementsSize = elements.size();
            for (int i = 0; i < elementsSize; i++) {
                Elements infomation = elements.get(i).select("td");
                Notice insertNotice = new Notice();
                insertNotice.postNum = Integer.valueOf(infomation.get(0).text().equals("") ? "0" : infomation.get(0).text());
                insertNotice.postDetailNum = Integer.valueOf(infomation.get(1).select("a").attr("href").replaceAll("[^0-9]", ""));
                insertNotice.postTitle = infomation.get(1).text();
                insertNotice.writer = infomation.get(2).text();
                insertNotice.RegistrationDate = infomation.get(3).text();
                currNoticeList.add(insertNotice);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getMatchingCharSet(String charset) {
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
