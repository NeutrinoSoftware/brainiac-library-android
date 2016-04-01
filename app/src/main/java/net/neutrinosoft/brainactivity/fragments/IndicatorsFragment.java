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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
        final RelativeLayout rlIndicators = (RelativeLayout) getActivity().findViewById(R.id.rlIndicators);
        final TextView tvSleep = (TextView) getActivity().findViewById(R.id.tvSleep);
        final TextView tvDeepRelaxation = (TextView) getActivity().findViewById(R.id.tvDeepRelaxation);
        final TextView tvRelaxation = (TextView) getActivity().findViewById(R.id.tvRelaxation);
        final TextView tvNormalActivation = (TextView) getActivity().findViewById(R.id.tvNormalActivation);
        final TextView tvExcitement = (TextView) getActivity().findViewById(R.id.tvExcitement);
        final TextView tvDeepExcitement = (TextView) getActivity().findViewById(R.id.tvDeepExcitement);
        final int widthLeft = (tvSleep.getWidth() + tvDeepRelaxation.getWidth() + tvRelaxation.getWidth());
        final int widthRight = (tvNormalActivation.getWidth() + tvExcitement.getWidth() + tvDeepExcitement.getWidth());
        brainiacManager.setOnIndicatorsStateChangedCallback(new OnIndicatorsStateChangedCallback() {
            @Override
            public void onIndicatorsStateChanged(IndicatorsState indicatorsState) {
                float percent = indicatorsState.getActivities().getPercent();
                ManagerActivityZone zone = indicatorsState.getActivities().getActivityZone();
                boolean direction = false;

                if (rlIndicators.getChildCount() > 1) {
                    rlIndicators.removeViewAt(0);
                }

                if (zone == ManagerActivityZone.Relaxation) {
                    direction = false;
                }
                if (zone == ManagerActivityZone.HighRelaxation) {
                    direction = false;
                }
                if (zone == ManagerActivityZone.Dream) {
                    direction = false;
                }
                if (zone == ManagerActivityZone.NormalActivity) {
                    direction = true;
                }
                if (zone == ManagerActivityZone.Agitation) {
                    direction = true;
                }
                if (zone == ManagerActivityZone.HighAgitation) {
                    direction = true;
                }

                View view = new View(getActivity());
                if (direction) {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (widthRight * percent), rlIndicators.getHeight());
                    view.setX(tvNormalActivation.getX());
                    view.setY(tvNormalActivation.getY());
                    view.setLayoutParams(params);
                    view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green));
                    rlIndicators.addView(view, 0);
                } else {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams((int) (widthLeft * percent), rlIndicators.getHeight());
                    view.setX(tvNormalActivation.getX() - widthLeft * percent);
                    view.setY(tvNormalActivation.getY());
                    view.setLayoutParams(params);
                    view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green));
                    rlIndicators.addView(view, 0);
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

}