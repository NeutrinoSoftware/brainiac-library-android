package net.neutrinosoft.brainactivity.activities;

import android.support.v7.app.AppCompatActivity;
import butterknife.ButterKnife;

public class BaseActivity  extends AppCompatActivity {

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.inject(this);
    }

}
