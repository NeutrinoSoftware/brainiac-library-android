package net.neutrinosoft.brainactivity.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;

import net.neutrinosoft.brainactivity.R;
import net.neutrinosoft.brainactivity.adapters.MainActivityPagerAdapter;

import butterknife.InjectView;

public class MainActivity extends BaseActivity {

    @InjectView(R.id.tabs)
    PagerSlidingTabStrip tabs;
    @InjectView(R.id.viewPager)
    ViewPager viewPager;


    public static final String EXTRA_VALUES = "EXTRA_VALUES";


    public static Intent createIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MainActivityPagerAdapter adapter = new MainActivityPagerAdapter(getSupportFragmentManager());

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        tabs.setViewPager(viewPager);
    }


}
