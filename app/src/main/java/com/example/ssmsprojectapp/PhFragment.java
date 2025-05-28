package com.example.ssmsprojectapp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ssmsprojectapp.databasehelpers.MeasurementDbHelper;
import com.example.ssmsprojectapp.datamodels.Measurement;
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

public class PhFragment extends Fragment {

    private LineChart phChart;
    private List<Measurement> measurementList;

    private MeasurementDbHelper measurementDbHelper;


    public PhFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ph, container, false);

        measurementDbHelper = new MeasurementDbHelper(getContext());

        measurementList = new ArrayList<>();
        getMeasurements();

        phChart = view.findViewById(R.id.ph_chart);
        setupPhChart();
        loadTPhData();
        return view;
    }

    private void loadTPhData() {
        List<Entry> entries = new ArrayList<>();

        //load the data from the measurements
        for (Measurement measurement: measurementList) {
            entries.add(new Entry( measurement.getTimestamp().getDate(), (float)measurement.getTemperature()));
        }
        // Add sample data (x = timestamp, y = moisture %)
        //entries.add(new Entry(System.currentTimeMillis() - 86400000*2, 45f)); // 2 days ago
        //entries.add(new Entry(System.currentTimeMillis() - 86400000, 52f));   // 1 day ago
        //entries.add(new Entry(System.currentTimeMillis(), 48f));               // now

        LineDataSet dataSet = new LineDataSet(entries, "Soil Ph (℃)");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new PhFragment.PercentValueFormatter());

        LineData lineData = new LineData(dataSet);
        phChart.setData(lineData);
        phChart.invalidate(); // refresh

    }

    private void setupPhChart() {
        phChart.getDescription().setEnabled(true);
        phChart.getDescription().setText("Soil temperature");
        phChart.setTouchEnabled(true);
        phChart.setDragEnabled(true);
        phChart.setScaleEnabled(true);
        phChart.setPinchZoom(true);

        // X-axis setup
        XAxis xAxis = phChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new PhFragment.DateAxisValueFormatter());

        // Y-axis setup
        YAxis leftAxis = phChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setGranularity(5f);
        leftAxis.setTextColor(Color.BLUE);
        leftAxis.setValueFormatter(new PhFragment.PercentValueFormatter());

        phChart.getAxisRight().setEnabled(false);
        phChart.getLegend().setEnabled(true);
    }

    private void getMeasurements() {
        measurementList.clear();
        measurementList = measurementDbHelper.getAllMeasurements();
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