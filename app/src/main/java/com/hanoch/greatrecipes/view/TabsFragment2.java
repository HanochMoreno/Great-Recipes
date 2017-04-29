package com.hanoch.greatrecipes.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hanoch.greatrecipes.AppStateManager;
import com.hanoch.greatrecipes.R;
import com.hanoch.greatrecipes.api.great_recipes_api.UserRecipesResponse;
import com.hanoch.greatrecipes.model.ApiProvider;
import com.hanoch.greatrecipes.model.MyBus;
import com.hanoch.greatrecipes.model.RecipesListsDownloadedEvent;
import com.hanoch.greatrecipes.model.TabChangedEvent;
import com.hanoch.greatrecipes.view.adapters.PagerAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import rx.Single;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class TabsFragment2 extends Fragment {

    private static final String ARG_EXTRA_SERVING = "serving";
    private ViewPager viewPager;
    private MyBus bus;
    private ArrayList<Subscriber<UserRecipesResponse>> subscribersList = new ArrayList<>();
    private ProgressDialog progressDialog;

    public static TabsFragment2 newInstance(String extra_serving) {

        TabsFragment2 fragment = new TabsFragment2();
        Bundle args = new Bundle();
        args.putString(ARG_EXTRA_SERVING, extra_serving);
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

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tabs, container, false);

        TabLayout tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);

        tabLayout.addTab(tabLayout.newTab().setText(R.string.online));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.my_own));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.favourites));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        String extra_serving = getArguments().getString(ARG_EXTRA_SERVING);

        viewPager = (ViewPager) view.findViewById(R.id.pager);
        viewPager.setOffscreenPageLimit(2);
        PagerAdapter adapter = new PagerAdapter(getFragmentManager(), tabLayout.getTabCount(), extra_serving);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

                bus.post(new TabChangedEvent());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        return view;
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        downloadUserRecipes();
    }

//-------------------------------------------------------------------------------------------------

    @Override
    public void onDetach() {
        super.onDetach();

        subscribersList.stream()
                .filter(subscriber -> subscriber != null && !subscriber.isUnsubscribed())
                .forEach(subscriber -> {
                    Log.d("RecipeReviewFragment", "onDetach: subscriber is going to get unSubscribed");
                    subscriber.unsubscribe();
                });
    }

//-------------------------------------------------------------------------------------------------

    public void downloadUserRecipes() {

        // TODO: delete
        String userId = "yummly-id-7";

        try {
            userId = URLEncoder.encode(userId, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        progressDialog.show();

        Subscriber<UserRecipesResponse> subscriber = new Subscriber<UserRecipesResponse>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable t) {
                onDownloadRecipesListsCompleted(false, null, t);
            }

            @Override
            public void onNext(UserRecipesResponse response) {
                onDownloadRecipesListsCompleted(true, response, null);
            }
        };

        subscribersList.add(subscriber);

        Single<UserRecipesResponse> getAllUserRecipes =
                ApiProvider.getGreatRecipesApi().getUserRecipes(userId);

        getAllUserRecipes
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(subscriber);
    }

//-------------------------------------------------------------------------------------------------

    private void onDownloadRecipesListsCompleted(boolean isSuccess, UserRecipesResponse response, Throwable t) {
        progressDialog.dismiss();

        if (isFragmentAttached()) {
            if (isSuccess) {
                AppStateManager.getInstance().userRecipes = response;
                bus.post(new RecipesListsDownloadedEvent(response));
            } else {

            }
        }
    }

//-------------------------------------------------------------------------------------------------

    private boolean isFragmentAttached() {
        return (!isDetached() && !isRemoving() && isVisible());
    }

}
