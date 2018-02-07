package com.lifekau.android.lifekau;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PortalInfomation {

    private ArrayList<Scholarship> mScholarshipArray;
    private ArrayList<CurrGrade> mCurrGrade;
    private TotalCurrGrade mTotalCurrGrade;
    private ArrayList<AccumulatedGrade> mAccumulatedGradeArray;
    private ArrayList<AccumulatedGradeSummary> mAccumulatedGradeSummaryArray;
    private TotalAccumulatedGrade mTotalAccumulatedGrade;
    private Map<String, String> mCookies;

    public PortalInfomation(String id, String password) {
        mScholarshipArray = new ArrayList<Scholarship>();
        mCurrGrade = new ArrayList<CurrGrade>();
        mTotalCurrGrade = new TotalCurrGrade();
        mAccumulatedGradeArray = new ArrayList<AccumulatedGrade>();
        mAccumulatedGradeSummaryArray = new ArrayList<AccumulatedGradeSummary>();
        mTotalAccumulatedGrade = new TotalAccumulatedGrade();
        mCookies = getSession(id, password);
        getAccumulatedGradeSummary();
    }

    public Map<String, String> getSession(String id, String password) {
        Map<String, String> cookies = null;
        try {
            Connection.Response res = Jsoup.connect("https://www.kau.ac.kr/page/login.jsp?target_page=act_Lms_Check.jsp@chk1-1")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .execute();
            res = Jsoup.connect("https://www.kau.ac.kr/page/act_login.jsp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(res.cookies())
                    .referrer("https://www.kau.ac.kr/page/login.jsp?target_page=act_Lms_Check.jsp@chk1-1")
                    .data("target_page", "act_Lms_Check.jsp@chk1-1")
                    .data("refer_page", "")
                    .data("SessionID", "")
                    .data("SessionRequestData", "")
                    .data("AlgID", "SEED")
                    .data("ppage", "")
                    .data("p_id", id)
                    .data("p_pwd", password)
                    .method(Connection.Method.POST)
                    .execute();
            String string = new String(res.bodyAsBytes(), getMatchingCharSet(res.charset()));
            String[] strings = string.split("\'");
            res = Jsoup.connect("https://www.kau.ac.kr/page/act_Portal_Check.jsp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(res.cookies())
                    .data("chk1", "1")
                    .execute();
            res = Jsoup.connect("https://portal.kau.ac.kr/portal/PortalLoginSso")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .referrer("https://www.kau.ac.kr/page/act_Portal_Check.jsp?chk1=1")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(res.cookies())
                    .data("seq_id", strings[3])
                    .data("ppage", "")
                    .validateTLSCertificates(false)
                    .execute();
            cookies = res.cookies();
            res = Jsoup.connect("https://portal.kau.ac.kr/portal/MyPortal_No.jsp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .referrer("https://portal.kau.ac.kr/portal/PortalLoginSso?seq_id=" + strings[3] + "&ppage=")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(cookies)
                    .validateTLSCertificates(false)
                    .execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cookies;
    }

    public void getScholarshipInfomation() {
        try {
            Connection.Response res = Jsoup.connect("https://portal.kau.ac.kr/sugang/PersScholarTakeList.jsp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .referrer("https://portal.kau.ac.kr/admt/MyMenuB.jsp")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
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
                insert.amount = Integer.valueOf(String.join("", infomation.get(3).text().split(",")));
                mScholarshipArray.add(insert);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getCurrGradeInfomation() {
        try {
            Connection.Response res = Jsoup.connect("https://portal.kau.ac.kr/sugang/GradHakList.jsp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .referrer("https://portal.kau.ac.kr/admt/MyMenuB.jsp")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(mCookies)
                    .validateTLSCertificates(false)
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            Elements elements = doc.getElementsByAttributeValue("cellspacing", "1");
            mCurrGrade.clear();
            if ("데이터 없음".equals(elements.get(0).select("tr").get(1).select("td").text())) return;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAccumulatedGradeSummary() {
        try {
            Connection.Response res = Jsoup.connect("https://portal.kau.ac.kr/sugang/GradTermList.jsp")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .referrer("https://portal.kau.ac.kr/admt/MyMenuB.jsp")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
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
        }
    }

    public void getAccumulatedGrade(int year, int semesterCode) {
        try {
            Connection.Response res = Jsoup.connect("https://portal.kau.ac.kr/sugang/GradTotList.jsp" + "?guYear=" + year + "&guHakgi=" + semesterCode)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate, br")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .referrer("https://portal.kau.ac.kr/admt/MyMenuB.jsp")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
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