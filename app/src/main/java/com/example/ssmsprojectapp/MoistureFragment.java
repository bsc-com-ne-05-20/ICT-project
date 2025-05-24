package com.example.ssmsprojectapp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

public class MoistureFragment extends Fragment {

    private LineChart moistureChart;
    private List<Measurement> measurementList = new ArrayList<>();

    public MoistureFragment() {
        // Required empty public constructor
    }

    public  MoistureFragment(List<Measurement> measurements){
        this.measurementList = measurements;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_moisture, container, false);

        moistureChart = view.findViewById(R.id.moisture_chart);
        setupMoistureChart();

        // Load data (in real app, this would come from your database/API)
        loadMoistureData();

        return view;
    }

    private void setupMoistureChart() {
        moistureChart.getDescription().setEnabled(true);
        moistureChart.getDescription().setText("Soil Moisture Over Time");
        moistureChart.setTouchEnabled(true);
        moistureChart.setDragEnabled(true);
        moistureChart.setScaleEnabled(true);
        moistureChart.setPinchZoom(true);

        // X-axis setup
        XAxis xAxis = moistureChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new DateAxisValueFormatter());

        // Y-axis setup
        YAxis leftAxis = moistureChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(100f);
        leftAxis.setGranularity(5f);
        leftAxis.setTextColor(Color.BLUE);
        leftAxis.setValueFormatter(new PercentValueFormatter());

        moistureChart.getAxisRight().setEnabled(false);
        moistureChart.getLegend().setEnabled(true);
    }

    private void loadMoistureData() {

        List<Entry> entries = new ArrayList<>();
        //load the data from the measurements

        if (!measurementList.isEmpty()){
            for (Measurement measurement : measurementList) {
                // Convert timestamp to float (usually X axis)
                float xValue = (float) measurement.getTimestamp().getTime(); // Using getTime() for milliseconds

                // Get moisture value (Y axis)
                float yValue = (float) measurement.getMoisture();

                // Create and add entry
                entries.add(new Entry(xValue, yValue));
            }
        }
        else {
            Toast.makeText(getContext(), "Empty List", Toast.LENGTH_SHORT).show();
        }

        // Add sample data (x = timestamp, y = moisture %)
        //entries.add(new Entry(System.currentTimeMillis() - 86400000*2, 45f)); // 2 days ago
        //entries.add(new Entry(System.currentTimeMillis() - 86400000, 52f));   // 1 day ago
        //entries.add(new Entry(System.currentTimeMillis(), 48f));               // now

        LineDataSet dataSet = new LineDataSet(entries, "Soil Moisture (%)");
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.BLUE);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new PercentValueFormatter());

        LineData lineData = new LineData(dataSet);
        moistureChart.setData(lineData);
        moistureChart.invalidate(); // refresh
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