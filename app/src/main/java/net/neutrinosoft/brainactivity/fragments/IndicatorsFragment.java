package net.neutrinosoft.brainactivity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.neutrinosoft.brainactivity.R;

public class IndicatorsFragment extends Fragment implements View.OnClickListener {

    private Button t3;
    private Button t4;
    private Button o1;
    private Button o2;


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

        return view;
    }


    @Override
    public void onClick(View v) {
        t3.setEnabled(v.getId() == t3.getId());
        t4.setEnabled(v.getId() == t4.getId());
        o1.setEnabled(v.getId() == o1.getId());
        o2.setEnabled(v.getId() == o2.getId());
    }
}
