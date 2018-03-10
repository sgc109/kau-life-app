package com.lifekau.android.lifekau.manager;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.lifekau.android.lifekau.PersistentLoadCookieJar;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.model.AccumulatedGrade;
import com.lifekau.android.lifekau.model.AccumulatedGradeSummary;
import com.lifekau.android.lifekau.model.CurrentGrade;
import com.lifekau.android.lifekau.model.ExaminationTimeTable;
import com.lifekau.android.lifekau.model.Scholarship;
import com.lifekau.android.lifekau.model.TotalAccumulatedGrade;
import com.lifekau.android.lifekau.model.TotalCurrentGrade;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.ConnectionPool;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LMSPortalManager {

    private static final int EXAMINATION_TIME_TABLE_HEADER_NUM = 0;
    private static final int EXAMINATION_TIME_TABLE_DAY_NUM = 0;

    private ArrayList<Scholarship> mScholarshipArray;
    private ArrayList<CurrentGrade> mCurrentGrade;
    private TotalCurrentGrade mTotalCurrentGrade;
    private ArrayList<AccumulatedGrade> mAccumulatedGradeArray;
    private ArrayList<AccumulatedGradeSummary> mAccumulatedGradeSummaryArray;
    private TotalAccumulatedGrade mTotalAccumulatedGrade;
    private ArrayList<ExaminationTimeTable> mExaminationTimeTable;
    private PersistentLoadCookieJar mCookieJar;
    private OkHttpClient mClient;
    private String mSSOToken;
    private String mStudentId;

    private LMSPortalManager() {
        mScholarshipArray = new ArrayList<>();
        mCurrentGrade = new ArrayList<>();
        mTotalCurrentGrade = new TotalCurrentGrade();
        mAccumulatedGradeArray = new ArrayList<>();
        mAccumulatedGradeSummaryArray = new ArrayList<>();
        mTotalAccumulatedGrade = new TotalAccumulatedGrade();
        mExaminationTimeTable = new ArrayList<>();
    }

    private static class LazyHolder {
        public static final LMSPortalManager INSTANCE = new LMSPortalManager();
    }

    public static LMSPortalManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public synchronized OkHttpClient getClient(Context context) {
        if (mClient == null) {
            mCookieJar =
                    new PersistentLoadCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));
            ConnectionPool connectionPool = new ConnectionPool();
            mClient = new OkHttpClient.Builder()
                    .connectionPool(connectionPool)
                    .cookieJar(mCookieJar)
                    .retryOnConnectionFailure(true)
                    .build();
        }
        return mClient;
    }

    public int pullSession(Context context, String id, String password) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_lms_check_page))
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(resources.getInteger(R.integer.server_error));
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
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
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_lms_check_page))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .post(body)
                .build();
        call = client.newCall(request);
        String loginInfomation;
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
            loginInfomation = res.body().string();
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        if (loginInfomation.contains(resources.getString(R.string.portal_login_failed)))
            return resources.getInteger(R.integer.session_error);
        mSSOToken = loginInfomation.split("\'")[3];
        String url = HttpUrl.parse(resources.getString(R.string.portal_portal_check_page)).newBuilder()
                .addQueryParameter("chk1", "1")
                .build().toString();
        request = new Request.Builder()
                .url(url)
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .build();
        call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        url = HttpUrl.parse(resources.getString(R.string.portal_portal_login_page)).newBuilder()
                .addQueryParameter("seq_id", mSSOToken)
                .addQueryParameter("ppage", "")
                .build().toString();
        request = new Request.Builder()
                .url(url)
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_portal_check_page))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .build();
        call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        url = HttpUrl.parse(resources.getString(R.string.lms_sso_page)).newBuilder()
                .addQueryParameter("seq_id", mSSOToken)
                .build().toString();
        request = new Request.Builder()
                .url(url)
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .build();
        call = client.newCall(request);
        String newId, newPassword;
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
            Document doc = Jsoup.parse(res.body().string());
            newId = doc.select("input").get(0).attr("value");
            newPassword = doc.select("input").get(1).attr("value");
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        body = new FormBody.Builder()
                .add("username", newId)
                .add("password", newPassword)
                .build();
        request = new Request.Builder()
                .url(resources.getString(R.string.lms_login_page))
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .post(body)
                .build();
        call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        url = HttpUrl.parse(resources.getString(R.string.lms_login_index_page)).newBuilder()
                .addQueryParameter("testsession", "4837")
                .build().toString();
        request = new Request.Builder()
                .url(url)
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .build();
        call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        return resources.getInteger(R.integer.no_error);
    }

    public int pullStudentId(Context context) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        Request request = new Request.Builder()
                .url(resources.getString(R.string.lms_my_page))
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("Referer", resources.getString(R.string.lms_my_page))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
            Document doc = Jsoup.parse(res.body().string());
            mStudentId = doc.select("#loggedin-user").get(0).getElementsByAttributeValue("class", "dropdown-toggle").text().replaceAll("[^0-9]", "");
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.session_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        return resources.getInteger(R.integer.no_error);
    }

    public int pullScholarship(Context context) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_scholar_page))
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.
                        getString(R.string.header_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
            Document doc = Jsoup.parse(res.body().string());
            Elements elements = doc.getElementsByAttributeValue("class", "table1").select("tr");
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
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.session_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        return resources.getInteger(R.integer.no_error);
    }

    public int pullCurrentGrade(Context context) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_curr_grade_page))
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
            Document doc = Jsoup.parse(res.body().string());
            Elements elements = doc.getElementsByAttributeValue("cellspacing", "1");
            mCurrentGrade.clear();
            if (resources.getString(R.string.portal_curr_grade_no_data).equals(elements.get(0).select("tr").get(1).select("td").text())) {
                return resources.getInteger(R.integer.missing_data_error);
            }
            int elementsSize = elements.size();
            for (int i = 0; i < elementsSize; i++) {
                Elements grades = elements.get(i).select("tr");
                int gradesSize = grades.size();
                for (int j = 1; j < gradesSize; j++) {
                    Elements infomation = grades.get(j).select("td");
                    CurrentGrade grade = new CurrentGrade();
                    grade.courseNumber = infomation.get(0).text();
                    grade.courseTitle = infomation.get(1).text();
                    grade.credits = infomation.get(2).text();
                    grade.evaluation = infomation.get(3).text();
                    grade.grade = infomation.get(4).text();
                    grade.major = infomation.get(5).text();
                    grade.portfolio = infomation.get(6).text();
                    grade.remarks = infomation.get(7).text();
                    grade.retake = infomation.get(8).text();
                    mCurrentGrade.add(grade);
                }
            }
            Elements totalGradeSummary = elements.get(1).select("tr").get(1).select("td");
            mTotalCurrentGrade.registeredCredits = Integer.valueOf(totalGradeSummary.get(0).text());
            mTotalCurrentGrade.acquiredCredits = Integer.valueOf(totalGradeSummary.get(1).text());
            mTotalCurrentGrade.totalGrades = Double.valueOf(totalGradeSummary.get(2).text());
            mTotalCurrentGrade.GPA = Double.valueOf(totalGradeSummary.get(3).text());
            mTotalCurrentGrade.semesterRanking = totalGradeSummary.get(4).text();
            mTotalCurrentGrade.remarks = totalGradeSummary.get(5).text();
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.session_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        return resources.getInteger(R.integer.no_error);
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
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) {
                return resources.getInteger(R.integer.server_error);
            }
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
                mAccumulatedGradeArray.add(accumulatedGrade);
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

    public int pullAccumulatedGradeSummary(Context context) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_accumulated_grade_summary_page))
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
            Document doc = Jsoup.parse(res.body().string());
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
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.session_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        return resources.getInteger(R.integer.no_error);
    }

    public int pullExaminationTimeTable(Context context) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        FormBody body = new FormBody.Builder()
                .build();
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_examination_time_table_page))
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .post(body)
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
            Document doc = Jsoup.parse(res.body().string());
            Elements timeTableElements = doc.getElementsByAttributeValue("class", "table1").get(1).select("tr");
            String year = doc.getElementsByAttributeValue("class", "input").get(0).attr("value");
            String semester = doc.getElementsByAttributeValue("name", "hakgi").get(0).getElementsByAttribute("selected").attr("value");
            String termType = doc.getElementsByAttributeValue("name", "junggi_gb").get(0).getElementsByAttribute("selected").attr("value");
            pullExaminationTimeTable(context, year, semester, termType);
            if (timeTableElements.select("td").get(1).text().equals(resources.getString(R.string.portal_titme_table_no_data)))
                return resources.getInteger(R.integer.missing_data_error);
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.session_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        return resources.getInteger(R.integer.no_error);
    }

    public int pullExaminationTimeTable(Context context, String year, String semesterCode, String examCode) {
        Resources resources = context.getResources();
        OkHttpClient client = getClient(context);
        FormBody body = new FormBody.Builder()
                .add("year", year)
                .add("hakgi", semesterCode)
                .add("junggi_gb", examCode)
                .build();
        Request request = new Request.Builder()
                .url(resources.getString(R.string.portal_examination_time_table_page) + "?" + year + "hakgi=" + examCode)
                .addHeader("Accept", resources.getString(R.string.header_accept))
                .addHeader("Accept-Encoding", resources.getString(R.string.header_accept_encoding_with_br))
                .addHeader("Accept-Language", resources.getString(R.string.header_accpet_language))
                .addHeader("User-Agent", resources.getString(R.string.header_user_agent))
                .addHeader("Referer", resources.getString(R.string.portal_my_menu_b_page))
                .addHeader("keep-alive", resources.getString(R.string.header_connection))
                .post(body)
                .build();
        Call call = client.newCall(request);
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
            Document doc = Jsoup.parse(res.body().string());
            String temp = doc.html().replace("<br>", "%%");
            doc = Jsoup.parse(temp);
            Elements timeTableElements = doc.getElementsByAttributeValue("class", "table1").get(1).select("tr");
            int timeTableElementsSize = timeTableElements.size();
            for (int i = 0; i < timeTableElementsSize; i++) {
                Elements dataElements = timeTableElements.get(i).select("td");
                int dataElementsSize = dataElements.size();
                for (int j = 0; j < dataElementsSize; j++) {
                    String string = dataElements.get(j).text();
                    String[] strings = string.split("%%");
                    ExaminationTimeTable examinationTimeTable = new ExaminationTimeTable();
                    if(strings.length == 3) {
                        examinationTimeTable.subjectTitle = strings[0];
                        examinationTimeTable.professorName = strings[1].split(":")[1];
                        examinationTimeTable.place = strings[2].split(":")[1];
                    }
                    if(strings.length == 2){
                        examinationTimeTable.subjectTitle = strings[0];
                        examinationTimeTable.professorName = strings[1];
                    }
                    mExaminationTimeTable.add(examinationTimeTable);
                }
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.session_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        return resources.getInteger(R.integer.no_error);
    }

    public int getExaminationTimeTableSize() {
        return mExaminationTimeTable.size();
    }

    public ExaminationTimeTable getExaminationTimeTable(int index){
        return index < getExaminationTimeTableSize() ? mExaminationTimeTable.get(index) : null;
    }

    public void clearExaminationTimeTable(){
        mExaminationTimeTable.clear();
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

    public void clearScholarship() {
        mScholarshipArray.clear();
    }

    public int getCurrentGradeSize() {
        return mCurrentGrade.size();
    }

    public CurrentGrade getCurrentGrade(int index) {
        return index < getCurrentGradeSize() ? mCurrentGrade.get(index) : null;
    }

    public void clearTotalCurrentGrade() {
        mTotalCurrentGrade = new TotalCurrentGrade();
    }

    public TotalCurrentGrade getTotalCurrentGrade() {
        return mTotalCurrentGrade;
    }

    public void clearCurrentGrade() {
        mCurrentGrade.clear();
    }

    public int getAccumulatedGradeSize() {
        return mAccumulatedGradeArray.size();
    }

    public AccumulatedGrade getAccumulatedGrade(int index) {
        return index < getAccumulatedGradeSize() ? mAccumulatedGradeArray.get(index) : null;
    }

    public void clearAccumulatedGrade() {
        mAccumulatedGradeArray.clear();
    }

    public int getAccumulatedGradeSummarySize() {
        return mAccumulatedGradeSummaryArray.size();
    }

    public AccumulatedGradeSummary getAccumulatedGradeSummary(int index) {
        return index < getAccumulatedGradeSummarySize() ? mAccumulatedGradeSummaryArray.get(index) : null;
    }

    public void clearAccumulatedGradeSummary() {
        mAccumulatedGradeSummaryArray.clear();
    }

    public List<Cookie> getCookie(Context context) {
        if (mCookieJar == null) return null;
        else return mCookieJar.LoadCookies();
    }
}