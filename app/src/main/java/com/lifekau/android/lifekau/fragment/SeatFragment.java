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
import com.lifekau.android.lifekau.activity.ReadingRoomListActivity;

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
                intent = new Intent(view.getContext(), ReadingRoomListActivity.class);
                intent.putExtra("roomType", 0);
                startActivity(intent);
                break;
            case R.id.fragment_seat_show_study_room_button:
                intent = new Intent(view.getContext(), ReadingRoomListActivity.class);
                intent.putExtra("roomType", 1);
                startActivity(intent);
                break;
        }
    }
}
