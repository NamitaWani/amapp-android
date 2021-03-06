/*
 * Copyright (c) 2016. Anoopam Mission. This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License at "http://www.gnu.org/licenses/licenses.html" for more details.
 *
 */

package org.anoopam.main.news;

import android.content.ContentValues;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.transition.Slide;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import org.anoopam.main.AMAppMasterActivity;
import org.anoopam.main.R;
import org.anoopam.main.common.AMConstants;
import org.anoopam.main.common.AMServiceRequest;
import org.anoopam.ext.smart.caching.SmartCaching;
import org.anoopam.ext.smart.weservice.SmartWebManager;

import java.util.ArrayList;

/**
 * Created by dadesai on 1/1/16.
 */
public class NewsListActivity extends AMAppMasterActivity {

    private static final String TAG = "NewsListActivity";
    private FrameLayout frmListFragmentContainer;
    private NewsListFragment newsListFragment;
    private ArrayList<ContentValues> newsImages = new ArrayList<>();
    private SmartCaching smartCaching;
    private boolean isCachedDataDisplayed = false;

    @Override
    public View getLayoutView() {
        return null;
    }

    @Override
    public int getLayoutID() {
        return R.layout.temples_activity;
    }

    @Override
    public View getFooterLayoutView() {
        return null;
    }

    @Override
    public int getFooterLayoutID() {
        return 0;
    }

    @Override
    public View getHeaderLayoutView() {
        return null;
    }

    @Override
    public int getHeaderLayoutID() {
        return 0;
    }

    @Override
    public void setAnimations() {
        super.setAnimations();
        getWindow().setEnterTransition(new Slide(Gravity.RIGHT));
        getWindow().setReturnTransition(new Slide(Gravity.BOTTOM));
    }

    @Override
    public void preOnCreate() {

    }

    @Override
    public void initComponents() {
        super.initComponents();

        frmListFragmentContainer = (FrameLayout) findViewById(R.id.frmListFragmentContainer);
        newsListFragment = new NewsListFragment();
        smartCaching = new SmartCaching(this);
    }

    @Override
    public void prepareViews() {
        getSupportFragmentManager().beginTransaction().add(R.id.frmListFragmentContainer, newsListFragment).commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getNewsUpdates();
            }
        }, 500);
    }

    @Override
    public void setActionListeners() {
        super.setActionListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectDrawerItem(NAVIGATION_ITEMS.THAKORJI_TODAY);
    }

    @Override
    public void manageAppBar(ActionBar actionBar, Toolbar toolbar, ActionBarDrawerToggle actionBarDrawerToggle) {
        toolbar.setTitle(getString(R.string.app_subtitle));
        SpannableString spannableString=new SpannableString(getString(R.string.app_subtitle));
        spannableString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spannableString.length(), 0);
        toolbar.setSubtitle(spannableString);
    }

    @Override
    public boolean shouldKeyboardHideOnOutsideTouch() {
        return false;
    }

    @Override
    protected void onStop() {
        super.onStop();

        SmartWebManager.getInstance(this).getRequestQueue().cancelAll(AMConstants.AMS_Request_Get_Temples_Tag);
    }

    private void getNewsUpdates() {
        ArrayList<ContentValues> newsImages = smartCaching.getDataFromCache("newsImages");
        if (newsImages == null || newsImages.size() <= 0) {
            AMServiceRequest.getInstance().startNewsUpdatesFromServer();
            isCachedDataDisplayed = false;
        } else {
            this.newsImages = newsImages;
            setTempleDataInFragments(newsImages, isCachedDataDisplayed);
            isCachedDataDisplayed = true;
        }
    }

    private void setTempleDataInFragments(ArrayList<ContentValues> temples, boolean isCachedDataDisplayed) {
        newsListFragment.setTemples(temples, isCachedDataDisplayed);
    }
}