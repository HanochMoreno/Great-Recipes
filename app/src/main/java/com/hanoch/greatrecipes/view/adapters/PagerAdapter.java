package com.hanoch.greatrecipes.view.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.hanoch.greatrecipes.view.MyListFragment;
import com.hanoch.greatrecipes.view.ListFavouriteFragment2;
import com.hanoch.greatrecipes.view.ListMyOwnFragment2;
import com.hanoch.greatrecipes.view.ListOnlineFragment2;

public class PagerAdapter extends FragmentStatePagerAdapter {

    private int mNumOfTabs;
    private int action;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, int action) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.action = action;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return MyListFragment.newInstance(new ListOnlineFragment2(), action);

            case 1:
                return MyListFragment.newInstance(new ListMyOwnFragment2(), action);

            case 2:
                return MyListFragment.newInstance(new ListFavouriteFragment2(), action);

            default:
                return null;
        }
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public int getCount() {
        return mNumOfTabs;
    }

}