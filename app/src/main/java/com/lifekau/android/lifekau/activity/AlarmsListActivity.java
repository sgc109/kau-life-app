package com.lifekau.android.lifekau.activity;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.viewholder.AlarmViewHolder;

public class AlarmsListActivity extends AppCompatActivity {
    RecyclerView mRecyclerView;

    public static Intent newIntent(Context context){
        Intent intent = new Intent(context, AlarmsListActivity.class);
        return intent;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms_list);

        mRecyclerView = findViewById(R.id.alarms_list_recycler_view);
        mRecyclerView.setAdapter(new RecyclerView.Adapter<AlarmViewHolder>() {
            @Override
            public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return null;
            }

            @Override
            public void onBindViewHolder(AlarmViewHolder holder, int position) {

            }

            @Override
            public int getItemCount() {
                return 0;
            }
        });
        CardView cardView = findViewById(R.id.list_item_alarms_card_view);

        final SwipeDismissBehavior<CardView> swipe
                = new SwipeDismissBehavior();

        swipe.setSwipeDirection(
                SwipeDismissBehavior.SWIPE_DIRECTION_ANY);

        swipe.setListener(
                new SwipeDismissBehavior.OnDismissListener() {
                    @Override public void onDismiss(View view) {
                        Toast.makeText(AlarmsListActivity.this,
                                "Card swiped !!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDragStateChanged(int state) {}
                });
        CoordinatorLayout.LayoutParams coordinatorParams =
                (CoordinatorLayout.LayoutParams) cardView.getLayoutParams();

        coordinatorParams.setBehavior(swipe);
    }
}
