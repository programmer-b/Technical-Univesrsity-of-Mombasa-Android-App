package com.tum.app;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;


public class UserGroup extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    TabItem tabItem1, tabItem2, tabItem3;
    GroupAdapter groupAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_group);

        findViewById(R.id.back_button).setOnClickListener(view -> finish());

        tabLayout = findViewById(R.id.tabsLayout);
        viewPager = findViewById(R.id.ViewPager);

        tabItem1 = findViewById(R.id.memosTab);
        tabItem2 = findViewById(R.id.notesTab);
        tabItem3 = findViewById(R.id.assignmentsTab);

        groupAdapter = new GroupAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(groupAdapter);;

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                groupAdapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String title = sharedPreferences.getString("userGroup",null);

        toolbar.setTitle(title);
    }
    @Override
    public void onBackPressed(){
        finish();
    }
}