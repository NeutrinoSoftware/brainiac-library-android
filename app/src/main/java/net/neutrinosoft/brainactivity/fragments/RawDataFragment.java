package net.neutrinosoft.brainactivity.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.squareup.otto.Subscribe;

import net.neutrinosoft.brainactivity.R;
import net.neutrinosoft.brainactivity.common.BusProvider;
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
    private int index;

    List<Value> values = new ArrayList<>();

    List<String> xValuesList = new ArrayList<>();
    List<List<Entry>> entriesList = new ArrayList<>();
    private TimeZoom timeZoom = TimeZoom.Five;
    private ValueZoom valueZoom = ValueZoom.TwoHundred;

    private Handler handler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        for (int i = 0; i < 4; i++) {
            entriesList.add(new ArrayList<Entry>());
        }
        for (int i = 0; i < 2000; i++) {
            values.add(new Value());
            xValuesList.add("");
            for (int j = 0; j < 4; j++) {
                List<Entry> list = entriesList.get(j);
                list.add(new Entry(values.get(values.size() - 1).toFloatArray()[j], index));
            }
            index++;
        }
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
                    lineChart.setVisibleXRange(timeZoom.getZoomValue(), timeZoom.getZoomValue());
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
                    lineChart.setVisibleXRange(timeZoom.getZoomValue(), timeZoom.getZoomValue());
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
        initCharts();
        BusProvider.getBus().register(this);
    }

    private void initCharts() {

        for (int i = 0; i < charts.size(); i++) {
            final LineChart chart = charts.get(i);

            chart.setTouchEnabled(false);
            chart.setDoubleTapToZoomEnabled(false);


            List<Entry> entryValues = entriesList.get(i);
            LineDataSet lineDataSet = new LineDataSet(entryValues, "");
            lineDataSet.setDrawCircles(false);
            lineDataSet.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                    return "";
                }
            });
            lineDataSet.setColor(ContextCompat.getColor(getActivity(), R.color.raw_data_plot_color));
            LineData lineData = new LineData(xValuesList, lineDataSet);

            chart.setDescription("");
            chart.setData(lineData);
            chart.setGridBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            chart.setVisibleXRange(timeZoom.getZoomValue(), timeZoom.getZoomValue());

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setLabelCount(0, true);
            leftAxis.setValueFormatter(new YAxisValueFormatter() {
                @Override
                public String getFormattedValue(float value, YAxis yAxis) {
                    return "";
                }
            });
            //leftAxis.setStartAtZero(false);

            leftAxis.setAxisMinValue(-200000);
            leftAxis.setAxisMaxValue(200000);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setEnabled(false);

            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setLabelsToSkip(250);

            Legend legend = chart.getLegend();
            legend.setEnabled(false);
        }
    }


    @Override
    public void onDestroy() {

        super.onDestroy();
        BusProvider.getBus().unregister(this);
    }

    @Subscribe
    public void onDataReceive(Value value) {
        if (handler == null) {
            handler = new Handler();
        }
        if (value != null) {
            values.add(value);
            index++;

            if ((values.size() % 5) == 0) {
                for (int i = 0; i < charts.size(); i++) {

                    LineChart chart = charts.get(i);
                    List<Entry> entries = entriesList.get(i);
                    if (values.size() > 2000) {
                        values.remove(0);
                        entries.remove(0);
                    }

                    entries.add(new Entry(values.get(values.size() - 1).toFloatArray()[i], index));
                    chart.setVisibleXRange(timeZoom.getZoomValue(), timeZoom.getZoomValue());
                    chart.moveViewToX(index);

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
