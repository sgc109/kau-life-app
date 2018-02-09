package com.lifekau.android.lifekau;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PortalManager {

    private static final int EXAMIANATION_TIME_TABLE_ROW = 13;
    private static final int EXAMIANATION_TIME_TABLE_COL = 7;

    private ArrayList<Scholarship> mScholarshipArray;
    private ArrayList<CurrGrade> mCurrGrade;
    private TotalCurrGrade mTotalCurrGrade;
    private ArrayList<AccumulatedGrade> mAccumulatedGradeArray;
    private ArrayList<AccumulatedGradeSummary> mAccumulatedGradeSummaryArray;
    private TotalAccumulatedGrade mTotalAccumulatedGrade;
    private String[][] mExaminationTimeTable;
    private Map<String, String> mCookies;

    private PortalManager() {
        mScholarshipArray = new ArrayList<Scholarship>();
        mCurrGrade = new ArrayList<CurrGrade>();
        mTotalCurrGrade = new TotalCurrGrade();
        mAccumulatedGradeArray = new ArrayList<AccumulatedGrade>();
        mAccumulatedGradeSummaryArray = new ArrayList<AccumulatedGradeSummary>();
        mTotalAccumulatedGrade = new TotalAccumulatedGrade();
        mExaminationTimeTable = new String[EXAMIANATION_TIME_TABLE_ROW][EXAMIANATION_TIME_TABLE_COL];
    }

    private static class LazyHolder{
        public static final PortalManager INSTANCE = new PortalManager();
    }

    public static PortalManager getInstance(){
        return LazyHolder.INSTANCE;
    }

    public Map<String, String> getSession(Context context, String id, String password) {
        Resources resources = context.getResources();
        Map<String, String> cookies = null;
        try {
            Connection.Response res = Jsoup.connect(resources.getString(R.string.portal_jsoup_lms_check_page))
                    .header("Accept", resources.getString(R.string.portal_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.portal_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.portal_jsoup_header_accpet_language))
                    .userAgent(resources.getString(R.string.portal_jsoup_user_agent))
                    .execute();
            res = Jsoup.connect(resources.getString(R.string.portal_jsoup_act_login_page))
                    .header("Accept", resources.getString(R.string.portal_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.portal_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.portal_jsoup_header_accpet_language))
                    .userAgent(resources.getString(R.string.portal_jsoup_user_agent))
                    .referrer(resources.getString(R.string.portal_jsoup_lms_check_page))
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
            res = Jsoup.connect(resources.getString(R.string.portal_jsoup_portal_check_page))
                    .header("Accept", resources.getString(R.string.portal_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.portal_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.portal_jsoup_header_accpet_language))
                    .userAgent(resources.getString(R.string.portal_jsoup_user_agent))
                    .data("chk1", "1")
                    .cookies(res.cookies())
                    .execute();
            res = Jsoup.connect(resources.getString(R.string.portal_jsoup_portal_login_page))
                    .header("Accept", resources.getString(R.string.portal_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.portal_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.portal_jsoup_header_accpet_language))
                    .userAgent(resources.getString(R.string.portal_jsoup_user_agent))
                    .referrer(resources.getString(R.string.portal_jsoup_portal_check_page))
                    .data("seq_id", strings[3])
                    .data("ppage", "")
                    .cookies(res.cookies())
                    .validateTLSCertificates(false)
                    .execute();
            cookies = res.cookies();
            res = Jsoup.connect(resources.getString(R.string.portal_jsoup_my_portal_page))
                    .header("Accept", resources.getString(R.string.portal_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.portal_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.portal_jsoup_header_accpet_language))
                    .userAgent(resources.getString(R.string.portal_jsoup_user_agent))
                    .referrer(resources.getString(R.string.portal_jsoup_portal_login_page) + "?seq_id=" + strings[3] + "&ppage=")
                    .cookies(cookies)
                    .validateTLSCertificates(false)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
            return mCookies = null;
        }
        return mCookies = cookies;
    }

    public int checkSessionVaild(){
        return 0;
    }

    public int getScholarshipInfomation(Context context) {
        Resources resources = context.getResources();
        try {
            Connection.Response res = Jsoup.connect(resources.getString(R.string.portal_jsoup_scholar_page))
                    .header("Accept", resources.getString(R.string.portal_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.portal_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.portal_jsoup_header_accpet_language))
                    .userAgent(resources.getString(R.string.portal_jsoup_user_agent))
                    .referrer(resources.getString(R.string.portal_jsoup_my_menu_b_page))
                    .cookies(mCookies)
                    .validateTLSCertificates(false)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            Elements elements = doc.getElementsByAttributeValue("class", "table1").select("tr");
            mScholarshipArray.clear();
            int elementsSize = elements.size();
            for (int i = 1; i < elementsSize; i++) {
                Elements infomation = elements.get(i).select("td");
                Scholarship insert = new Scholarship();
                insert.semester = infomation.get(0).text();
                insert.categorization = infomation.get(1).text();
                insert.type = infomation.get(2).text();
                insert.amount = Integer.valueOf(TextUtils.join("", infomation.get(3).text().split(",")));
                mScholarshipArray.add(insert);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int getCurrGradeInfomation(Context context) {
        Resources resources = context.getResources();
        try {
            Connection.Response res = Jsoup.connect(resources.getString(R.string.portal_jsoup_curr_grade_page))
                    .header("Accept", resources.getString(R.string.portal_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.portal_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.portal_jsoup_header_accpet_language))
                    .userAgent(resources.getString(R.string.portal_jsoup_user_agent))
                    .referrer(resources.getString(R.string.portal_jsoup_my_menu_b_page))
                    .cookies(mCookies)
                    .validateTLSCertificates(false)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            Elements elements = doc.getElementsByAttributeValue("cellspacing", "1");
            mCurrGrade.clear();
            if (resources.getString(R.string.portal_curr_grade_no_data).equals(elements.get(0).select("tr").get(1).select("td").text())) return -1;
            int elementsSize = elements.size();
            for (int i = 0; i < elementsSize; i++) {
                Elements grades = elements.get(i).select("tr");
                int gradesSize = grades.size();
                for (int j = 1; j < gradesSize; j++) {
                    Elements infomation = grades.get(j).select("td");
                    CurrGrade grade = new CurrGrade();
                    grade.courseNumber = infomation.get(0).text();
                    grade.courseTitle = infomation.get(1).text();
                    grade.credits = infomation.get(2).text();
                    grade.evaluation = infomation.get(3).text();
                    grade.grade = infomation.get(4).text();
                    grade.major = infomation.get(5).text();
                    grade.portfolio = infomation.get(6).text();
                    grade.remarks = infomation.get(7).text();
                    grade.retake = infomation.get(8).text();
                    mCurrGrade.add(grade);
                }
            }
            Elements totalGradeSummary = elements.get(1).select("tr").get(1).select("td");
            mTotalCurrGrade.registeredCredits = Integer.valueOf(totalGradeSummary.get(0).text());
            mTotalCurrGrade.acquiredCredits = Integer.valueOf(totalGradeSummary.get(1).text());
            mTotalCurrGrade.totalGrades = Double.valueOf(totalGradeSummary.get(2).text());
            mTotalCurrGrade.GPA = Double.valueOf(totalGradeSummary.get(3).text());
            mTotalCurrGrade.semesterRanking = totalGradeSummary.get(4).text();
            mTotalCurrGrade.remarks = totalGradeSummary.get(5).text();

        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int getAccumulatedGrade(Context context, int year, int semesterCode) {
        Resources resources = context.getResources();
        try {
            Connection.Response res = Jsoup.connect(resources.getString(R.string.portal_jsoup_accumulated_grade_page) + "?guYear=" + year + "&guHakgi=" + semesterCode)
                    .header("Accept", resources.getString(R.string.portal_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.portal_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.portal_jsoup_header_accpet_language))
                    .userAgent(resources.getString(R.string.portal_jsoup_user_agent))
                    .referrer(resources.getString(R.string.portal_jsoup_my_menu_b_page))
                    .cookies(mCookies)
                    .validateTLSCertificates(false)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            mAccumulatedGradeArray.clear();
            Elements elements = doc.getElementsByAttributeValue("class", "table1");
            Elements gradeSummary = elements.get(1).select("tr");
            int gradeSummarySize = gradeSummary.size();
            for (int i = 2; i < gradeSummarySize; i++) {
                Elements data = gradeSummary.get(i).select("td");
                int adjustVal = (i == 2 ? 1 : 0);
                AccumulatedGrade accumulatedGrade = new AccumulatedGrade();
                accumulatedGrade.year = year;
                accumulatedGrade.semesterCode = semesterCode;
                accumulatedGrade.subjectCode = data.get(0 + adjustVal).text();
                accumulatedGrade.subjectTitle = data.get(1 + adjustVal).text();
                accumulatedGrade.professorName = data.get(2 + adjustVal).text();
                accumulatedGrade.type = data.get(3 + adjustVal).text();
                accumulatedGrade.credits = Integer.valueOf(data.get(4 + adjustVal).text());
                accumulatedGrade.grade = data.get(5 + adjustVal).text();
                accumulatedGrade.retake = data.get(6 + adjustVal).text();
                accumulatedGrade.remarks = data.get(7 + adjustVal).text();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int getAccumulatedGradeSummary(Context context) {
        Resources resources = context.getResources();
        try {
            Connection.Response res = Jsoup.connect(resources.getString(R.string.portal_jsoup_accumulated_grade_summary_page))
                    .header("Accept", resources.getString(R.string.portal_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.portal_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.portal_jsoup_header_accpet_language))
                    .userAgent(resources.getString(R.string.portal_jsoup_user_agent))
                    .referrer(resources.getString(R.string.portal_jsoup_my_menu_b_page))
                    .cookies(mCookies)
                    .validateTLSCertificates(false)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            mAccumulatedGradeSummaryArray.clear();
            Elements elements = doc.getElementsByAttributeValue("class", "table1");
            Elements gradeSummary = elements.get(0).select("tr");
            int gradeSummarySize = gradeSummary.size();
            for (int i = 1; i < gradeSummarySize; i++) {
                Elements data = gradeSummary.get(i).select("td");
                String[] strings = data.get(0).select("a").attr("href").split("\'");
                AccumulatedGradeSummary accumulatedGradeSummary = new AccumulatedGradeSummary();
                accumulatedGradeSummary.semester = data.get(0).text();
                accumulatedGradeSummary.year = Integer.valueOf(strings[1]);
                accumulatedGradeSummary.semesterCode = Integer.valueOf(strings[3]);
                accumulatedGradeSummary.registeredCredits = Integer.valueOf(data.get(1).text());
                accumulatedGradeSummary.acquiredCredits = Integer.valueOf(data.get(2).text());
                accumulatedGradeSummary.totalGrades = Double.valueOf(data.get(3).text());
                accumulatedGradeSummary.GPA = Double.valueOf(data.get(4).text());
                accumulatedGradeSummary.remarks = data.get(5).text();
                mAccumulatedGradeSummaryArray.add(accumulatedGradeSummary);
            }
            Elements totalGradeSummary = elements.get(1).select("tr").select("td");
            mTotalAccumulatedGrade.registeredCredits = Integer.valueOf(totalGradeSummary.get(2).text());
            mTotalAccumulatedGrade.acquiredCredits = Integer.valueOf(totalGradeSummary.get(4).text());
            mTotalAccumulatedGrade.totalGrades = Double.valueOf(totalGradeSummary.get(6).text());
            mTotalAccumulatedGrade.GPA = Double.valueOf(totalGradeSummary.get(8).text());
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int getExaminationTimeTable(Context context, int year, int semesterCode, int examCode) {
        Resources resources = context.getResources();
        try {
            Connection.Response res = Jsoup.connect(resources.getString(R.string.portal_jsoup_examination_time_table_page) + "?" + year + "hakgi=" + examCode)
                    .header("Accept", resources.getString(R.string.portal_jsoup_header_accept))
                    .header("Accept-Encoding", resources.getString(R.string.portal_jsoup_header_accept_encoding_with_br))
                    .header("Accept-Language", resources.getString(R.string.portal_jsoup_header_accpet_language))
                    .userAgent(resources.getString(R.string.portal_jsoup_user_agent))
                    .referrer(resources.getString(R.string.portal_jsoup_my_menu_b_page))
                    .data("year", String.valueOf(year))
                    .data("hakgi", String.valueOf(semesterCode))
                    .data("junggi_gb", String.valueOf(examCode))
                    .method(Connection.Method.POST)
                    .cookies(mCookies)
                    .validateTLSCertificates(false)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            Elements timeTableElements = doc.getElementsByAttributeValue("class", "table1").get(1).select("tr");
            if(timeTableElements.select("td").get(1).text().equals(resources.getString(R.string.portal_titme_table_no_data))) return -1;
            int timeTableElementsSize = timeTableElements.size();
            for (int i = 0; i < timeTableElementsSize; i++) {
                Elements dataElements = timeTableElements.get(i).select("td");
                int dataElementsSize = dataElements.size();
                for(int j = 0; j < dataElementsSize; j++){
                    mExaminationTimeTable[i][j] = dataElements.get(j).text();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
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