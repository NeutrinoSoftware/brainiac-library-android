package net.neutrinosoft.brainactivity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.neutrinosoft.brainactivity.R;
import net.neutrinosoft.brainiac.BrainiacManager;

public class IndicatorsFragment extends Fragment implements View.OnClickListener {

    private Button t3;
    private Button t4;
    private Button o1;
    private Button o2;
    private View green;
    private View yellow;
    private View red1;
    private View red2;
    private int channel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        channel = 0;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_indicators, container, false);

        t3 = (Button) view.findViewById(R.id.t3);
        t3.setOnClickListener(this);

        t4 = (Button) view.findViewById(R.id.t4);
        t4.setOnClickListener(this);

        o1 = (Button) view.findViewById(R.id.o1);
        o1.setOnClickListener(this);

        o2 = (Button) view.findViewById(R.id.o2);
        o2.setOnClickListener(this);

        green = view.findViewById(R.id.green);
        yellow = view.findViewById(R.id.yellow);
        red1 = view.findViewById(R.id.red1);
        red2 = view.findViewById(R.id.red2);

        return view;
    }


    @Override
    public void onClick(View v) {
        t3.setEnabled(v.getId() != R.id.t3);
        o1.setEnabled(v.getId() != R.id.o1);
        t4.setEnabled(v.getId() != R.id.t4);
        o2.setEnabled(v.getId() != R.id.o2);

        switch (v.getId()) {
            case R.id.t3: {
                channel = 0;
                break;
            }
            case R.id.o1: {
                channel = 1;

                break;
            }
            case R.id.t4: {
                channel = 2;

                break;
            }
            case R.id.o2: {
                channel = 3;

                break;
            }
        }

        updateIndicators();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateIndicators();
    }

    private void updateIndicators() {
        green.setBackgroundResource(R.color.grey);
        yellow.setBackgroundResource(R.color.grey);
        red1.setBackgroundResource(R.color.grey);
        red2.setBackgroundResource(R.color.grey);

        BrainiacManager brainiacManager = BrainiacManager.getBrainiacManager(getActivity());
        if (brainiacManager.isConnected()||brainiacManager.isInTestMode()) {
            if (brainiacManager.processGreenChannel(channel)) {
                green.setBackgroundResource(R.color.green);
            }
            if (brainiacManager.processYellowForChannel(channel)) {
                yellow.setBackgroundResource(R.color.yellow);
            }
            if (brainiacManager.processRed1ForChannel(channel)) {
                red1.setBackgroundResource(R.color.red1);
            }
            if (brainiacManager.processRed2ForChannel(channel)) {
                red2.setBackgroundResource(R.color.red2);
            }
        }


    }
}
