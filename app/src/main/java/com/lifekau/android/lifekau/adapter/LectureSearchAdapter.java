package com.lifekau.android.lifekau.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.lifekau.android.lifekau.korean.KoreanChar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sgc109 on 2018-02-01.
 */

public class LectureSearchAdapter extends ArrayAdapter<String> {
    private LayoutInflater layoutInflater;
    List<String> mLectures;
    private Filter mFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            return ((String) resultValue);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint == null) return results;
            String keyword = constraint.toString();
            String tmpKeyword = "";
            for (int i = 0; i < keyword.length(); i++) {
                char c = keyword.charAt(i);
                if ('A' <= c && c <= 'Z') tmpKeyword += (char) (c - 'A' + 'a');
                else if (c != ' ') tmpKeyword += c;
            }
            keyword = tmpKeyword;

            ArrayList<String> suggestions = new ArrayList<String>();
            ArrayList<String> suggestions2 = new ArrayList<String>();
            for (String lecture : mLectures) {
                String transLecture = "";
                for (int i = 0; i < lecture.length(); i++) {
                    char c = lecture.charAt(i);
                    if ('A' <= c && c <= 'Z') transLecture += (char) (c - 'A' + 'a');
                    else if (c != ' ') transLecture += c;
                }

                if (keyword.length() > transLecture.length()) continue;

                boolean ok = true;
                for (int i = 0; i < keyword.length(); i++) {
                    if (keyword.charAt(i) == transLecture.charAt(i)) {
                        continue;
                    }
                    if (KoreanChar.isCompatChoseong(keyword.charAt(i))
                            && KoreanChar.isSyllable(transLecture.charAt(i))
                            && keyword.charAt(i) == KoreanChar.getCompatChoseong(transLecture.charAt(i))) {
                        continue;
                    }
                    ok = false;
                    break;
                }

                if (ok) {
                    suggestions.add(lecture);
                } else if (getLCS(transLecture, keyword) == keyword.length()) {
                    suggestions2.add(lecture);
                }
            }
            suggestions.addAll(suggestions2);
            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        private int getLCS(String S, String s) {
            int n = S.length();
            int m = s.length();
            int[][] dp = new int[n + 1][m + 1];
            for (int i = 1; i <= n; i++) {
                for (int j = 1; j <= m; j++) {
                    if (S.charAt(i - 1) == s.charAt(j - 1)) {
                        dp[i][j] = dp[i - 1][j - 1] + 1;
                    } else if (KoreanChar.isCompatChoseong(s.charAt(j - 1))
                            && KoreanChar.isSyllable(S.charAt(i - 1))
                            && s.charAt(j - 1) == KoreanChar.getCompatChoseong(S.charAt(i - 1))) {
                        dp[i][j] = dp[i - 1][j - 1] + 1;
                    } else {
                        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                    }
                }
            }
            return dp[n][m];
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results != null && results.count > 0) {
                addAll((ArrayList<String>) results.values);
            } else {
                addAll(mLectures);
            }
            notifyDataSetChanged();
        }
    };

    public LectureSearchAdapter(Context context, int textViewResourceId, List<String> lectures) {
        super(context, textViewResourceId, lectures);
        // copy all the customers into a master list
        mLectures = new ArrayList<String>();
        mLectures.addAll(lectures);
        layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public int getCount() {
        return Math.min(super.getCount(), 100);
    }
}
