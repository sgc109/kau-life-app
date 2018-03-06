package com.lifekau.android.lifekau.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.adapter.HomeViewPagerAdapter;
import com.lifekau.android.lifekau.adapter.PostRecyclerAdapter;
import com.lifekau.android.lifekau.fragment.CommunityFragment;
import com.lifekau.android.lifekau.fragment.PagerFragment;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener {

    private static final int REQUEST_POST_WRITE = 0;
    public static final int REQUEST_POST_DETAIL = 1;
    public static final String EXTRA_WAS_POST_DELETED = "extra_was_post_deleted";
    public static final String EXTRA_ITEM_POSITION = "extra_item_position";
    private Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;
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
        mToolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        initUI();
    }

    private void initUI() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

        mFab = findViewById(R.id.new_post_fab);
        CoordinatorLayout.LayoutParams params =
                (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
        params.setBehavior(new FABHideOnScrollBehavior(viewPager));
        mFab.requestLayout();
        setFabOnClickListenerToWritePost();

        mAppBarLayout = findViewById(R.id.home_app_bar_layout);
        bottomNavigation = findViewById(R.id.home_bottom_navigation_bar);
        viewPager = findViewById(R.id.home_view_pager);

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
                startActivityForResult(intent, REQUEST_POST_WRITE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        CommunityFragment fragment = (CommunityFragment) adapter.getCurrentFragment();
        if (requestCode == REQUEST_POST_WRITE) {
            fragment.initPostList();
        } else if(requestCode == REQUEST_POST_DETAIL){
            boolean wasPostDeleted = data.getBooleanExtra(EXTRA_WAS_POST_DELETED, false);
            int itemPosition = data.getIntExtra(EXTRA_ITEM_POSITION, 0);
            if(wasPostDeleted){
                PostRecyclerAdapter adapter = fragment.getAdapter();
                adapter.mPosts.remove(itemPosition);
                adapter.mPostKeys.remove(itemPosition);
                adapter.notifyItemRemoved(itemPosition);
            } else {
                fragment.updatePost(itemPosition);
            }
        }
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
            if(viewPager.getCurrentItem() != 0) {
                return super.onKeyDown(keyCode, event);
            }
            final CommunityFragment fragment = (CommunityFragment) adapter.getCurrentFragment();
            final RecyclerView recyclerView = fragment.getRecyclerView();
            LinearLayoutManager manager = (LinearLayoutManager)recyclerView.getLayoutManager();
            int first = manager.findFirstCompletelyVisibleItemPosition();
            if(first == 0) {
                return super.onKeyDown(keyCode, event);
            }
            final SwipeRefreshLayout swipeRefreshLayout = fragment.getSwipeRefreshLayout();
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                    fragment.onRefreshManually();
                    recyclerView.smoothScrollToPosition(0);
                    mAppBarLayout.setExpanded(true, true);
                }
            });
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
