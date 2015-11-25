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

public class RawDataFragment extends Fragment {


    private List<LineChart> charts = new ArrayList<>();
    private TextView timeZoomLabel;
    private TextView valueZoomLabel;
    private Button minusTimeZoom;
    private Button plusTimeZoom;
    private Button minusValueZoom;
    private Button plusValueZoom;


    List<Value> values = new ArrayList<>();

    List<String> xValuesList;
    List<List<Entry>> entriesList;
    private TimeZoom timeZoom = TimeZoom.Five;
    private ValueZoom valueZoom = ValueZoom.TwoHundred;

    private RawDataBroadcastReceiver rawDataBroadcastReceiver;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_raw_data, container, false);

        charts.add((LineChart) view.findViewById(R.id.chart0));
        charts.add((LineChart) view.findViewById(R.id.chart1));
        charts.add((LineChart) view.findViewById(R.id.chart2));
        charts.add((LineChart) view.findViewById(R.id.chart3));

        timeZoomLabel = (TextView) view.findViewById(R.id.timeZoomLabel);

        minusTimeZoom = (Button) view.findViewById(R.id.minusTimeZoom);
        minusTimeZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        plusTimeZoom = (Button) view.findViewById(R.id.plusTimeZoom);
        plusTimeZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });


        valueZoomLabel = (TextView) view.findViewById(R.id.valueZoomLabel);

        minusValueZoom = (Button) view.findViewById(R.id.minusValueZoom);
        minusValueZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        plusValueZoom = (Button) view.findViewById(R.id.plusValueZoom);
        plusValueZoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });


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

    private void initCharts() {

        for (int i = 0; i < charts.size(); i++) {
            final LineChart chart = charts.get(i);
            chart.setTouchEnabled(false);
            chart.setPinchZoom(false);
            chart.setDoubleTapToZoomEnabled(false);


            entriesList.add(new ArrayList<Entry>());
            List<Entry> entryValues = entriesList.get(i);
            LineDataSet lineDataSet = new LineDataSet(entryValues, "");
            lineDataSet.setDrawCircles(false);
            lineDataSet.setDrawCubic(true);
            lineDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "";
                }
            });
            lineDataSet.setColor(getResources().getColor(R.color.raw_data_plot_color));
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
        public synchronized void onReceive(Context context, Intent intent) {
            Value value = intent.getExtras().getParcelable(MainActivity.EXTRA_VALUES);

            if (value != null) {

                if (values.size() > 2048) {
                    xValuesList.clear();
                    values.clear();
                    for (List list : entriesList) {
                        list.clear();
                    }
                }

                xValuesList.add("");
                values.add(value);

                if ((values.size() % 250) == 0) {
                    for (int i = 0; i < charts.size(); i++) {

                        LineChart chart = charts.get(i);
                        List<Entry> entries = entriesList.get(i);
                        entries.clear();
                        for (int j = 0; j < values.size(); j++) {
                            entries.add(new Entry(values.get(j).toFloatArray()[i], j));
                        }
                        chart.setVisibleXRange(timeZoom.getZoomValue());
                        chart.moveViewToX(values.size() - timeZoom.getZoomValue());

                        chart.notifyDataSetChanged();
                        chart.invalidate();
                    }

                }
            }

            if (value != null) {
                Log.d("OrderNumber", String.valueOf(value.getHardwareOrderNumber()));
                xValuesList.add("");
            }
        }
    }
}
