package net.neutrinosoft.brainactivity.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;

import net.neutrinosoft.brainactivity.R;
import net.neutrinosoft.brainactivity.activities.MainActivity;
import net.neutrinosoft.brainactivity.activities.StartUpActivity;
import net.neutrinosoft.brainactivity.common.TimeZoom;
import net.neutrinosoft.brainactivity.common.ValueZoom;
import net.neutrinosoft.brainiac.Value;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.InjectViews;
import butterknife.OnClick;

public class RawDataFragment extends Fragment {


    @InjectViews({R.id.chart0, R.id.chart1, R.id.chart2, R.id.chart3})
    List<LineChart> charts;
    @InjectView(R.id.timeZoomLabel)
    TextView timeZoomLabel;
    @InjectView(R.id.minusTimeZoom)
    Button minusTimeZoom;
    @InjectView(R.id.plusTimeZoom)
    Button plusTimeZoom;
    @InjectView(R.id.minusValueZoom)
    Button minusValueZoom;
    @InjectView(R.id.plusValueZoom)
    Button plusValueZoom;
    @InjectView(R.id.valueZoomLabel)
    TextView valueZoomLabel;


    List<String> xValuesList;
    List<List<Entry>> entriesList;
    private TimeZoom timeZoom = TimeZoom.Five;
    private ValueZoom valueZoom = ValueZoom.TwoHundred;

    private RawDataBroadcastReceiver rawDataBroadcastReceiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_raw_data, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        xValuesList = new ArrayList<>();
        entriesList = new ArrayList<>();

        rawDataBroadcastReceiver = new RawDataBroadcastReceiver();

        initCharts();
        getActivity().registerReceiver(rawDataBroadcastReceiver, new IntentFilter(StartUpActivity.ACTION_NEW_VALUE));
    }

    @OnClick(R.id.minusTimeZoom)
    public void onMinusTimeZoomClick() {
        switch (timeZoom) {
            case Two: {
                timeZoom = TimeZoom.One;
                minusTimeZoom.setEnabled(false);

                break;
            }
            case Four: {
                timeZoom = TimeZoom.Two;
                break;
            }
            case Five: {
                timeZoom = TimeZoom.Four;
                plusTimeZoom.setEnabled(true);
                break;
            }
        }
        timeZoomLabel.setText(timeZoom.getLabel());
        for (LineChart lineChart : charts) {
            lineChart.setVisibleXRange(timeZoom.getZoomValue());
            lineChart.invalidate();

        }
    }

    @OnClick(R.id.plusTimeZoom)
    public void onPlusTimeZoomClick() {
        switch (timeZoom) {
            case One: {
                timeZoom = TimeZoom.Two;
                minusTimeZoom.setEnabled(true);
                break;
            }
            case Two: {
                timeZoom = TimeZoom.Four;
                break;
            }
            case Four: {
                timeZoom = TimeZoom.Five;
                plusTimeZoom.setEnabled(false);
                break;
            }
        }
        timeZoomLabel.setText(timeZoom.getLabel());
        for (LineChart lineChart : charts) {
            lineChart.setVisibleXRange(timeZoom.getZoomValue());
            lineChart.invalidate();
        }
    }

    @OnClick(R.id.minusValueZoom)
    public void onMinusValueZoomClick() {
        switch (valueZoom) {
            case Forty: {
                valueZoom = ValueZoom.Twenty;
                minusValueZoom.setEnabled(false);
                break;
            }
            case OneHundred: {
                valueZoom = ValueZoom.Forty;
                break;
            }
            case TwoHundred: {
                valueZoom = ValueZoom.OneHundred;
                break;
            }
            case FourHundred: {
                valueZoom = ValueZoom.TwoHundred;
                plusValueZoom.setEnabled(true);
                break;

            }
        }
        valueZoomLabel.setText(valueZoom.getLabel());
        for (LineChart lineChart : charts) {
            lineChart.getAxisLeft().setAxisMinValue(-valueZoom.getZoomValue());
            lineChart.getAxisLeft().setAxisMaxValue(valueZoom.getZoomValue());
        }
    }

    @OnClick(R.id.plusValueZoom)
    public void onPlusValueZoomClick() {
        switch (valueZoom) {
            case Twenty: {
                valueZoom = ValueZoom.Forty;
                minusValueZoom.setEnabled(true);
                break;
            }
            case Forty: {
                valueZoom = ValueZoom.OneHundred;
                break;
            }
            case OneHundred: {
                valueZoom = ValueZoom.TwoHundred;
                break;
            }
            case TwoHundred: {
                valueZoom = ValueZoom.FourHundred;
                plusValueZoom.setEnabled(false);
                break;

            }
        }
        valueZoomLabel.setText(valueZoom.getLabel());
        for (LineChart lineChart : charts) {
            lineChart.getAxisLeft().setAxisMinValue(-valueZoom.getZoomValue());
            lineChart.getAxisLeft().setAxisMaxValue(valueZoom.getZoomValue());
        }
    }


    private void initCharts() {
        int[] colors = new int[]{R.color.green, R.color.yellow, R.color.red1, R.color.red2};

        for (int i = 0; i < charts.size(); i++) {
            final LineChart chart = charts.get(i);
            chart.setPinchZoom(false);
            chart.setDoubleTapToZoomEnabled(false);


            entriesList.add(new ArrayList<Entry>());
            LineDataSet lineDataSet = new LineDataSet(entriesList.get(i), "");
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawCubic(true);
            lineDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "";
                }
            });
            lineDataSet.setColor(getResources().getColor(colors[i]));
            LineData lineData = new LineData(xValuesList, lineDataSet);

            chart.setDescription("");
            chart.setData(lineData);
            chart.setGridBackgroundColor(getResources().getColor(R.color.white));
            chart.setVisibleXRange(timeZoom.getZoomValue());

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setLabelCount(0);
            leftAxis.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "";
                }
            });
            leftAxis.setStartAtZero(false);

            leftAxis.setAxisMinValue(-200000);
            leftAxis.setAxisMaxValue(200000);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setEnabled(false);

            XAxis xAxis = chart.getXAxis();
            xAxis.setEnabled(false);

            Legend legend = chart.getLegend();
            legend.setEnabled(false);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(rawDataBroadcastReceiver);
    }

    class RawDataBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Value value = intent.getExtras().getParcelable(MainActivity.EXTRA_VALUES);
            if (value != null) {
                xValuesList.add("");
                for (int i = 0; i < charts.size(); i++) {
                    LineChart chart = charts.get(i);
                    List<Entry> entries = entriesList.get(i);


                    int xCount = xValuesList.size() - 1;
                    entries.add(new Entry(value.toFloatArray()[i], xCount));

                    if ((xValuesList.size() % 50) == 0) {
                        chart.setVisibleXRange(timeZoom.getZoomValue());
                        chart.moveViewToX(xCount - 1);

                        chart.notifyDataSetChanged();
                        chart.invalidate();
                    }
                }
            }
        }
    }
}
