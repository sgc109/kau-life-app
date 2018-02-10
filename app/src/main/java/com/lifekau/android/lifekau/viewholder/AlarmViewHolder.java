package com.lifekau.android.lifekau.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;

import com.lifekau.android.lifekau.OnAlarmClickListener;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.activity.AlarmsListActivity;
import com.lifekau.android.lifekau.manager.AlarmManager;
import com.lifekau.android.lifekau.model.Alarm;

/**
 * Created by sgc109 on 2018-02-07.
 */

public class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView mContentTextView;
    private Alarm mAlarm;
    private Context mContext;
    private RecyclerView.Adapter mActivityAdapter;
    private TextView mActivityTextView;
    private RecyclerView mActivityRecyclerView;
    private OnAlarmClickListener mOnAlarmClickListener;

    public AlarmViewHolder(View itemView, RecyclerView.Adapter adapter, RecyclerView recyclerView, TextView textView) {
        super(itemView);
        mContentTextView = itemView.findViewById(R.id.list_item_alarms_text_view);
        mActivityAdapter = adapter;
        mActivityRecyclerView = recyclerView;
        mActivityTextView = textView;
        mContext = itemView.getContext();
        itemView.setOnClickListener(this);
    }

    public void bind(Alarm alarm) {
        mAlarm = alarm;
        mContentTextView.setText(alarm.getText());
    }

    public Alarm getAlarm(){
        return mAlarm;
    }

    @Override
    public void onClick(View view) {
    AlarmManager.get(mContext).removeAlarm(mAlarm);
    mActivityAdapter.notifyDataSetChanged();
        mOnAlarmClickListener.onAlarmClick();
        Toast.makeText(mContext, "onClick!", Toast.LENGTH_SHORT).show();
        switch (mAlarm.getType()) {
            case Alarm.TYPE_COMMENT:

                break;
            case Alarm.TYPE_LIKE:

                break;
            case Alarm.TYPE_NOTICE:

                break;
            case Alarm.TYPE_LECTURE_MATERIAL:

                break;
            case Alarm.TYPE_GRADE:

                break;
            case Alarm.TYPE_TEST_SCHEDULE:

                break;
            default:
                return;
        }
    }

    public void setOnAlarmClickListener(OnAlarmClickListener listener){
        mOnAlarmClickListener = listener;
    }
}
