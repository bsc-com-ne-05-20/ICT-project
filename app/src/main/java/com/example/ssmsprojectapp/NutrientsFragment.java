package com.example.ssmsprojectapp;

import static android.opengl.ETC1.getHeight;
import static android.opengl.ETC1.getWidth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ssmsprojectapp.databasehelpers.MeasurementDbHelper;
import com.example.ssmsprojectapp.datamodels.Measurement;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.renderer.LineChartRenderer;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NutrientsFragment extends Fragment {

    private CombinedChart nutrientsChart;
    private List<Measurement> measurementList;
    private MeasurementDbHelper measurementDbHelper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long minTimestamp = Long.MAX_VALUE;

    public NutrientsFragment() {
        // Required empty public constructor
    }

    public NutrientsFragment(List<Measurement> measurements) {
        this.measurementList = measurements;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrients, container, false);

        // Initialize swipe refresh
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        nutrientsChart = view.findViewById(R.id.nutrients_chart);
        measurementDbHelper = new MeasurementDbHelper(getContext());

        setupNutrientsChart();
        setupEmptyChartState();
        loadInitialData();

        return view;
    }

    private void setupEmptyChartState() {
        nutrientsChart.clear();
        nutrientsChart.setNoDataText("Loading nutrients data...");
        nutrientsChart.setNoDataTextColor(Color.GRAY);
    }

    private void loadInitialData() {
        swipeRefreshLayout.setRefreshing(true);
        new Thread(() -> {
            measurementList = measurementDbHelper.getAllMeasurements();
            requireActivity().runOnUiThread(() -> {
                updateChartWithData();
                swipeRefreshLayout.setRefreshing(false);
            });
        }).start();
    }

    private void refreshData() {
        new Thread(() -> {
            measurementList = measurementDbHelper.getAllMeasurements();
            requireActivity().runOnUiThread(() -> {
                updateChartWithData();
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Data refreshed", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void setupNutrientsChart() {
        try {
            nutrientsChart.clear();
            nutrientsChart.getDescription().setEnabled(true);
            nutrientsChart.getDescription().setText("Soil Nutrients Over Time");
            nutrientsChart.setBackgroundColor(Color.WHITE);

            // Legend customization
            Legend legend = nutrientsChart.getLegend();
            legend.setForm(Legend.LegendForm.LINE);
            legend.setTextSize(12f);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);

            // X-axis advanced setup
            XAxis xAxis = nutrientsChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setValueFormatter(new DateAxisValueFormatter());
            xAxis.setAxisLineColor(Color.BLACK);
            xAxis.setGridColor(Color.LTGRAY);
            xAxis.setAxisMinimum(0f);
            xAxis.setLabelCount(7, true);

            // Left Y-axis
            YAxis leftAxis = nutrientsChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);
            leftAxis.setGranularity(5f);
            leftAxis.setTextColor(Color.BLUE);
            leftAxis.setAxisLineColor(Color.BLACK);
            leftAxis.setGridColor(Color.LTGRAY);

            // Right Y-axis disabled
            nutrientsChart.getAxisRight().setEnabled(false);

            // Add optimal range markers
            addOptimalRangeMarkers();

            // Setup interactions
            setupChartInteractions();

            // Performance optimizations
            optimizeChartPerformance();

        } catch (Exception e) {
            Log.e("ChartSetup", "Error configuring chart", e);
        }
    }

    private void addOptimalRangeMarkers() {
        YAxis leftAxis = nutrientsChart.getAxisLeft();

        // Nitrogen optimal range (15-30 ppm)
        LimitLine nitrogenLower = new LimitLine(15f, "N Min");
        nitrogenLower.setLineColor(Color.parseColor("#4CAF50"));
        nitrogenLower.setLineWidth(1f);
        nitrogenLower.enableDashedLine(10f, 10f, 0f);

        LimitLine nitrogenUpper = new LimitLine(30f, "N Max");
        nitrogenUpper.setLineColor(Color.parseColor("#4CAF50"));
        nitrogenUpper.setLineWidth(1f);
        nitrogenUpper.enableDashedLine(10f, 10f, 0f);

        // Phosphorus optimal range (10-20 ppm)
        LimitLine phosphorusLower = new LimitLine(10f, "P Min");
        phosphorusLower.setLineColor(Color.parseColor("#FF9800"));
        phosphorusLower.setLineWidth(1f);
        phosphorusLower.enableDashedLine(10f, 10f, 0f);

        LimitLine phosphorusUpper = new LimitLine(20f, "P Max");
        phosphorusUpper.setLineColor(Color.parseColor("#FF9800"));
        phosphorusUpper.setLineWidth(1f);
        phosphorusUpper.enableDashedLine(10f, 10f, 0f);

        // Potassium optimal range (20-40 ppm)
        LimitLine potassiumLower = new LimitLine(20f, "K Min");
        potassiumLower.setLineColor(Color.parseColor("#2196F3"));
        potassiumLower.setLineWidth(1f);
        potassiumLower.enableDashedLine(10f, 10f, 0f);

        LimitLine potassiumUpper = new LimitLine(40f, "K Max");
        potassiumUpper.setLineColor(Color.parseColor("#2196F3"));
        potassiumUpper.setLineWidth(1f);
        potassiumUpper.enableDashedLine(10f, 10f, 0f);

        leftAxis.addLimitLine(nitrogenLower);
        leftAxis.addLimitLine(nitrogenUpper);
        leftAxis.addLimitLine(phosphorusLower);
        leftAxis.addLimitLine(phosphorusUpper);
        leftAxis.addLimitLine(potassiumLower);
        leftAxis.addLimitLine(potassiumUpper);
    }

    private void updateChartWithData() {
        if (measurementList == null || measurementList.isEmpty()) {
            nutrientsChart.setNoDataText("No nutrients data available");
            nutrientsChart.invalidate();
            return;
        }

        // Clear any previous data
        nutrientsChart.clear();

        // Find min and max timestamps
        minTimestamp = Long.MAX_VALUE;
        long maxTimestamp = Long.MIN_VALUE;
        for (Measurement m : measurementList) {
            minTimestamp = Math.min(minTimestamp, m.getTimestamp().getTime());
            maxTimestamp = Math.max(maxTimestamp, m.getTimestamp().getTime());
        }

        // Create entries with validated data
        List<Entry> nitrogenEntries = new ArrayList<>();
        List<Entry> phosphorusEntries = new ArrayList<>();
        List<Entry> potassiumEntries = new ArrayList<>();

        for (Measurement m : measurementList) {
            float hoursSinceFirst = (m.getTimestamp().getTime() - minTimestamp) / (1000f * 60f * 60f);

            if (!Double.isNaN(m.getNitrogen()) && !Double.isInfinite(m.getNitrogen())) {
                nitrogenEntries.add(new Entry(hoursSinceFirst, (float)m.getNitrogen()));
            }
            if (!Double.isNaN(m.getPhosphorus()) && !Double.isInfinite(m.getPhosphorus())) {
                phosphorusEntries.add(new Entry(hoursSinceFirst, (float)m.getPhosphorus()));
            }
            if (!Double.isNaN(m.getPotassium()) && !Double.isInfinite(m.getPotassium())) {
                potassiumEntries.add(new Entry(hoursSinceFirst, (float)m.getPotassium()));
            }
        }

        // Sort entries by time to prevent drawing issues
        Collections.sort(nitrogenEntries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));
        Collections.sort(phosphorusEntries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));
        Collections.sort(potassiumEntries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));

        // Create combined data
        CombinedData data = new CombinedData();
        LineData lineData = new LineData();

        if (!nitrogenEntries.isEmpty()) {
            LineDataSet nitrogenSet = createDataSet(nitrogenEntries, "Nitrogen (ppm)", Color.parseColor("#4CAF50"));
            lineData.addDataSet(nitrogenSet);
        }

        if (!phosphorusEntries.isEmpty()) {
            LineDataSet phosphorusSet = createDataSet(phosphorusEntries, "Phosphorus (ppm)", Color.parseColor("#FF9800"));
            lineData.addDataSet(phosphorusSet);
        }

        if (!potassiumEntries.isEmpty()) {
            LineDataSet potassiumSet = createDataSet(potassiumEntries, "Potassium (ppm)", Color.parseColor("#2196F3"));
            lineData.addDataSet(potassiumSet);
        }

        data.setData(lineData);
        nutrientsChart.setData(data);

        // Configure viewport safely
        try {
            float totalHours = (maxTimestamp - minTimestamp) / (1000f * 60f * 60f);
            float visibleRange = Math.min(totalHours, 7 * 24f); // Show max 7 days

            nutrientsChart.setVisibleXRangeMaximum(visibleRange);
            nutrientsChart.moveViewToX(totalHours);
            nutrientsChart.fitScreen();
        } catch (Exception e) {
            Log.e("ChartError", "Viewport configuration failed", e);
        }

        nutrientsChart.invalidate();
    }

    private LineDataSet createDataSet(List<Entry> entries, String label, int color) {
        LineDataSet dataSet = new LineDataSet(entries, label);
        dataSet.setColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(3f);
        dataSet.setCircleHoleRadius(1.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        dataSet.setHighLightColor(Color.RED);
        dataSet.setHighlightLineWidth(1f);
        dataSet.setDrawHorizontalHighlightIndicator(false);
        return dataSet;
    }
    private void styleDataSet(LineDataSet dataSet, int color) {
        dataSet.setColor(color);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(color);
        dataSet.setCircleRadius(3f);
        dataSet.setCircleHoleRadius(1.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setDrawValues(false);
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Highlighting
        dataSet.setHighLightColor(Color.RED);
        dataSet.setHighlightLineWidth(1f);
        dataSet.setDrawHorizontalHighlightIndicator(false);
    }

    private void configureViewport(long minTimestamp, long maxTimestamp) {
        if (minTimestamp >= maxTimestamp) {
            // Handle case where all measurements have the same timestamp
            maxTimestamp = minTimestamp + (24 * 60 * 60 * 1000); // Add 1 day
        }

        float totalHours = (maxTimestamp - minTimestamp) / (1000f * 60f * 60f);
        float visibleRange = Math.max(24f, Math.min(totalHours, 7 * 24f)); // Ensure at least 1 day visible

        nutrientsChart.setVisibleXRangeMaximum(visibleRange);
        nutrientsChart.moveViewToX(totalHours);
        nutrientsChart.getAxisLeft().resetAxisMinimum();
        nutrientsChart.getAxisLeft().resetAxisMaximum();
        nutrientsChart.fitScreen();
    }

    private void setupChartInteractions() {
        // Enable marker view
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mv.setChartView(nutrientsChart);
        nutrientsChart.setMarker(mv);

        nutrientsChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                nutrientsChart.getAxisLeft().resetAxisMinimum();
                nutrientsChart.getAxisLeft().resetAxisMaximum();
            }
            @Override public void onChartLongPressed(MotionEvent me) {}
            @Override public void onChartDoubleTapped(MotionEvent me) {}
            @Override public void onChartSingleTapped(MotionEvent me) {}
            @Override public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
            @Override public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}
            @Override public void onChartTranslate(MotionEvent me, float dX, float dY) {}
        });

        nutrientsChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                showDataPointDetails(e);
            }

            @Override
            public void onNothingSelected() {}
        });
    }

    private void optimizeChartPerformance() {
        nutrientsChart.setDrawMarkers(true);
        nutrientsChart.setMaxVisibleValueCount(100);
        nutrientsChart.setHardwareAccelerationEnabled(true);
    }

    private void showDataPointDetails(Entry entry) {
        Measurement measurement = findMeasurementByEntry(entry);

        if (measurement != null && getContext() != null) {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Nutrient Measurement")
                    .setMessage(createNutrientDetailsMessage(measurement))
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private Measurement findMeasurementByEntry(Entry entry) {
        if (measurementList == null || measurementList.isEmpty()) {
            return null;
        }

        long entryTimestamp = minTimestamp + (long)(entry.getX() * 60 * 60 * 1000);

        for (Measurement measurement : measurementList) {
            if (Math.abs(measurement.getTimestamp().getTime() - entryTimestamp) < 1000 * 60) {
                return measurement;
            }
        }

        return null;
    }

    private String createNutrientDetailsMessage(Measurement measurement) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        return String.format(Locale.getDefault(),
                "Timestamp: %s\n\n" +
                        "Nitrogen: %.1f ppm\n" +
                        "Phosphorus: %.1f ppm\n" +
                        "Potassium: %.1f ppm\n\n",
                dateFormat.format(measurement.getTimestamp()),
                measurement.getNitrogen(),
                measurement.getPhosphorus(),
                measurement.getPotassium(),
    }

    private class DateAxisValueFormatter extends ValueFormatter {
        private SimpleDateFormat dayFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
        private SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        private SimpleDateFormat weekFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            long timestamp = minTimestamp + (long)(value * 60 * 60 * 1000);
            Date date = new Date(timestamp);
            float totalRange = axis.getAxisMaximum() - axis.getAxisMinimum();

            if (totalRange <= 24) {
                return hourFormat.format(date);
            } else if (totalRange <= 24 * 7) {
                return dayFormat.format(date) + " " + hourFormat.format(date);
            } else {
                return weekFormat.format(date);
            }
        }
    }

    private class MyMarkerView extends MarkerView {
        private TextView tvDate;
        private TextView tvValue;

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvDate = findViewById(R.id.tvDate);
            tvValue = findViewById(R.id.tvValue);
        }

        @Override
        public void refreshContent(Entry e, Highlight h) {
            long timestamp = minTimestamp + (long)(e.getX() * 60 * 60 * 1000);
            String date = new SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
                    .format(new Date(timestamp));

            tvDate.setText(date);
            tvValue.setText(String.format(Locale.getDefault(), "%.1f ppm", e.getY()));
            super.refreshContent(e, h);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }
}
