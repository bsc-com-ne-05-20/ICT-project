package com.example.ssmsprojectapp;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ssmsprojectapp.datamodels.Measurement;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class NutrientsFragment extends Fragment {

    private CombinedChart nutrientsChart;
    private List<Measurement> measurementList =  new ArrayList<>();

    public NutrientsFragment() {
        // Required empty public constructor
    }

    public NutrientsFragment(List<Measurement> measurements) {
         this.measurementList = measurements;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_nutrients, container, false);
        nutrientsChart = view.findViewById(R.id.nutrients_chart);
        setupNutrientsChart();
        loadNutrientsData();
        return view;
    }

    private void setupNutrientsChart() {
        nutrientsChart.getDescription().setText("Soil Nutrients Over Time");
        nutrientsChart.setTouchEnabled(true);
        nutrientsChart.setDragEnabled(true);
        nutrientsChart.setScaleEnabled(true);
        nutrientsChart.setPinchZoom(true);

        // X-axis setup
        XAxis xAxis = nutrientsChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new DateAxisValueFormatter());

        // Y-axis setup
        YAxis leftAxis = nutrientsChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(5f);
        leftAxis.setTextColor(Color.BLUE);

        nutrientsChart.getAxisRight().setEnabled(false);
        nutrientsChart.getLegend().setEnabled(true);
    }

    private void loadNutrientsData() {
        CombinedData data = new CombinedData();

        // Line data for Nitrogen
        List<Entry> nitrogenEntries = new ArrayList<>();

        //load the data from the measurements
        for (Measurement measurement: measurementList) {
            nitrogenEntries.add(new Entry( measurement.getTimestamp().getDate(), (float)measurement.getNitrogen()));
        }

        //nitrogenEntries.add(new Entry(System.currentTimeMillis() - 86400000*2, 12f));
        //nitrogenEntries.add(new Entry(System.currentTimeMillis() - 86400000, 15f));
        //nitrogenEntries.add(new Entry(System.currentTimeMillis(), 14f));

        LineDataSet nitrogenSet = new LineDataSet(nitrogenEntries, "Nitrogen (ppm)");
        nitrogenSet.setColor(Color.parseColor("#4CAF50"));
        nitrogenSet.setLineWidth(2f);
        nitrogenSet.setCircleColor(Color.parseColor("#4CAF50"));
        nitrogenSet.setCircleRadius(4f);

        // Line data for Phosphorus
        List<Entry> phosphorusEntries = new ArrayList<>();
        //load the data from the measurements
        for (Measurement measurement: measurementList) {
            phosphorusEntries.add(new Entry( measurement.getTimestamp().getDate(), (float)measurement.getPhosphorus()));
        }

        //phosphorusEntries.add(new Entry(System.currentTimeMillis() - 86400000*2, 8f));
        //phosphorusEntries.add(new Entry(System.currentTimeMillis() - 86400000, 10f));
        //phosphorusEntries.add(new Entry(System.currentTimeMillis(), 9f));

        LineDataSet phosphorusSet = new LineDataSet(phosphorusEntries, "Phosphorus (ppm)");
        phosphorusSet.setColor(Color.parseColor("#FF9800"));
        phosphorusSet.setLineWidth(2f);
        phosphorusSet.setCircleColor(Color.parseColor("#FF9800"));
        phosphorusSet.setCircleRadius(4f);

        // Line data for Potassium
        List<Entry> potassiumEntries = new ArrayList<>();
        //load the data from the measurements
        for (Measurement measurement: measurementList) {
            potassiumEntries.add(new Entry( measurement.getTimestamp().getDate(), (float)measurement.getPotassium()));
        }

        //potassiumEntries.add(new Entry(System.currentTimeMillis() - 86400000*2, 25f));
        //potassiumEntries.add(new Entry(System.currentTimeMillis() - 86400000, 28f));
        //potassiumEntries.add(new Entry(System.currentTimeMillis(), 26f));

        LineDataSet potassiumSet = new LineDataSet(potassiumEntries, "Potassium (ppm)");
        potassiumSet.setColor(Color.parseColor("#2196F3"));
        potassiumSet.setLineWidth(2f);
        potassiumSet.setCircleColor(Color.parseColor("#2196F3"));
        potassiumSet.setCircleRadius(4f);

        LineData lineData = new LineData();
        lineData.addDataSet(nitrogenSet);
        lineData.addDataSet(phosphorusSet);
        lineData.addDataSet(potassiumSet);

        data.setData(lineData);
        nutrientsChart.setData(data);
        nutrientsChart.invalidate();
    }

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