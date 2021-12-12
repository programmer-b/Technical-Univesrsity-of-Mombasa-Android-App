package com.tum.app;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


public class GroupAdapter extends FragmentPagerAdapter {

    private final int tabCount;

    public GroupAdapter(@NonNull FragmentManager fm, int tabCount)
    {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.tabCount = tabCount;
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        switch (position){
            case 0:
                return new Memos();

            case 1:
                return new Notes();

            case 2:
                return new Assignments();
            default:

                Fragment o = null;
                assert false;
                return o;
        }
    }

    @Override

    public int getCount()
    {
        return tabCount;
    }

    @Override
    public int getItemPosition(@NonNull Object object){
        return POSITION_NONE;
    }
}
