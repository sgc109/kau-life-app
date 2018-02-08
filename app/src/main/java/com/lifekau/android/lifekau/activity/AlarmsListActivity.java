package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lifekau.android.lifekau.OnAlarmClickListener;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.manager.AlarmManager;
import com.lifekau.android.lifekau.model.Alarm;
import com.lifekau.android.lifekau.viewholder.AlarmViewHolder;

import java.util.List;

public class AlarmsListActivity extends AppCompatActivity implements OnAlarmClickListener {
    RecyclerView mRecyclerView;
    RecyclerView.Adapter<AlarmViewHolder> mAdapter;
    TextView mEmptyListTextView;
    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, AlarmsListActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms_list);

        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        mEmptyListTextView = findViewById(R.id.alarms_list_empty_text_view);

        mRecyclerView = findViewById(R.id.alarms_list_recycler_view);
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new RecyclerView.Adapter<AlarmViewHolder>() {
            @Override
            public AlarmViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.list_item_alarms, parent, false);
                AlarmViewHolder holder = new AlarmViewHolder(view, mAdapter, mRecyclerView, mEmptyListTextView);
                holder.setOnAlarmClickListener(AlarmsListActivity.this);
                return holder;
            }

            @Override
            public void onBindViewHolder(AlarmViewHolder holder, int position) {
                holder.bind(AlarmManager.get(AlarmsListActivity.this).getAlarms().get(position));
            }

            @Override
            public int getItemCount() {
                return AlarmManager.get(AlarmsListActivity.this).getAlarms().size();
            }
        };
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                AlarmManager.get(AlarmsListActivity.this).removeAlarm(((AlarmViewHolder)viewHolder).getAlarm());
                mAdapter.notifyDataSetChanged();
//                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition()); // 흠.. 전체 업뎃안하고 해당 번호만 업뎃해도되려나.. 왠지 뭔가가 있어서 안될수도있을것같은..
                setTextIfEmptyList();
            }
        });
        swipeToDismissTouchHelper.attachToRecyclerView(mRecyclerView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        setTextIfEmptyList();
    }

    private void setTextIfEmptyList(){
        if(AlarmManager.get(this).getAlarms().size() == 0){
            mEmptyListTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAlarmClick() {
        mAdapter.notifyDataSetChanged();
        setTextIfEmptyList();
    }
}
