package com.lifekau.android.lifekau.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifekau.android.lifekau.R;

public class SeatFragment extends PagerFragment {

    public static SeatFragment newInstance(){
        SeatFragment fragment = new SeatFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seat, container, false);
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
        mFragmentContainer = view.findViewById(R.id.fragment_seat_container);
    }

    @Override
    public void refresh() {

    }

}