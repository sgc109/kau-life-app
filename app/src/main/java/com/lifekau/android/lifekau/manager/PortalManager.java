package com.lifekau.android.lifekau.manager;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.model.AccumulatedGrade;
import com.lifekau.android.lifekau.model.AccumulatedGradeSummary;
import com.lifekau.android.lifekau.model.CurrGrade;
import com.lifekau.android.lifekau.model.Scholarship;
import com.lifekau.android.lifekau.model.TotalAccumulatedGrade;
import com.lifekau.android.lifekau.model.TotalCurrGrade;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Map;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private OkHttpClient mClient;
    private String mSSOToken;
    private String mStudentId;

    private PortalManager() {
        mScholarshipArray = new ArrayList<Scholarship>();
        mCurrGrade = new ArrayList<CurrGrade>();
        mTotalCurrGrade = new TotalCurrGrade();
        mAccumulatedGradeArray = new ArrayList<AccumulatedGrade>();
        mAccumulatedGradeSummaryArray = new ArrayList<AccumulatedGradeSummary>();
        mTotalAccumulatedGrade = new TotalAccumulatedGrade();
        mExaminationTimeTable = new String[EXAMIANATION_TIME_TABLE_ROW][EXAMIANATION_TIME_TABLE_COL];
    }

    private static class LazyHolder {
        public static final PortalManager INSTANCE = new PortalManager();
    }

    public static PortalManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public synchronized OkHttpClient getClient(Context context) {
        if (mClient == null) {
            ClearableCookieJar cookieJar =
                    new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
            mClient = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();
        }
        return mClient;
    }

    public int pullSession(Context context, String id, String password) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_lms_check_page))
                .addHeader("Accept", resources.getString(R.string.portal_header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.portal_header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.portal_header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.portal_user_agent))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        RequestBody body = new FormBody.Builder()
                .add("target_page", "act_Lms_Check.jsp@chk1-1")
                .add("refer_page", "")
                .add("SessionID", "")
                .add("SessionRequestData", "")
                .add("AlgID", "SEED")
                .add("ppage", "")
                .add("p_id", id)
                .add("p_pwd", password)
                .build();
        request = new Request.Builder()
                .url(resources.getString(R.string.portal_act_login_page))
                .addHeader("Accept", resources.getString(R.string.portal_header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.portal_header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.portal_header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.portal_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_lms_check_page))
                .post(body)
                .build();
        call = client.newCall(request);
        String loginInfomation;
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
            loginInfomation = res.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        if (loginInfomation.contains(resources.getString(R.string.portal_login_failed))) return -1;
        mSSOToken = loginInfomation.split("\'")[3];
        String url = HttpUrl.parse(resources.getString(R.string.portal_portal_check_page)).newBuilder()
                .addQueryParameter("chk1", "1")
                .build().toString();
        request = new Request.Builder()
                .url(url)
                .addHeader("Accept", resources.getString(R.string.portal_header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.portal_header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.portal_header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.portal_user_agent))
                .build();
        call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        url = HttpUrl.parse(resources.getString(R.string.portal_portal_login_page)).newBuilder()
                .addQueryParameter("seq_id", mSSOToken)
                .addQueryParameter("ppage", "")
                .build().toString();
        request = new Request.Builder()
                .url(url)
                .addHeader("Accept", resources.getString(R.string.portal_header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.portal_header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.portal_header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.portal_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_portal_check_page))
                .build();
        call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int checkSessionVaild() {
        return 0;
    }

    public int pullScholarship(Context context) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_scholar_page))
                .addHeader("Accept", resources.getString(R.string.portal_header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.portal_header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.portal_header_accpet_language))
                .addHeader("User-Agent", resources.
                        getString(R.string.portal_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
            Document doc = Jsoup.parse(res.body().string());
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

    public int pullCurrGrade(Context context) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_curr_grade_page))
                .addHeader("Accept", resources.getString(R.string.portal_header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.portal_header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.portal_header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.portal_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
            Document doc = Jsoup.parse(res.body().string());
            Elements elements = doc.getElementsByAttributeValue("cellspacing", "1");
            mCurrGrade.clear();
            if (resources.getString(R.string.portal_curr_grade_no_data).equals(elements.get(0).select("tr").get(1).select("td").text()))
                return -1;
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

    public int pullAccumulatedGrade(Context context, int year, int semesterCode) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        String url = HttpUrl.parse(resources.getString(R.string.portal_accumulated_grade_page)).newBuilder()
                .addQueryParameter("guYear", String.valueOf(year))
                .addQueryParameter("guHakgi", String.valueOf(semesterCode))
                .build().toString();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", resources.getString(R.string.portal_header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.portal_header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.portal_header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.portal_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
            Document doc = Jsoup.parse(res.body().string());
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

    public int pullAccumulatedGradeSummary(Context context) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_accumulated_grade_summary_page))
                .addHeader("Accept", resources.getString(R.string.portal_header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.portal_header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.portal_header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.portal_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
            Document doc = Jsoup.parse(res.body().string());
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

    public int pullExaminationTimeTable(Context context, int year, int semesterCode, int examCode) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        FormBody body = new FormBody.Builder()
                .add("year", String.valueOf(year))
                .add("hakgi", String.valueOf(semesterCode))
                .add("junggi_gb", String.valueOf(examCode))
                .build();
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_examination_time_table_page) + "?" + year + "hakgi=" + examCode)
                .addHeader("Accept", resources.getString(R.string.portal_header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.portal_header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.portal_header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.portal_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .post(body)
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
            Document doc = Jsoup.parse(res.body().string());
            Elements timeTableElements = doc.getElementsByAttributeValue("class", "table1").get(1).select("tr");
            if (timeTableElements.select("td").get(1).text().equals(resources.getString(R.string.portal_titme_table_no_data)))
                return -1;
            int timeTableElementsSize = timeTableElements.size();
            for (int i = 0; i < timeTableElementsSize; i++) {
                Elements dataElements = timeTableElements.get(i).select("td");
                int dataElementsSize = dataElements.size();
                for (int j = 0; j < dataElementsSize; j++) {
                    mExaminationTimeTable[i][j] = dataElements.get(j).text();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public int pullStudentId(Context context) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        String url = HttpUrl.parse(resources.getString(R.string.portal_get_student_id_page)).newBuilder()
                .addQueryParameter("sso_token", mSSOToken)
                .addQueryParameter("sso_link", "portal")
                .build().toString();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Accept", resources.getString(R.string.portal_header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.portal_header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.portal_header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.portal_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) return -1;
            Document doc = Jsoup.parse(res.body().string());
            mStudentId = doc.getElementsByAttributeValue("name", "USERID").get(0).attr("value");
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public String getStudentId() {
        return mStudentId;
    }


    public int getScholarshipSize() {
        return mScholarshipArray.size();
    }

    public Scholarship getScholarship(int index) {
        return index < getScholarshipSize() ? mScholarshipArray.get(index) : null;
    }
}