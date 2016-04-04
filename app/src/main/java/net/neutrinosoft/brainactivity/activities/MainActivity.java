package net.neutrinosoft.brainactivity.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;

import net.neutrinosoft.brainactivity.R;
import net.neutrinosoft.brainactivity.adapters.MainActivityPagerAdapter;


public class MainActivity extends FragmentActivity {


    public static final String EXTRA_VALUES = "EXTRA_VALUES";


    public static Intent createIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //throw new RuntimeException("Crashlytics test");

        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);

        MainActivityPagerAdapter adapter = new MainActivityPagerAdapter(getSupportFragmentManager());

        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        tabs.setViewPager(viewPager);
    }


}
