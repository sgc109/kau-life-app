package com.lifekau.android.lifekau.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.LibraryListActivity;

public class SeatFragment extends PagerFragment implements View.OnClickListener{

    public static SeatFragment newInstance(){
        SeatFragment fragment = new SeatFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seat, container, false);
        findFragmentContainer(view);
        FrameLayout showReadingRoomButton = view.findViewById(R.id.fragment_seat_show_reading_room_button);
        showReadingRoomButton.setOnClickListener(this);
        FrameLayout showStudyRoomButton = view.findViewById(R.id.fragment_seat_show_study_room_button);
        showStudyRoomButton.setOnClickListener(this);

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
            case R.id.fragment_seat_show_reading_room_button:
                intent = LibraryListActivity.newIntent(getActivity(), LibraryListActivity.TYPE_READING_ROOM);
                startActivity(intent);
                break;
            case R.id.fragment_seat_show_study_room_button:
                intent = LibraryListActivity.newIntent(getActivity(), LibraryListActivity.TYPE_STUDY_ROOM);
                startActivity(intent);
                break;
            case R.id.fragment_seat_show_empty_room_button:
                Toast.makeText(getActivity(), "빠른 시일 내에 추가될 예정입니다.", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
