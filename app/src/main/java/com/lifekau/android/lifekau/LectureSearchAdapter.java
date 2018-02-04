package com.lifekau.android.lifekau;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

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
            return ((String)resultValue);
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null) {
                ArrayList<String> suggestions = new ArrayList<String>();
                String keyword = constraint.toString();
                int last = -1;
                for(int i = keyword.length() - 1; i >= 0; i--){
                    if(keyword.charAt(i) != ' ') {
                        last = i;
                        break;
                    }
                }
                if(last == -1) return results;

                keyword = keyword.substring(0, last + 1);
                for (String lecture : mLectures) {
                    boolean ok = true;
                    int pos1 = 0;
                    int pos2 = 0;
                    while(pos1 < keyword.length() && pos2 < lecture.length()){
                        if(keyword.charAt(pos1) == ' ') pos1++;
                        else if(lecture.charAt(pos2) == ' ') pos2++;
                        else if(keyword.charAt(pos1) == lecture.charAt(pos2)) {
                            pos1++;
                            pos2++;
                        }
                        else {
                            ok = false;
                            break;
                        }
                    }
                    if(!ok || pos1 < keyword.length()) continue;
                    suggestions.add(lecture);
                }

                results.values = suggestions;
                results.count = suggestions.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results != null && results.count > 0) {
                // we have filtered results
                addAll((ArrayList<String>) results.values);
            } else {
                // no filter, add entire original list back in
                addAll(mLectures);
            }
            notifyDataSetChanged();
        }
    };

    public LectureSearchAdapter(Context context, int textViewResourceId, List<String> customers) {
        super(context, textViewResourceId, customers);
        // copy all the customers into a master list
        mLectures = new ArrayList<String>(customers.size());
        mLectures.addAll(customers);
        layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }
}
