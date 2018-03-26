package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.model.EmptyLectureRoom;
import com.lifekau.android.lifekau.model.LectureRoomTimeTable;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class EmptyLectureRoomActivity extends AppCompatActivity {

    private static final int NO_NEXT_TIME = 0;
    private static final String EXTRA_INDEX = "extra_index";
    private static final String DATABASE_FILE_NAME = "LectureRoomTimeTable.db";
    private static final String[][] TYPE_NAME = {{"과학관", "전산실", "어학실"},
            {"전자관"},
            {"기계관", "대강당"},
            {"강의동"},
            {"학군단"}};

    private int mTypeIndex;
    private ArrayList<EmptyLectureRoom> mEmptyLectureRooms;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView mRecyclerView;
    private ViewGroup mMainLayout;
    private ViewGroup mProgressBarLayout;

    public static Intent newIntent(Context context, int typeIndex) {
        Intent intent = new Intent(context, EmptyLectureRoomActivity.class);
        intent.putExtra(EXTRA_INDEX, typeIndex);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_lecture_room);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        mTypeIndex = getIntent().getIntExtra(EXTRA_INDEX, 1);
        mEmptyLectureRooms = new ArrayList<>();
        getLectureList();
        mRecyclerAdapter = new RecyclerView.Adapter<RectureRoomItemViewHolder>() {
            @Override
            public RectureRoomItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_empty_lecture_room, parent, false);
                return new RectureRoomItemViewHolder(view);
            }

            @Override
            public void onBindViewHolder(RectureRoomItemViewHolder holder, int position) {
                holder.bind(position);
            }

            @Override
            public int getItemCount() {
                return mEmptyLectureRooms.size();
            }
        };
        mRecyclerView = findViewById(R.id.empty_lecture_room_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mMainLayout = findViewById(R.id.empty_lecture_room_main_layout);
        mMainLayout.setVisibility(View.VISIBLE);
        mProgressBarLayout = findViewById(R.id.empty_lecture_room_progress_bar_layout);
        mProgressBarLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public class RectureRoomItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mLectureRoomNameTextView;
        private TextView mNextTimeTextView;

        private RectureRoomItemViewHolder(View itemView) {
            super(itemView);
            mLectureRoomNameTextView = itemView.findViewById(R.id.list_item_empty_lecture_room_name);
            mNextTimeTextView = itemView.findViewById(R.id.list_item_empty_lecture_room_next_time);
        }

        public void bind(int position) {
            mLectureRoomNameTextView.setText(mEmptyLectureRooms.get(position).lectureRoomName);
            mNextTimeTextView.setText(mEmptyLectureRooms.get(position).nextTime);
        }
    }

    public void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    public void getLectureList() {
        Calendar now = Calendar.getInstance();
        int currTime = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int currDayIndex = now.get(Calendar.DAY_OF_WEEK);
        try {
            InputStream is = getAssets().open(DATABASE_FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(is);
            Set<String> usedLectureName = new HashSet<>();
            ArrayList<LectureRoomTimeTable> timeTables = (ArrayList<LectureRoomTimeTable>)ois.readObject();
            for (LectureRoomTimeTable nextTimeTable : timeTables) {
                if (currDayIndex >= 2 && currDayIndex <= 6 && nextTimeTable.index != currDayIndex)
                    continue;
                for (String buildingName : TYPE_NAME[mTypeIndex]) {
                    if (!nextTimeTable.lectureRoomName.contains(buildingName)) continue;
                    boolean isEmpty = true;
                    if(usedLectureName.contains(nextTimeTable.lectureRoomName)) break;
                    usedLectureName.add(nextTimeTable.lectureRoomName);
                    if(currDayIndex == 1 || currDayIndex == 7){
                        isEmpty = false;
                        addEmptyLectureRoom(nextTimeTable.lectureRoomName, NO_NEXT_TIME);
                    }
                    int prevTime = NO_NEXT_TIME;
                    int nextStartTime = NO_NEXT_TIME;
                    for (int i = 0; i < nextTimeTable.startTimes.size(); i++) {
                        if (nextTimeTable.startTimes.get(i) <= currTime &&
                                currTime <= nextTimeTable.endTimes.get(i)) isEmpty = false;
                        if(prevTime <= currTime && currTime <= nextTimeTable.startTimes.get(i)){
                            nextStartTime = nextTimeTable.startTimes.get(i);
                            break;
                        }
                        prevTime = nextTimeTable.endTimes.get(i);
                    }
                    if (!isEmpty) break;
                    addEmptyLectureRoom(nextTimeTable.lectureRoomName, nextStartTime);
                }
            }
            for(LectureRoomTimeTable nextTimeTable : timeTables){
                if(usedLectureName.contains(nextTimeTable.lectureRoomName)) continue;
                for (String buildingName : TYPE_NAME[mTypeIndex]) {
                    if(!nextTimeTable.lectureRoomName.contains(buildingName)) continue;
                    usedLectureName.add(nextTimeTable.lectureRoomName);
                    addEmptyLectureRoom(nextTimeTable.lectureRoomName, NO_NEXT_TIME);
                    break;
                }
            }
            Collections.sort(mEmptyLectureRooms, new Comparator<EmptyLectureRoom>() {
                @Override
                public int compare(EmptyLectureRoom o1, EmptyLectureRoom o2) {
                    return o1.lectureRoomName.compareTo(o2.lectureRoomName);
                }
            });
            ois.close();
        } catch (Exception e) {
            e.printStackTrace();
            showToast("강의실 시간표 목록을 가져오는 도중 오류가 발생하였습니다.");
        }
    }

    public void addEmptyLectureRoom(String lectureRoomName, int nextTime){
        EmptyLectureRoom emptyLectureRoom = new EmptyLectureRoom();
        emptyLectureRoom.lectureRoomName = lectureRoomName;
        emptyLectureRoom.nextTime = getNextTime(nextTime);
        mEmptyLectureRooms.add(emptyLectureRoom);
    }

    public String getNextTime(int nextStartTime){
        return nextStartTime != NO_NEXT_TIME ? String.format(Locale.getDefault(), "%02d:%02d", nextStartTime / 60, nextStartTime % 60)  : "없음";
    }
}