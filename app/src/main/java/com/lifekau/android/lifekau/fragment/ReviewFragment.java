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
import com.lifekau.android.lifekau.activity.FoodReviewCornerListActivity;
import com.lifekau.android.lifekau.activity.LectureReviewSearchActivity;

public class ReviewFragment extends PagerFragment implements View.OnClickListener{
    private FrameLayout mGoToFoodReviewButton;
    private FrameLayout mGoToLectureReviewButton;

    public static ReviewFragment newInstance(){
        ReviewFragment fragment = new ReviewFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);
        findFragmentContainer(view);
        mGoToFoodReviewButton = view.findViewById(R.id.go_to_food_review_button);
        mGoToFoodReviewButton.setOnClickListener(this);
        mGoToLectureReviewButton = view.findViewById(R.id.go_to_lecture_review_button);
        mGoToLectureReviewButton.setOnClickListener(this);

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
        mFragmentContainer = view.findViewById(R.id.fragment_review_container);
    }

    @Override
    public void refresh() {

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()){
            case R.id.go_to_food_review_button:
                intent = FoodReviewCornerListActivity.newIntent(getActivity());
                startActivity(intent);
                break;
            case R.id.go_to_lecture_review_button:
                intent = LectureReviewSearchActivity.newIntent(getActivity());
                startActivity(intent);
                break;
        }
    }
}
