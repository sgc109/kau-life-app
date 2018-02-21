package com.lifekau.android.lifekau.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.AccumulatedGradeSummaryActivity;
import com.lifekau.android.lifekau.activity.ScholarshipActivity;

public class LmsAndInfoFragment extends PagerFragment implements View.OnClickListener {

    public static LmsAndInfoFragment newInstance() {
        LmsAndInfoFragment fragment = new LmsAndInfoFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lms_and_info, container, false);
        findFragmentContainer(view);
        FrameLayout showScholarshipButton = view.findViewById(R.id.fragment_portal_show_scholarship_button);
        showScholarshipButton.setOnClickListener(this);
        FrameLayout showAccumulatedGradeSummaryButton = view.findViewById(R.id.fragment_portal_show_accumulated_grade_summary_button);
        showAccumulatedGradeSummaryButton.setOnClickListener(this);
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_items_setting, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void findFragmentContainer(View view) {
        mFragmentContainer = view.findViewById(R.id.fragment_seat_container);
    }

    @Override
    public void refresh() {

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.fragment_portal_show_scholarship_button:
                intent = new Intent(view.getContext(), ScholarshipActivity.class);
                startActivity(intent);
                break;
            case R.id.fragment_portal_show_accumulated_grade_summary_button:
                intent = new Intent(view.getContext(), AccumulatedGradeSummaryActivity.class);
                startActivity(intent);
                break;
        }
    }
}