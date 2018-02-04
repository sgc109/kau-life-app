package com.lifekau.android.lifekau;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class CommunityFragment extends PagerFragment {

    public static CommunityFragment newInstance(){
        CommunityFragment fragment = new CommunityFragment();
        return fragment;
    }

    @Override
    public void findFragmentContainer(ViewGroup viewGroup) {
        mFragmentContainer = viewGroup.findViewById(R.id.fragment_community_container);
    }

    @Override
    public void refresh() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_community, container, false);
    }

}
