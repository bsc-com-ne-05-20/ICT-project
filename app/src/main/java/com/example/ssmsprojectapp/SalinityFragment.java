package com.example.ssmsprojectapp;

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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
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

public class SalinityFragment extends Fragment {

    private LineChart salinityChart;
    private List<Measurement> measurementList;
    private MeasurementDbHelper measurementDbHelper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long minTimestamp = Long.MAX_VALUE;

    public SalinityFragment() {
        // Required empty public constructor
    }

    public SalinityFragment(List<Measurement> measurements) {
        this.measurementList = measurements;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salinity, container, false);

        // Initialize swipe refresh
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        salinityChart = view.findViewById(R.id.salinity_chart);
        measurementDbHelper = new MeasurementDbHelper(getContext());

        setupEmptyChartState();
        setupSalinityChart();
        loadInitialData();

        return view;
    }

    private void setupEmptyChartState() {
        salinityChart.clear();
        salinityChart.setNoDataText("Loading salinity data...");
        salinityChart.setNoDataTextColor(Color.GRAY);
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

    private void setupSalinityChart() {
        try {
            salinityChart.clear();
            salinityChart.getDescription().setEnabled(true);
            salinityChart.getDescription().setText("Soil Salinity Over Time");
            salinityChart.setBackgroundColor(Color.WHITE);

            // Legend customization
            Legend legend = salinityChart.getLegend();
            legend.setForm(Legend.LegendForm.LINE);
            legend.setTextSize(12f);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);

            // X-axis advanced setup
            XAxis xAxis = salinityChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setValueFormatter(new DateAxisValueFormatter());
            xAxis.setAxisLineColor(Color.BLACK);
            xAxis.setGridColor(Color.LTGRAY);
            xAxis.setAxisMinimum(0f);
            xAxis.setLabelCount(7, true);

            // Left Y-axis
            YAxis leftAxis = salinityChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);
            leftAxis.setAxisMaximum(10f); // Typical salinity range in dS/m
            leftAxis.setGranularity(1f);
            leftAxis.setTextColor(Color.BLUE);
            leftAxis.setValueFormatter(new SalinityValueFormatter());
            leftAxis.setAxisLineColor(Color.BLACK);
            leftAxis.setGridColor(Color.LTGRAY);

            // Right Y-axis disabled
            salinityChart.getAxisRight().setEnabled(false);

            // Add salinity range markers
            addSalinityRangeMarkers();

            // Setup interactions
            setupChartInteractions();

            // Performance optimizations
            optimizeChartPerformance();

        } catch (Exception e) {
            Log.e("ChartSetup", "Error configuring chart", e);
        }
    }

    private void addSalinityRangeMarkers() {
        YAxis leftAxis = salinityChart.getAxisLeft();

        // Optimal range (0-2 dS/m)
        LimitLine optimalUpper = new LimitLine(2f, "Optimal Max");
        optimalUpper.setLineColor(Color.GREEN);
        optimalUpper.setLineWidth(1f);
        optimalUpper.enableDashedLine(10f, 10f, 0f);

        // Moderate salinity (2-4 dS/m)
        LimitLine moderateUpper = new LimitLine(4f, "Moderate Max");
        moderateUpper.setLineColor(Color.YELLOW);
        moderateUpper.setLineWidth(1f);
        moderateUpper.enableDashedLine(10f, 10f, 0f);

        // High salinity (>4 dS/m)
        LimitLine highUpper = new LimitLine(8f, "High Salinity");
        highUpper.setLineColor(Color.RED);
        highUpper.setLineWidth(1f);
        highUpper.enableDashedLine(10f, 10f, 0f);

        leftAxis.addLimitLine(optimalUpper);
        leftAxis.addLimitLine(moderateUpper);
        leftAxis.addLimitLine(highUpper);
    }

    private void updateChartWithData() {
        if (measurementList == null || measurementList.isEmpty()) {
            salinityChart.setNoDataText("No salinity data available");
            salinityChart.invalidate();
            return;
        }

        // Process data
        List<Entry> entries = new ArrayList<>();
        minTimestamp = Long.MAX_VALUE;
        long maxTimestamp = Long.MIN_VALUE;

        // Find time range and validate data
        for (Measurement m : measurementList) {
            minTimestamp = Math.min(minTimestamp, m.getTimestamp().getTime());
            maxTimestamp = Math.max(maxTimestamp, m.getTimestamp().getTime());
        }

        // Normalize timestamps and create entries
        for (Measurement m : measurementList) {
            float normalizedTime = (m.getTimestamp().getTime() - minTimestamp) / (1000f * 60f * 60f); // hours
            float salinity = (float) m.getSalinity();

            if (!Float.isNaN(salinity) && !Float.isInfinite(salinity)) {
                entries.add(new Entry(normalizedTime, salinity));
            }
        }

        if (entries.isEmpty()) {
            salinityChart.setNoDataText("No valid salinity data");
            salinityChart.invalidate();
            return;
        }

        // Sort by time
        Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Soil Salinity (dS/m)");
        styleDataSet(dataSet);

        LineData lineData = new LineData(dataSet);
        salinityChart.setData(lineData);

        // Configure viewport
        configureViewport(minTimestamp, maxTimestamp);

        salinityChart.invalidate();
    }

    private void styleDataSet(LineDataSet dataSet) {
        // Line styling
        dataSet.setColor(Color.parseColor("#8E44AD")); // Purple color for salinity
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.parseColor("#8E44AD"));
        dataSet.setCircleRadius(3f);
        dataSet.setCircleHoleRadius(1.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new SalinityValueFormatter());
        dataSet.setDrawValues(false);

        // Enable cubic lines for smoother curves
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Gradient fill
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient_purple);
            dataSet.setFillDrawable(drawable);
        }
        dataSet.setFillAlpha(80);
        dataSet.setDrawFilled(true);

        // Highlighting
        dataSet.setHighLightColor(Color.RED);
        dataSet.setHighlightLineWidth(1f);
        dataSet.setDrawHorizontalHighlightIndicator(false);
    }

    private void configureViewport(long minTimestamp, long maxTimestamp) {
        float totalHours = (maxTimestamp - minTimestamp) / (1000f * 60f * 60f);
        float visibleRange = Math.min(totalHours, 7 * 24f); // Show max 7 days

        salinityChart.setVisibleXRangeMaximum(visibleRange);
        salinityChart.moveViewToX(totalHours);

        salinityChart.getAxisLeft().resetAxisMinimum();
        salinityChart.getAxisLeft().resetAxisMaximum();
        salinityChart.fitScreen();
    }

    private void setupChartInteractions() {
        // Enable marker view
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mv.setChartView(salinityChart);
        salinityChart.setMarker(mv);

        salinityChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                salinityChart.getAxisLeft().resetAxisMinimum();
                salinityChart.getAxisLeft().resetAxisMaximum();
            }
            @Override public void onChartLongPressed(MotionEvent me) {}
            @Override public void onChartDoubleTapped(MotionEvent me) {}
            @Override public void onChartSingleTapped(MotionEvent me) {}
            @Override public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
            @Override public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}
            @Override public void onChartTranslate(MotionEvent me, float dX, float dY) {}
        });

        salinityChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                showDataPointDetails(e);
            }

            @Override
            public void onNothingSelected() {}
        });
    }

    private void optimizeChartPerformance() {
        salinityChart.setDrawMarkers(true);
        salinityChart.setMaxVisibleValueCount(100);
        salinityChart.setHardwareAccelerationEnabled(true);
    }

    private void showDataPointDetails(Entry entry) {
        Measurement measurement = findMeasurementByEntry(entry);

        if (measurement != null && getContext() != null) {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Salinity Measurement")
                    .setMessage(createMeasurementDetailsMessage(measurement))
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

    private String createMeasurementDetailsMessage(Measurement measurement) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        return String.format(Locale.getDefault(),
                "Timestamp: %s\n\n" +
                        "Salinity: %.1f dS/m\n\n" +
                        "Moisture: %.1f%%\n" +
                        "Temperature: %.1f°C\n" +
                        "pH: %.1f",
                dateFormat.format(measurement.getTimestamp()),
                measurement.getSalinity(),
                measurement.getMoisture(),
                measurement.getTemperature(),
                measurement.getPh());
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

    private class SalinityValueFormatter extends ValueFormatter {
        @Override
        public String getPointLabel(Entry entry) {
            return String.format(Locale.getDefault(), "%.1f dS/m", entry.getY());
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return String.format(Locale.getDefault(), "%.1f dS/m", value);
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
            tvValue.setText(String.format(Locale.getDefault(), "%.1f dS/m", e.getY()));
            super.refreshContent(e, h);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }
}