package net.neutrinosoft.brainactivity.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;
import com.squareup.otto.Subscribe;

import net.neutrinosoft.brainactivity.R;
import net.neutrinosoft.brainactivity.common.BusProvider;
import net.neutrinosoft.brainiac.FftValue;

import java.util.ArrayList;
import java.util.List;

public class SpectrumFragment extends Fragment {

    private List<LineChart> charts = new ArrayList<>();
    private List<String> xValues = new ArrayList<>();
    private List<List<Entry>> blueEntries = new ArrayList<>();
    private List<List<Entry>> yellowEntries = new ArrayList<>();
    private List<List<Entry>> grayEntries = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_spectrum, container, false);

        charts.add((LineChart) view.findViewById(R.id.chart0));
        charts.add((LineChart) view.findViewById(R.id.chart1));
        charts.add((LineChart) view.findViewById(R.id.chart2));
        charts.add((LineChart) view.findViewById(R.id.chart3));

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        for (int i = 0; i < charts.size(); i++) {
            LineChart chart = charts.get(i);
            chart.setPinchZoom(false);
            chart.setTouchEnabled(false);
            chart.setDoubleTapToZoomEnabled(false);

            List<LineDataSet> lineDataSets = new ArrayList<>();

            blueEntries.add(new ArrayList<Entry>());
            LineDataSet blue = new LineDataSet(blueEntries.get(i), "");
            blue.setDrawCircles(false);
            blue.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "";
                }
            });
            blue.setColor(Color.BLUE);
            lineDataSets.add(blue);

            grayEntries.add(new ArrayList<Entry>());
            LineDataSet gray = new LineDataSet(grayEntries.get(i), "");
            gray.setDrawCircles(false);
            gray.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "";
                }
            });
            gray.setColor(Color.GRAY);
            lineDataSets.add(gray);


            yellowEntries.add(new ArrayList<Entry>());
            LineDataSet yellow = new LineDataSet(yellowEntries.get(i), "");
            yellow.setDrawCircles(false);
            yellow.setValueFormatter(new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    return "";
                }
            });
            yellow.setColor(Color.YELLOW);
            lineDataSets.add(yellow);

            LineData lineData = new LineData(xValues, lineDataSets);


            chart.setDescription("");
            chart.setData(lineData);
            chart.setGridBackgroundColor(getResources().getColor(R.color.white));

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setLabelCount(5);
            leftAxis.setStartAtZero(false);
            leftAxis.setAxisMinValue(0);
            leftAxis.setAxisMaxValue(25);

            YAxis rightAxis = chart.getAxisRight();
            rightAxis.setEnabled(false);

            XAxis xAxis = chart.getXAxis();
            xAxis.setEnabled(false);

            Legend legend = chart.getLegend();
            legend.setEnabled(false);
        }
        BusProvider.getBus().register(this);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getBus().unregister(this);

    }

    @Subscribe
    public void onDataReceive(FftValue[] fftValues) {

        xValues.add("");

        for (int i = 0; i < charts.size(); i++) {

            List<Entry> blues = blueEntries.get(i);
            List<Entry> grays = grayEntries.get(i);
            List<Entry> yellows = yellowEntries.get(i);

            int xCount = xValues.size() - 1;
            blues.add(new Entry(fftValues[i].getData1(), xCount));
            grays.add(new Entry(fftValues[i].getData2(), xCount));
            yellows.add(new Entry(fftValues[i].getData3(), xCount));


            LineChart chart = charts.get(i);
            chart.setVisibleXRange(20);
            chart.moveViewToX(xCount - 1);

            chart.notifyDataSetChanged();
            chart.invalidate();
        }


    }
}