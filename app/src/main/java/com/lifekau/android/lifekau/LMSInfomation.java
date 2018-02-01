package com.lifekau.android.lifekau;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

//TODO: 기존에 저장된 내부 LMS 데이터를 불러온다.
//TODO: 파싱하는 도중 에러가 발생할 때 예외 처리를 추가한다.
//TODO: 하드코딩한 string 데이터를 R.string에 옮겨야 한다.
//TODO: 코드 리팩토링이 필요하다.

public class LMSInfomation {

    private final int MAX_SUBJECT_NUM = 21;
    Map<String, String> cookies;

    LMSInfomation(){
    }
    public Integer connectSession(String id, String password){
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
                    .cookies(cookies = res.cookies())
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
            res = Jsoup.connect("http://lms.kau.ac.kr/local/ssotm/sso_chk.php")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(cookies = res.cookies())
                    .data("seq_id", strings[3])
                    .execute();
            Document doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
            res = Jsoup.connect("http://lms.kau.ac.kr/login/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(res.cookies())
                    .data("username", doc.select("input").get(0).attr("value"))
                    .data("password", doc.select("input").get(1).attr("value"))
                    .method(Connection.Method.POST)
                    .execute();
            res = Jsoup.connect("http://lms.kau.ac.kr/login/index.php")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(cookies = res.cookies())
                    .data("testsession", "4837")
                    .execute();
            String referrer = "http://lms.kau.ac.kr/local/ssotm/sso_chk.php" + "?" + "seq_id" + strings[3];
            res = Jsoup.connect("http://lms.kau.ac.kr/my/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                    .cookies(cookies)
                    .referrer(referrer)
                    .execute();
            doc = Jsoup.parse(new String(res.bodyAsBytes(), getMatchingCharSet(res.charset())));
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    public Boolean isSessinVaild(){
        return true;
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