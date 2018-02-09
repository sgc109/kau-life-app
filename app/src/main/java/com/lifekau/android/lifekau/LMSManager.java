package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.res.Resources;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

public class LMSManager {

    private final int MAX_SUBJECT_NUM = 21;

    private String mStudentId;
    private Map<String, String> mCookies;

    private LMSManager(){
        mCookies = null;
    }

    private static class LazyHolder{
        private static final LMSManager INSTANCE = new LMSManager();
    }

    public static LMSManager getInstance(){
        return LazyHolder.INSTANCE;
    }

    public Map<String, String> getCookies(Context context, String id, String password){
        Resources resources = context.getResources();
        try {
            Connection.Response res = Jsoup.connect(resources.getString(R.string.lms_jsoup_lms_check_page))
                    .header("Accept", resources.getString(R.string.lms_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.lms_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.lms_jsoup_header_accept_language))
                    .userAgent(resources.getString(R.string.lms_jsoup_user_agent))
                    .execute();
            res = Jsoup.connect(resources.getString(R.string.lms_jsoup_act_login_page))
                    .header("Accept", resources.getString(R.string.lms_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.lms_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.lms_jsoup_header_accept_language))
                    .userAgent(resources.getString(R.string.lms_jsoup_user_agent))
                    .referrer(resources.getString(R.string.lms_jsoup_lms_check_page))
                    .data("target_page", "act_Lms_Check.jsp@chk1-1")
                    .data("refer_page", "")
                    .data("SessionID", "")
                    .data("SessionRequestData", "")
                    .data("AlgID", "SEED")
                    .data("ppage", "")
                    .data("p_id", id)
                    .data("p_pwd", password)
                    .method(Connection.Method.POST)
                    .cookies(res.cookies())
                    .execute();
            String string = new String(res.bodyAsBytes(), getMatchingCharSet(res.charset()));
            String[] strings = string.split("\'");
            res = Jsoup.connect(resources.getString(R.string.lms_jsoup_lms_sso_page))
                    .header("Accept", resources.getString(R.string.lms_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.lms_jsoup_header_accept_encoding))
                    .header("Accept-Language", resources.getString(R.string.lms_jsoup_header_accept_language))
                    .userAgent(resources.getString(R.string.lms_jsoup_user_agent))
                    .cookies(res.cookies())
                    .data("seq_id", strings[3])
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            res = Jsoup.connect(resources.getString(R.string.lms_jsoup_lms_login_page))
                    .header("Accept", resources.getString(R.string.lms_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.lms_jsoup_header_accept_encoding))
                    .header("Accept-Language", resources.getString(R.string.lms_jsoup_header_accept_language))
                    .userAgent(resources.getString(R.string.lms_jsoup_user_agent))
                    .cookies(res.cookies())
                    .data("username", doc.select("input").get(0).attr("value"))
                    .data("password", doc.select("input").get(1).attr("value"))
                    .method(Connection.Method.POST)
                    .execute();
            res = Jsoup.connect(resources.getString(R.string.lms_jsoup_lms_login_index_page))
                    .header("Accept", resources.getString(R.string.lms_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.lms_jsoup_header_accept_encoding))
                    .header("Accept-Language", resources.getString(R.string.lms_jsoup_header_accept_language))
                    .userAgent(resources.getString(R.string.lms_jsoup_user_agent))
                    .cookies(mCookies = res.cookies())
                    .data("testsession", "4837")
                    .execute();
            doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
        } catch (Exception e) {
            e.printStackTrace();
            return mCookies = null;
        }
        return mCookies;
    }

    public int checkCookieValid(){
        return 0;
    }

    public String getStudentId(Context context){
        Resources resources = context.getResources();
        try {
            Connection.Response res = Jsoup.connect(resources.getString(R.string.lms_jsoup_lms_my_page))
                    .header("Accept", resources.getString(R.string.lms_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.lms_jsoup_header_accept_encoding))
                    .header("Accept-Language", resources.getString(R.string.lms_jsoup_header_accept_language))
                    .userAgent(resources.getString(R.string.lms_jsoup_user_agent))
                    .cookies(mCookies)
                    .referrer(resources.getString(R.string.lms_jsoup_lms_my_page))
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            mStudentId = doc.select("#loggedin-user").get(0).getElementsByAttributeValue("class", "dropdown-toggle").text().replaceAll("[^0-9]", "");
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return mStudentId;
    }

    public String getMatchingCharSet(String charset){
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