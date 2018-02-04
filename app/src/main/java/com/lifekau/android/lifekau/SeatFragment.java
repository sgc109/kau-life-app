package com.lifekau.android.lifekau;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SeatFragment extends PagerFragment {

    public static SeatFragment newInstance(){
        SeatFragment fragment = new SeatFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_seat, container, false);
    }

    @Override
    public void findFragmentContainer(ViewGroup viewGroup) {
        mFragmentContainer = viewGroup.findViewById(R.id.fragment_seat_container);
    }

    @Override
    public void refresh() {

    }

}
