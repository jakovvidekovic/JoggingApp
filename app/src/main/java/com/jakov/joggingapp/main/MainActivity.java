package com.jakov.joggingapp.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.jakov.joggingapp.R;
import com.jakov.joggingapp.extra.Const;
import com.jakov.joggingapp.extra.Run;
import com.jakov.joggingapp.jogging.JoggingFragment;
import com.jakov.joggingapp.reports.ReportsFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.ui.ParseLoginBuilder;

import java.util.List;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {
    public static final int LOGIN_REQUEST = 0;


    ParseUser currentUser;

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            ParseLoginBuilder loginBuilder = new ParseLoginBuilder(
                    this);
            startActivityForResult(loginBuilder.build(), LOGIN_REQUEST);
        }
    }
    SharedPreferences sharedPreferences;

    @Override
    protected void onPause() {
        super.onPause();
        sharedPreferences.edit().putInt(Const.PREFS_CURRENT_TAB,mViewPager.getCurrentItem()).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sharedPreferences==null){
            sharedPreferences=getSharedPreferences(Const.SHARED_PREFS_KEY,MODE_PRIVATE);
        }
        mViewPager.setCurrentItem(sharedPreferences.getInt(Const.PREFS_CURRENT_TAB,0));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOGIN_REQUEST && resultCode == RESULT_OK) {
            mViewPager.setAdapter(null);
            Run.unpinAllInBackground();
            ParseQuery<Run> online = new ParseQuery<Run>(Run.class);
            online.findInBackground(new FindCallback<Run>() {
                @Override
                public void done(List<Run> runs, ParseException e) {
                    if (e == null)
                        Run.pinAllInBackground(runs, new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    mViewPager.setAdapter(mSectionsPagerAdapter);
                                }
                            }
                        });

                }
            });
        }
    }

    ViewPager mViewPager;
    SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this)
            );
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            ParseUser.logOut();
            currentUser = null;
            onStart();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0)
                return new JoggingFragment();
            if(position==1){
                return new ReportsFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Jogging";
                case 1:
                    return "Reports";
            }
            return null;
        }
    }
}
