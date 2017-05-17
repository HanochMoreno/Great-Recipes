//package com.hanoch.greatrecipes.view;
//
//import android.os.Bundle;
//import android.support.design.widget.TabLayout;
//import android.support.v4.app.Fragment;
////import android.content.Intent;
////import android.support.v4.content.LocalBroadcastManager;
//import android.support.v4.view.ViewPager;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
////import com.hanoch.greatrecipes.AppConsts;
//import com.hanoch.greatrecipes.R;
//import com.hanoch.greatrecipes.bus.MyBus;
//import com.hanoch.greatrecipes.bus.OnTabChangedEvent;
//import com.hanoch.greatrecipes.view.adapters.PagerAdapter;
//
//
//public class TabsFragment extends Fragment {
//
//    private static final String ARG_EXTRA_SERVING = "serving";
//    private ViewPager viewPager;
////    private Intent broadcastIntent;
//    private MyBus bus;
//
//    public static TabsFragment newInstance(String extra_serving) {
//
//        TabsFragment fragment = new TabsFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_EXTRA_SERVING, extra_serving);
//        fragment.setArguments(args);
//
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        bus = MyBus.getInstance();
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_tabs, container, false);
//
//        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
//
//        tabLayout.addTab(tabLayout.newTab().setText(R.string.online));
//        tabLayout.addTab(tabLayout.newTab().setText(R.string.my_own));
//        tabLayout.addTab(tabLayout.newTab().setText(R.string.favourites));
//
//        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
//
//        String extra_serving = getArguments().getString(ARG_EXTRA_SERVING);
//
//        viewPager = (ViewPager) view.findViewById(R.id.pager);
//        viewPager.setOffscreenPageLimit(2);
//        PagerAdapter adapter = new PagerAdapter(getFragmentManager(), tabLayout.getTabCount(), extra_serving);
//        viewPager.setAdapter(adapter);
//        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
//
//        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//                viewPager.setCurrentItem(tab.getPosition());
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//
//                bus.post(new OnTabChangedEvent());
//
//                /*broadcastIntent = new Intent(AppConsts.Actions.ACTION_TAB_CHANGED);
//                LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getContext());
//                lbm.sendBroadcast(broadcastIntent);*/
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//            }
//        });
//
//        return view;
//    }
//
//}
