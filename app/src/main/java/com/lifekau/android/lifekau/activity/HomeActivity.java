package com.lifekau.android.lifekau.activity;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationAdapter;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationViewPager;
import com.lifekau.android.lifekau.R;
import com.lifekau.android.lifekau.adapter.HomeViewPagerAdapter;
import com.lifekau.android.lifekau.adapter.PostRecyclerAdapter;
import com.lifekau.android.lifekau.fragment.CommunityFragment;
import com.lifekau.android.lifekau.fragment.PagerFragment;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements AHBottomNavigation.OnTabSelectedListener {

    private static final int REQUEST_POST_WRITE = 0;
    public static final int REQUEST_POST_DETAIL = 1;
    public static final int REQUEST_SETTINGS = 2;
    public static final String EXTRA_HAS_PUSHED_LOGOUT = "extra_has_pushed_logout";
    public static final String EXTRA_WAS_POST_DELETED = "extra_was_post_deleted";
    public static final String EXTRA_ITEM_POSITION = "extra_item_position";
    private static final String CURRENT_POSITION = "current_position";
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
    private long mPressedTime;

    public static Intent newIntent(Context context, int position) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.putExtra(CURRENT_POSITION, position);
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
        mPressedTime = 0;
        initUI();
    }

    private void initUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }

//        mAppBarLayout = findViewById(R.id.home_app_bar_layout);
        bottomNavigation = findViewById(R.id.home_bottom_navigation_bar);
        viewPager = findViewById(R.id.home_view_pager);

        tabColors = getApplicationContext().getResources().getIntArray(R.array.tab_colors);
        navigationAdapter = new AHBottomNavigationAdapter(this, R.menu.bottom_navigation_menu_5);
        navigationAdapter.setupWithBottomNavigation(bottomNavigation, tabColors);

        viewPager.setOffscreenPageLimit(4);
        adapter = new HomeViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        currentFragment = adapter.getCurrentFragment();

        updateBottomNavigationItems();

        mFab = findViewById(R.id.new_post_fab);
        setFabOnClickListenerToWritePost();
        bottomNavigation.manageFloatingActionButtonBehavior(mFab);
        bottomNavigation.setTranslucentNavigationEnabled(true);
        bottomNavigation.setOnTabSelectedListener(this);
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
            case R.id.menu_refresh:
                final CommunityFragment fragment = (CommunityFragment) adapter.getCurrentFragment();
                final RecyclerView recyclerView = fragment.getRecyclerView();
                final SwipeRefreshLayout swipeRefreshLayout = fragment.getSwipeRefreshLayout();
                swipeRefreshLayout.post(new Runnable() {
                    @Override
                    public void run() {
                        fragment.onRefreshManually();
                        recyclerView.smoothScrollToPosition(0);
//                    mAppBarLayout.setExpanded(true, true);
                        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
                    }
                });
                return true;
            case R.id.menu_community_setting:
                intent = SettingsActivity.newIntent(this);
                startActivityForResult(intent, REQUEST_SETTINGS);
                return true;
            case R.id.menu_setting:
                intent = SettingsActivity.newIntent(this);
                startActivityForResult(intent, REQUEST_SETTINGS);
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
        } else if (requestCode == REQUEST_POST_DETAIL) {
            boolean wasPostDeleted = data.getBooleanExtra(EXTRA_WAS_POST_DELETED, false);
            int itemPosition = data.getIntExtra(EXTRA_ITEM_POSITION, 0);
            if (wasPostDeleted) {
                PostRecyclerAdapter adapter = fragment.getAdapter();
                adapter.mPosts.remove(itemPosition);
                adapter.mPostKeys.remove(itemPosition);
                adapter.notifyItemRemoved(itemPosition);
            } else {
                fragment.updatePost(itemPosition);
            }
        } else if (requestCode == REQUEST_SETTINGS) {
            Intent intent = LoginActivity.newIntent(this);
            startActivity(intent);
            finish();
        }
    }

    @Override
    public boolean onTabSelected(int position, boolean wasSelected) {
        if (currentFragment == null) {
            currentFragment = adapter.getCurrentFragment();
        }
        if (wasSelected && currentFragment != null) {
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

        setFabVisibility(position);
        return true;
    }

    private void setFabVisibility(int position) {
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
                    .setDuration(300)
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
            mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
//            mAppBarLayout.setExpanded(true, true);
            if (mFab.getVisibility() == View.VISIBLE) {
                mFab.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mPressedTime == 0) {
            Toast.makeText(this, " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show();
            mPressedTime = System.currentTimeMillis();
        } else {
            int seconds = (int) (System.currentTimeMillis() - mPressedTime);
            if (seconds > 2000) {
                Toast.makeText(this, " 한 번 더 누르면 종료됩니다.", Toast.LENGTH_LONG).show();
                mPressedTime = 0;
            } else {
                super.onBackPressed();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void hideViews() {
        mToolbar.animate().translationY(-mToolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2));

//        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
//        int fabBottomMargin = lp.bottomMargin;
//        mFab.animate().translationY(mFab.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();

        mFab.hide(new FloatingActionButton.OnVisibilityChangedListener() {
            @Override
            public void onHidden(FloatingActionButton fab) {
                super.onHidden(fab);
                fab.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        int position = getIntent().getIntExtra(CURRENT_POSITION, -1);
        getIntent().removeExtra(CURRENT_POSITION);
        if (position != -1) {
            bottomNavigation.setCurrentItem(position);
            if (position != 0) mFab.setVisibility(View.INVISIBLE);
        }
    }

    public void showViews() {
        mToolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
//        mFab.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        mFab.show();
    }
}
