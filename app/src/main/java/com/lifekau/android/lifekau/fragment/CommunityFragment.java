package com.lifekau.android.lifekau.fragment;


import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lifekau.android.lifekau.R;

public class CommunityFragment extends PagerFragment {

    private RecyclerView mRecyclerView;
    public static CommunityFragment newInstance() {
        CommunityFragment fragment = new CommunityFragment();
        return fragment;
    }
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_community, container, false);
        mRecyclerView = view.findViewById(R.id.community_recycler_view);
        mRecyclerView.setAdapter(new RecyclerView.Adapter<PostViewHolder>() {
            @Override
            public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view;
                if(viewType == 1) {
                    LinearLayout.LayoutParams params = new
                            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT);
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_upper_blank, parent, false);
//                    params.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics());
//                    view.findViewById(R.id.card_view).setLayoutParams(params);
                } else {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post, parent, false);
                }
                return new PostViewHolder(view);
            }

            @Override
            public int getItemViewType(int position) {
                if(position == 0) return 1;
                return 2;
            }

            @Override
            public void onBindViewHolder(PostViewHolder holder, int position) {
                holder.bind(position);
            }

            @Override
            public int getItemCount() {
                return 50;
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_items_alarm, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    class PostViewHolder extends RecyclerView.ViewHolder{
        CardView mCardView;
        TextView mTextView;
        public PostViewHolder(View itemView) {
            super(itemView);
            mCardView = itemView.findViewById(R.id.card_view);
            mTextView = itemView.findViewById(R.id.layout_item_demo_title);
        }
        public void bind(int position){
        }
    }

    @Override
    public void findFragmentContainer(View view) {
        mFragmentContainer = view.findViewById(R.id.fragment_community_container);
    }

    @Override
    public void refresh() {

    }
}
