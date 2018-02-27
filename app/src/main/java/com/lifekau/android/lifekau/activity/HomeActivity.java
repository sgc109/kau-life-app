package com.lifekau.android.lifekau.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.lifekau.android.lifekau.FABHideOnScrollBehavior;
import com.lifekau.android.lifekau.adapter.HomeViewPagerAdapter;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.fragment.PagerFragment;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener {

    private PagerFragment currentFragment;
    private HomeViewPagerAdapter adapter;
    private AHBottomNavigationAdapter navigationAdapter;
    private ArrayList<AHBottomNavigationItem> bottomNavigationItems = new ArrayList<>();
    private int[] tabColors;

    private AHBottomNavigationViewPager viewPager;
    private AHBottomNavigation bottomNavigation;
    private FloatingActionButton mFab;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean enabledTranslucentNavigation = getSharedPreferences("shared", Context.MODE_PRIVATE)
                .getBoolean("translucentNavigation", false);
        setTheme(enabledTranslucentNavigation ? R.style.AppTheme_TranslucentNavigation : R.style.Theme_AppCompat_Light_NoActionBar);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        initUI();
    }

    private void initUI() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        bottomNavigation = findViewById(R.id.home_bottom_navigation_bar);
        viewPager = findViewById(R.id.home_view_pager);
        mFab = findViewById(R.id.new_post_fab);
        setFabOnClickListenerToWritePost();

        tabColors = getApplicationContext().getResources().getIntArray(R.array.tab_colors);
        navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_menu_5);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation, tabColors);

        bottomNavigation.manageFloatingActionButtonBehavior(mFab);
        bottomNavigation.setTranslucentNavigationEnabled(true);

        bottomNavigation.setOnTabSelectedListener(this);

        viewPager.setOffscreenPageLimit(4);
        adapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        currentFragment = adapter.getCurrentFragment();

        updateBottomNavigationItems();

        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
        params.setBehavior(new FABHideOnScrollBehavior());
        mFab.requestLayout();
    }

    public void updateBottomNavigationColor(boolean isColored) {
        bottomNavigation.setColored(isColored);
    }

    public boolean isBottomNavigationColored() {
        return bottomNavigation.isColored();
    }

    public void updateBottomNavigationItems() {
        bottomNavigation.setAccentColor(getResources().getColor(R.color.colorPrimaryDark));
//        bottomNavigation.setNotification("1", 2);
//        bottomNavigation.setNotification("1", 4);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_alarm:
                intent = AlarmsListActivity.newIntent(this);
                startActivity(intent);
                return true;
            case R.id.menu_setting:
                intent = SettingsActivity.newIntent(this);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setTitleState(AHBottomNavigation.TitleState titleState) {
        bottomNavigation.setTitleState(titleState);
    }

    public void reload() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    public int getBottomNavigationNbItems() {
        return bottomNavigation.getItemsCount();
    }

    private void setFabOnClickListenerToWritePost() {
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = PostWriteActivity.newIntent(HomeActivity.this);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        if (currentFragment == null) {
            currentFragment = adapter.getCurrentFragment();
        }
        if (wasSelected) {
            currentFragment.refresh();
            return true;
        }
        if (currentFragment != null) {
            currentFragment.willBeHidden();
        }
        viewPager.setCurrentItem(position, false);

        if (currentFragment == null) {
            return true;
        }
        currentFragment = adapter.getCurrentFragment();
        currentFragment.willBeDisplayed();

        if (position == 0) {
            bottomNavigation.setNotification("", 1);

            mFab.setVisibility(View.VISIBLE);
            mFab.setAlpha(0f);
            mFab.setScaleX(0f);
            mFab.setScaleY(0f);
            mFab.animate()
                    .alpha(1)
                    .scaleX(1)
                    .scaleY(1)
                    .setDuration(150)
                    .setInterpolator(new OvershootInterpolator())
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mFab.animate()
                                    .setInterpolator(new LinearOutSlowInInterpolator())
                                    .start();
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    })
                    .start();

        } else {
            if (mFab.getVisibility() == View.VISIBLE) {
                mFab.setVisibility(View.INVISIBLE);
            }
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            // 정말 종료하시겠습니까? 다시 묻지 않기 체크
            return super.onKeyDown(keyCode, event);
//            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
