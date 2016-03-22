package net.neutrinosoft.brainactivity.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import net.neutrinosoft.brainactivity.R;
import net.neutrinosoft.brainiac.BrainiacManager;
import net.neutrinosoft.brainiac.IndicatorsState;
import net.neutrinosoft.brainiac.ManagerActivityZone;
import net.neutrinosoft.brainiac.callback.OnIndicatorsStateChangedCallback;

public class IndicatorsFragment extends Fragment implements OnClickListener {

    private Button btnStart;
    private View green;
    private View yellow;
    private View red1;
    private View red2;
    private BrainiacManager brainiacManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        brainiacManager = BrainiacManager.getBrainiacManager(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_indicators, container, false);

        btnStart = (Button) view.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);

        green = view.findViewById(R.id.green);
        yellow = view.findViewById(R.id.yellow);
        red1 = view.findViewById(R.id.red1);
        red2 = view.findViewById(R.id.red2);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        brainiacManager.disableIndicators();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnStart) {
            if (brainiacManager.enableIndicators()) {
                btnStart.setEnabled(false);
                startIndicators();
            }
        }
    }

    private void startIndicators() {
        final RelativeLayout rlRelaxation = (RelativeLayout) getActivity().findViewById(R.id.rlRelaxation);
        final RelativeLayout rlDeepRelaxation = (RelativeLayout) getActivity().findViewById(R.id.rlDeepRelaxation);
        final RelativeLayout rlSleep = (RelativeLayout) getActivity().findViewById(R.id.rlSleep);
        final RelativeLayout rlNormalActivation = (RelativeLayout) getActivity().findViewById(R.id.rlNormalActivation);
        final RelativeLayout rlExcitement = (RelativeLayout) getActivity().findViewById(R.id.rlExcitement);
        final RelativeLayout rlDeepExcitement = (RelativeLayout) getActivity().findViewById(R.id.rlDeepExcitement);
        brainiacManager.setOnIndicatorsStateChangedCallback(new OnIndicatorsStateChangedCallback() {
            @Override
            public void onIndicatorsStateChanged(IndicatorsState indicatorsState) {
                float percent = indicatorsState.getActivities().getPercent();
                ManagerActivityZone zone = indicatorsState.getActivities().getActivityZone();

                clearRL(rlRelaxation);
                clearRL(rlDeepRelaxation);
                clearRL(rlSleep);
                clearRL(rlNormalActivation);
                clearRL(rlExcitement);
                clearRL(rlDeepExcitement);

                if (zone == ManagerActivityZone.Relaxation) {
                    paintRL(rlRelaxation, percent);
                }
                if (zone == ManagerActivityZone.HighRelaxation) {
                    paintRL(rlDeepRelaxation, percent);
                }
                if (zone == ManagerActivityZone.Dream) {
                    paintRL(rlSleep, percent);
                }
                if (zone == ManagerActivityZone.NormalActivity) {
                    paintRL(rlNormalActivation, percent);
                }
                if (zone == ManagerActivityZone.Agitation) {
                    paintRL(rlExcitement, percent);
                }
                if (zone == ManagerActivityZone.HighAgitation) {
                    paintRL(rlDeepExcitement, percent);
                }

                green.setBackgroundResource(R.color.grey);
                yellow.setBackgroundResource(R.color.grey);
                red1.setBackgroundResource(R.color.grey);
                red2.setBackgroundResource(R.color.grey);
                for (String color :
                        indicatorsState.getColors()) {
                    switch (color) {
                        case "green":
                            green.setBackgroundResource(R.color.green);
                            break;
                        case "yellow":
                            yellow.setBackgroundResource(R.color.yellow);
                            break;
                        case "red1":
                            red1.setBackgroundResource(R.color.red1);
                            break;
                        case "red2":
                            red2.setBackgroundResource(R.color.red2);
                            break;
                    }
                }
            }
        });
    }

    private void paintRL(RelativeLayout rl, double percent) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams((int) (rl.getWidth() * percent), rl.getHeight());
        View view = new View(getActivity());
        view.setLayoutParams(params);
        view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green));
        rl.addView(view, 0);
    }

    private void clearRL(RelativeLayout rl) {
        if (rl.getChildCount() > 1) {
            rl.removeViewAt(0);
        }
    }

}