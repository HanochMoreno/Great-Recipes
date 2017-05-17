package com.hanoch.greatrecipes.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.bus.MyBus;
import com.hanoch.greatrecipes.bus.OnTabChangedEvent;
import com.hanoch.greatrecipes.view.adapters.PagerAdapter;


public class TabsFragment2 extends Fragment {

    private static final String ARG_ACTION = "ARG_ACTION";
    private ViewPager viewPager;
    private MyBus bus;
    private ProgressDialog progressDialog;

    public static TabsFragment2 newInstance(int action) {

        TabsFragment2 fragment = new TabsFragment2();
        Bundle args = new Bundle();
        args.putInt(ARG_ACTION, action);
        fragment.setArguments(args);

        return fragment;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle(getString(R.string.loading_info));
        progressDialog.setMessage(getString(R.string.please_wait));

        bus = MyBus.getInstance();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tabs, container, false);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.online));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.my_own));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.favourites));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        int action = getArguments().getInt(ARG_ACTION);

        viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(2);
        PagerAdapter adapter = new PagerAdapter(getFragmentManager(), tabLayout.getTabCount(), action);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                bus.post(new OnTabChangedEvent());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return view;
    }

}
