package com.lifekau.android.lifekau.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.model.Notice;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.TreeSet;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoticeManager {


    private final static int TOTAL_NOTICE_NUM = 5;
    private static final String SAVE_STUDENT_REST_FILE_NAME = "shared_preference_student_restaurant_file_name";
    private static final String SAVE_STUDENT_REST_DETAIL_NUM = "shared_preference_student_restaurant_detail_num";
    private static final String SAVE_DORMITORY_REST_FILE_NAME = "shared_preference_dormitory_restaurant_file_name";
    private static final String SAVE_DORMITORY_REST_DETAIL_NUM = "shared_preference_dormitory_restaurant_detail_num";
    private final static String[] COMMUNITY_KEY = {"B0146", "B0147", "B0230", "B0259", "B0148"};
    private final static String[] URL_TYPE = {"/page/kauspace/", "/page/kau_media/"};
    private final static String[] NOTICE_LIST = {"general_list", "academicinfo_list", "scholarship_list", "career_list", "event_list"};

    private boolean[] mAllPageFetched;
    private int[] mLatestPageNum;
    private int[] mLoadedListNum;
    private int[] mLatestDetailPageNum;
    private ArrayList<Notice>[] mImportantNoticeList;
    private Hashtable<Integer, Notice>[] mNoticeListMap;
    private Set<String> mStudentRestFileName;
    private Set<String> mDormitoryRestFileName;

    private NoticeManager() {
        mAllPageFetched = new boolean[TOTAL_NOTICE_NUM];
        mLatestPageNum = new int[TOTAL_NOTICE_NUM];
        mLoadedListNum = new int[TOTAL_NOTICE_NUM];
        mLatestDetailPageNum = new int[TOTAL_NOTICE_NUM];
        mNoticeListMap = new Hashtable[TOTAL_NOTICE_NUM];
        mImportantNoticeList = new ArrayList[TOTAL_NOTICE_NUM];
        mStudentRestFileName = new TreeSet<>();
        mDormitoryRestFileName = new TreeSet<>();
        for (int i = 0; i < TOTAL_NOTICE_NUM; i++) mNoticeListMap[i] = new Hashtable<>();
        for (int i = 0; i < TOTAL_NOTICE_NUM; i++) mImportantNoticeList[i] = new ArrayList<>();
    }

    private static class LazyHolder {
        private static NoticeManager INSTANCE = new NoticeManager();
    }

    public static NoticeManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public int pullNoticeList(Context context, int noticeType) {
        if (pullNoticeList(context, noticeType, mLoadedListNum[noticeType] + 1) == 0) {
            mLoadedListNum[noticeType]++;
            return 0;
        }
        return -1;
    }

    public int pullNoticeList(Context context, int noticeType, int listNum) {
        Resources resources = context.getResources();
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
            if (res.code() <= 199 || res.code() >= 301)
                return resources.getInteger(R.integer.server_error);
            Document doc = Jsoup.parse(res.body().string());
            Elements postElements = doc.getElementsByAttributeValue("id", "board_form").select("tbody").select("tr");
            int postElementsSize = postElements.size();
            if (postElementsSize == 1 && postElements.get(0).select("td").get(0).text().equals("등록된 글이 없습니다.")) {
                mAllPageFetched[noticeType] = true;
                return resources.getInteger(R.integer.missing_data_error);
            }
            for (int i = 0; i < postElementsSize; i++) {
                Elements pageDataElements = postElements.get(i).select("td");
                Notice insertedNotice = new Notice();
                insertedNotice.postNum = Integer.valueOf(pageDataElements.get(0).text().equals("") ? "0" : pageDataElements.get(0).text());
                insertedNotice.postDetailNum = Integer.valueOf(pageDataElements.get(1).select("a").attr("href").replaceAll("[^0-9]", ""));
                insertedNotice.postTitle = pageDataElements.get(1).text();
                insertedNotice.writer = pageDataElements.get(2).text();
                insertedNotice.RegistrationDate = pageDataElements.get(3).text();
                mLatestDetailPageNum[noticeType] = Math.max(mLatestDetailPageNum[noticeType], insertedNotice.postDetailNum);
                mLatestPageNum[noticeType] = Math.max(mLatestPageNum[noticeType], insertedNotice.postNum);
                if (insertedNotice.postNum == 0)
                    mImportantNoticeList[noticeType].add(insertedNotice);
                else currNoticeList.put(insertedNotice.postNum, insertedNotice);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        return resources.getInteger(R.integer.no_error);
    }

    public void clear(int noticeType) {
        mLatestDetailPageNum[noticeType] = 0;
        mLatestPageNum[noticeType] = 0;
        mLoadedListNum[noticeType] = 0;
        mImportantNoticeList[noticeType].clear();
        mNoticeListMap[noticeType].clear();
        mAllPageFetched[noticeType] = false;
    }

    public int getLatestDetailPageNum(int noticeType) {
        return mLatestDetailPageNum[noticeType];
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

    public int pullStudentRestFoodMenu(Context context) {
        Resources resources = context.getResources();
        SharedPreferences sharedPref = context.getSharedPreferences(resources.getString(R.string.shared_preference_app), Context.MODE_PRIVATE);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(resources.getString(R.string.food_menu_student_rest_page))
                .build();
        Call call = client.newCall(request);
        int currIndex = -1;
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) {
                return resources.getInteger(R.integer.server_error);
            }
            Document doc = Jsoup.parse(res.body().string());
            Elements postElements = doc.getElementsByAttributeValue("id", "content_list").get(0).getElementsByAttributeValue("class", "tb01_3");
            for (Element e : postElements) {
                if (e.text().contains("학생식당")) {
                    String string = e.select("a").get(0).attr("href");
                    currIndex = Integer.valueOf(string.split("'")[1]);
                    break;
                }
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.missing_data_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        request = new Request.Builder()
                .url(String.format(resources.getString(R.string.food_menu_student_rest_view_page), currIndex))
                .build();
        call = client.newCall(request);
        ArrayList<String> foodMenuUrls = new ArrayList<>();
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) {
                return resources.getInteger(R.integer.server_error);
            }
            Document doc = Jsoup.parse(res.body().string());
            Elements fileElements = doc.getElementsByAttributeValue("class", "tb01_3").get(1).select("a");
            if (fileElements.size() > 0) {
                for (Element e : fileElements) {
                    foodMenuUrls.add(resources.getString(R.string.portal_page) + e.attr("href"));
                }
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.missing_data_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        int prevIndex = sharedPref.getInt(SAVE_STUDENT_REST_DETAIL_NUM, -1);
        mStudentRestFileName = sharedPref.getStringSet(SAVE_STUDENT_REST_FILE_NAME, new HashSet<String>());
        if (currIndex == prevIndex && foodMenuUrls.size() == mStudentRestFileName.size()) return resources.getInteger(R.integer.no_error);
        mStudentRestFileName.clear();
        int count = 0;
        for (String url : foodMenuUrls) {
            request = new Request.Builder()
                    .url(url)
                    .build();
            call = client.newCall(request);
            try (Response res = call.execute()) {
                String fileName = "s" + count + ".jpg";
                count++;
                mStudentRestFileName.add(fileName);
                byte[] imageBytes = res.body().bytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 5, baos);
                File file = new File(context.getFilesDir(), fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baos.toByteArray());
                fos.close();
            } catch (IOException e) {
                return resources.getInteger(R.integer.file_write_error);
            } catch (Exception e) {
                return resources.getInteger(R.integer.network_error);
            }
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(SAVE_STUDENT_REST_DETAIL_NUM, currIndex);
        editor.putStringSet(SAVE_STUDENT_REST_FILE_NAME, mStudentRestFileName);
        editor.apply();
        return resources.getInteger(R.integer.no_error);
    }

    public int pullDormitoryRestFoodMenu(Context context) {
        Resources resources = context.getResources();
        SharedPreferences sharedPref = context.getSharedPreferences(resources.getString(R.string.shared_preference_app), Context.MODE_PRIVATE);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(resources.getString(R.string.food_menu_dormitory_rest_page))
                .build();
        Call call = client.newCall(request);
        int currIndex = -1;
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) {
                return resources.getInteger(R.integer.server_error);
            }
            Document doc = Jsoup.parse(res.body().string());
            Elements postElements = doc.getElementsByAttributeValue("headers", "board_title");
            for (Element e : postElements) {
                if (e.text().contains("월")) {
                    String string = e.select("a").get(0).attr("href");
                    currIndex = Integer.valueOf(string.split("\\(")[1].split(",")[0]);
                    break;
                }
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.missing_data_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        RequestBody body = new FormBody.Builder()
                .add("communityKey", "B0126")
                .add("pageNum", "1")
                .add("pageSize", "10")
                .add("act", "VIEW")
                .add("boardId", String.valueOf(currIndex))
                .add("branch_session", "")
                .add("only_reply", "")
                .add("mbo_mother_page", "/page/web/life/community/meal_li.jsp")
                .add("board_table_name", "WCM_BOARD_" + "B0126")
                .add("sort_type", "DESC")
                .add("sort_column", "")
                .add("memoTable", "WCM_BOARD_MEMO" + "B0126")
                .add("login_id", "")
                .add("searchType", "TITLE")
                .add("searchWord", "")
                .add("chg_page_size", "10")
                .build();
        request = new Request.Builder()
                .url(resources.getString(R.string.food_menu_dormitory_rest_page))
                .post(body)
                .build();
        call = client.newCall(request);
        ArrayList<String> foodMenuUrls = new ArrayList<>();
        try (Response res = call.execute()) {
            if (res.code() <= 199 || res.code() >= 301) {
                return resources.getInteger(R.integer.server_error);
            }
            Document doc = Jsoup.parse(res.body().string());
            Elements fileElements = doc.getElementsByAttributeValueStarting("href", "/page/cms/board/Download.jsp");
            if (fileElements.size() > 0) {
                for (Element e : fileElements) {
                    foodMenuUrls.add(resources.getString(R.string.portal_page) + e.attr("href"));
                }
            }
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.missing_data_error);
        } catch (Exception e) {
            e.printStackTrace();
            return resources.getInteger(R.integer.network_error);
        }
        int prevIndex = sharedPref.getInt(SAVE_DORMITORY_REST_DETAIL_NUM, -1);
        mDormitoryRestFileName = sharedPref.getStringSet(SAVE_DORMITORY_REST_FILE_NAME, new HashSet<String>());
        if (currIndex == prevIndex && foodMenuUrls.size() == mDormitoryRestFileName.size()) return resources.getInteger(R.integer.no_error);
        mDormitoryRestFileName.clear();
        int count = 0;
        for (String url : foodMenuUrls) {
            request = new Request.Builder()
                    .url(url)
                    .build();
            call = client.newCall(request);
            try (Response res = call.execute()) {
                String fileName = "d" + count + ".jpg";
                count++;
                mDormitoryRestFileName.add(fileName);
                byte[] imageBytes = res.body().bytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 5, imageBytes.length);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 0, baos);
                File file = new File(context.getFilesDir(), fileName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(baos.toByteArray());
                fos.close();
            } catch (IOException e) {
                return resources.getInteger(R.integer.file_write_error);
            } catch (Exception e) {
                return resources.getInteger(R.integer.network_error);
            }
        }
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet(SAVE_DORMITORY_REST_FILE_NAME, mDormitoryRestFileName);
        editor.putInt(SAVE_DORMITORY_REST_DETAIL_NUM, currIndex);
        editor.apply();
        return resources.getInteger(R.integer.no_error);
    }
    
    public Set<String> getStudentRestFoodMenuFileName(){
        return mStudentRestFileName;
    }

    public Set<String> getDormitoryRestFileName(){
        return mDormitoryRestFileName;
    }
}
