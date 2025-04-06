package com.example.ssmsprojectapp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MetalsFragment extends Fragment {

    private LineChart metalsChart;



    public MetalsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_metals, container, false);

        metalsChart = view.findViewById(R.id.metals_chart);
        setupMetalsChart();

        // Load data (in real app, this would come from your database/API)
        loadMetalsData();

        return view;
    }

    private void setupMetalsChart() {
        metalsChart.getDescription().setEnabled(true);
        //metalsChart.getDescription().setText("Soil salinity Over Time");
        metalsChart.setTouchEnabled(true);
        metalsChart.setDragEnabled(true);
        metalsChart.setScaleEnabled(true);
        metalsChart.setPinchZoom(true);

        // X-axis setup
        XAxis xAxis = metalsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new DateAxisValueFormatter());

        // Y-axis setup
        YAxis leftAxis = metalsChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setGranularity(5f);
        leftAxis.setTextColor(Color.BLUE);
        leftAxis.setValueFormatter(new PercentValueFormatter());

        metalsChart.getAxisRight().setEnabled(false);
        metalsChart.getLegend().setEnabled(true);
    }

    private void loadMetalsData() {
        // Example data - replace with your actual data loading logic
        List<Entry> entries = new ArrayList<>();

        // Add sample data (x = timestamp, y = moisture %)
        entries.add(new Entry(System.currentTimeMillis() - 86400000*2, 45f)); // 2 days ago
        entries.add(new Entry(System.currentTimeMillis() - 86400000, 52f));   // 1 day ago
        entries.add(new Entry(System.currentTimeMillis(), 48f));               // now

        LineDataSet dataSet = new LineDataSet(entries, "Soil salinity (%)");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new PercentValueFormatter());

        LineData lineData = new LineData(dataSet);
        metalsChart.setData(lineData);
        metalsChart.invalidate(); // refresh
    }

    // Custom value formatters
    private class DateAxisValueFormatter extends ValueFormatter {
        private final SimpleDateFormat mFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return mFormat.format(new Date((long) value));
        }
    }

    private class PercentValueFormatter extends ValueFormatter {
        @Override
        public String getPointLabel(Entry entry) {
            return String.format(Locale.getDefault(), "%.1f%%", entry.getY());
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return String.format(Locale.getDefault(), "%.0f%%", value);
        }
    }
}