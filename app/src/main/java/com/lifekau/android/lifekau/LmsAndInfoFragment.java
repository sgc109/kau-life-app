package com.lifekau.android.lifekau;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class LmsAndInfoFragment extends PagerFragment {

    public static LmsAndInfoFragment newInstance(){
        LmsAndInfoFragment fragment = new LmsAndInfoFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_lms_and_info, container, false);
    }

    @Override
    public void findFragmentContainer(ViewGroup container) {
        mFragmentContainer = (FrameLayout)container.findViewById(R.id.fragment_lms_and_info_container);
    }

    @Override
    public void refresh() {

    }
}
