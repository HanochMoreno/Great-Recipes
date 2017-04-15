package com.hanoch.greatrecipes.view.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import com.hanoch.greatrecipes.view.ListFavouriteFragment;
import com.hanoch.greatrecipes.view.ListMyOwnFragment;
import com.hanoch.greatrecipes.view.ListOnlineFragment;

public class PagerAdapter extends FragmentStatePagerAdapter {

    int mNumOfTabs;
    String extra_serving;

    public PagerAdapter(FragmentManager fm, int NumOfTabs, String extra_serving) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.extra_serving = extra_serving;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public Fragment getItem(int position) {

        switch (position) {

            case 0:
                return ListOnlineFragment.newInstance(extra_serving);

            case 1:
                return ListMyOwnFragment.newInstance(extra_serving);

            case 2:
                return ListFavouriteFragment.newInstance(extra_serving);

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





