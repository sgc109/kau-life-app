package com.lifekau.android.lifekau;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import com.lifekau.android.lifekau.CommunityFragment;
import com.lifekau.android.lifekau.LmsAndInfoFragment;
import com.lifekau.android.lifekau.NoticeFragment;
import com.lifekau.android.lifekau.ReviewFragment;
import com.lifekau.android.lifekau.SeatFragment;

import java.util.ArrayList;

/**
 *
 */
public class HomeViewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Fragment> fragments = new ArrayList<>();
    private PagerFragment currentFragment;

    public HomeViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments.clear();
        fragments.add(CommunityFragment.newInstance());
        fragments.add(ReviewFragment.newInstance());
        fragments.add(NoticeFragment.newInstance());
        fragments.add(SeatFragment.newInstance());
        fragments.add(LmsAndInfoFragment.newInstance());
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        if (getCurrentFragment() != object) {
            currentFragment = ((PagerFragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    public PagerFragment getCurrentFragment() {
        return currentFragment;
    }
}