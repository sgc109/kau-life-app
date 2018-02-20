package com.lifekau.android.lifekau.korean;
/**
 * {@link KoreanTextMatcher} 매칭 결과를 담고 있는 클래스
 *
 * @author 방준영 &lt;junyoung@mogua.com&gt;
 */
public final class KoreanTextMatch {

    /**
     * 검색이 실패했을 때 결과로 리턴되는 인스턴스.
     * <p>
     * 이 인스턴스의 success()는 항상 <code>false</code>다. index(),
     * length(), value() 등 다른 프로퍼티들의 값은 미정이다.
     */
    public static final KoreanTextMatch EMPTY = new KoreanTextMatch();

    private final KoreanTextMatcher _matcher;
    private final String _text;
    private final String _value;
    private final int _index;
    private final boolean _success;

    private KoreanTextMatch() {
        _matcher = null;
        _text = null;
        _value = "";
        _index = 0;
        _success = false;
    }

    KoreanTextMatch(KoreanTextMatcher matcher, String text, int startIndex, int length) {
        _matcher = matcher;
        _text = text;
        _value = text.substring(startIndex, startIndex + length);
        _index = startIndex;
        _success = true;
    }

    /**
     * 매치가 성공했는지 여부를 조사한다.
     *
     * @return 성공했으면 <code>true</code>, 아니면 <code>false</code>.
     */
    public boolean success() {
        return _success;
    }

    /**
     * 매치의 시작 위치를 구한다.
     *
     * @return 검색 대상 문자열 내 패턴의 시작 위치
     */
    public int index() {
        return _index;
    }

    /**
     * 매치의 길이를 구한다.
     *
     * @return 검색 대상 문자열 내 매치의 길이
     */
    public int length() {
        return _value.length();
    }

    /**
     * 매치 문자열을 구한다.
     *
     * @return 검색 대상 문자열 내 실제 매치
     */
    public String value() {
        return _value;
    }

    /**
     * 마지막 매치가 끝나는 위치의 뒷문자부터 시작해서 다음 매치를 찾는다.
     *
     * @return 검색 결과를 담은 {@link KoreanTextMatch} 인스턴스. success()
     *         가 <code>true</code>일 때만 유효하며, 검색이 실패하면
     *         {@link KoreanTextMatch#EMPTY}가 리턴된다.
     */
    public KoreanTextMatch nextMatch() {
        if (_text == null)
            return EMPTY;

        KoreanTextMatch match = _matcher.match(_text, _index + _value.length());
        return match;
    }
}