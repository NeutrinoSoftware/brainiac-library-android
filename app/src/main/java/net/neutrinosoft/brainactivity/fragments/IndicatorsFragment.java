package net.neutrinosoft.brainactivity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.neutrinosoft.brainactivity.R;

import butterknife.ButterKnife;
import butterknife.InjectViews;
import butterknife.OnClick;

public class IndicatorsFragment extends Fragment {

    @InjectViews({R.id.t3, R.id.o1, R.id.t4, R.id.o2})
    Button[] channels;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_indicators, container, false);
        ButterKnife.inject(this, view);
        return view;
    }


    @OnClick({R.id.t3, R.id.o1, R.id.t4, R.id.o2})
    public void onChannelClick(Button selectedButton) {
        for (Button button : channels) {
            button.setEnabled(true);
        }
        selectedButton.setEnabled(false);
    }


}
