package com.lifekau.android.lifekau.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifekau.android.lifekau.R;

public class LmsAndInfoFragment extends PagerFragment {

    public static LmsAndInfoFragment newInstance(){
        LmsAndInfoFragment fragment = new LmsAndInfoFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lms_and_info, container, false);
        findFragmentContainer(view);
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
        mFragmentContainer = view.findViewById(R.id.fragment_lms_and_info_container);
    }

    @Override
    public void refresh() {

    }
}
