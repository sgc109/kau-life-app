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
import com.lifekau.android.lifekau.activity.NoticeListActivity;

public class NoticeFragment extends PagerFragment implements View.OnClickListener{

    private static String NOTICE_TYPE = "notice_type";

    public static NoticeFragment newInstance(){
        NoticeFragment fragment = new NoticeFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notice, container, false);
        findFragmentContainer(view);
        FrameLayout showGeneralListButton = view.findViewById(R.id.fragment_notice_show_general_list_button);
        showGeneralListButton.setOnClickListener(this);
        FrameLayout showAcademicListButton = view.findViewById(R.id.fragment_notice_show_academic_list_button);
        showAcademicListButton.setOnClickListener(this);
        FrameLayout showScholarshipListButton = view.findViewById(R.id.fragment_notice_show_scholarship_list_button);
        showScholarshipListButton.setOnClickListener(this);
        FrameLayout showCareerListButton = view.findViewById(R.id.fragment_notice_show_career_list_button);
        showCareerListButton.setOnClickListener(this);
        FrameLayout showEventListButton = view.findViewById(R.id.fragment_notice_show_event_list_button);
        showEventListButton.setOnClickListener(this);
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
        switch (view.getId()){
            case R.id.fragment_notice_show_general_list_button:
                intent = new Intent(view.getContext(), NoticeListActivity.class);
                intent.putExtra(NOTICE_TYPE, 0);
                startActivity(intent);
                break;
            case R.id.fragment_notice_show_academic_list_button:
                intent = new Intent(view.getContext(), NoticeListActivity.class);
                intent.putExtra(NOTICE_TYPE, 1);
                startActivity(intent);
                break;
            case R.id.fragment_notice_show_scholarship_list_button:
                intent = new Intent(view.getContext(), NoticeListActivity.class);
                intent.putExtra(NOTICE_TYPE, 2);
                startActivity(intent);
                break;
            case R.id.fragment_notice_show_career_list_button:
                intent = new Intent(view.getContext(), NoticeListActivity.class);
                intent.putExtra(NOTICE_TYPE, 3);
                startActivity(intent);
                break;
            case R.id.fragment_notice_show_event_list_button:
                intent = new Intent(view.getContext(), NoticeListActivity.class);
                intent.putExtra(NOTICE_TYPE, 4);
                startActivity(intent);
                break;
        }
    }
}