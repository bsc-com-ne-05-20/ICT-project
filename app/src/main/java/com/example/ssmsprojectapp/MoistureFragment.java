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

public class MoistureFragment extends Fragment {

    private LineChart moistureChart;
    private List<Measurement> measurementList;
    private MeasurementDbHelper measurementDbHelper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long minTimestamp = Long.MAX_VALUE;

    public MoistureFragment() {
        // Required empty public constructor
    }

    public MoistureFragment(List<Measurement> measurements) {
        this.measurementList = measurements;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_moisture, container, false);

        // Initialize swipe refresh
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        moistureChart = view.findViewById(R.id.moisture_chart);
        measurementDbHelper = new MeasurementDbHelper(getContext());

        setupMoistureChart();
        setupEmptyChartState();
        loadInitialData();

        return view;
    }

    private void setupEmptyChartState() {
        moistureChart.clear();
        moistureChart.setNoDataText("Loading moisture data...");
        moistureChart.setNoDataTextColor(Color.GRAY);
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

    private void setupMoistureChart() {
        try {
            moistureChart.clear();
            moistureChart.getDescription().setEnabled(true);
            moistureChart.getDescription().setText("Last 7 Days Soil Moisture");
            moistureChart.setBackgroundColor(Color.WHITE);

            // Legend customization
            Legend legend = moistureChart.getLegend();
            legend.setForm(Legend.LegendForm.LINE);
            legend.setTextSize(12f);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);

            // X-axis advanced setup
            XAxis xAxis = moistureChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setValueFormatter(new DateAxisValueFormatter());
            xAxis.setAxisLineColor(Color.BLACK);
            xAxis.setGridColor(Color.LTGRAY);
            xAxis.setAxisMinimum(0f);
            xAxis.setLabelCount(7, true);

            // Left Y-axis
            YAxis leftAxis = moistureChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);
            leftAxis.setAxisMaximum(100f);
            leftAxis.setGranularity(10f);
            leftAxis.setTextColor(Color.BLUE);
            leftAxis.setValueFormatter(new PercentValueFormatter());
            leftAxis.setAxisLineColor(Color.BLACK);
            leftAxis.setGridColor(Color.LTGRAY);

            // Right Y-axis disabled
            moistureChart.getAxisRight().setEnabled(false);

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
        YAxis leftAxis = moistureChart.getAxisLeft();

        LimitLine optimalLower = new LimitLine(40f, "Optimal Min");
        optimalLower.setLineColor(Color.GREEN);
        optimalLower.setLineWidth(1f);
        optimalLower.enableDashedLine(10f, 10f, 0f);

        LimitLine optimalUpper = new LimitLine(60f, "Optimal Max");
        optimalUpper.setLineColor(Color.GREEN);
        optimalUpper.setLineWidth(1f);
        optimalUpper.enableDashedLine(10f, 10f, 0f);

        leftAxis.addLimitLine(optimalLower);
        leftAxis.addLimitLine(optimalUpper);
    }

    private void updateChartWithData() {
        if (measurementList == null || measurementList.isEmpty()) {
            moistureChart.setNoDataText("No moisture data available");
            moistureChart.invalidate();
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
            float moisture = (float) m.getMoisture();

            if (!Float.isNaN(moisture)) {
                entries.add(new Entry(normalizedTime, moisture));
            }
        }

        if (entries.isEmpty()) {
            moistureChart.setNoDataText("No valid moisture data");
            moistureChart.invalidate();
            return;
        }

        // Sort by time
        Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Soil Moisture");
        styleDataSet(dataSet);

        LineData lineData = new LineData(dataSet);
        moistureChart.setData(lineData);

        // Configure viewport
        configureViewport(minTimestamp, maxTimestamp);

        moistureChart.invalidate();
    }

    private void styleDataSet(LineDataSet dataSet) {
        // Line styling
        dataSet.setColor(Color.parseColor("#2E86AB"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.parseColor("#2E86AB"));
        dataSet.setCircleRadius(3f);
        dataSet.setCircleHoleRadius(1.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new PercentValueFormatter());
        dataSet.setDrawValues(false);

        // Enable cubic lines for smoother curves
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        // Gradient fill
        /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient);
            dataSet.setFillDrawable(drawable);
        }
        dataSet.setFillAlpha(80);
        dataSet.setDrawFilled(true);*/

        // Highlighting
        dataSet.setHighLightColor(Color.RED);
        dataSet.setHighlightLineWidth(1f);
        dataSet.setDrawHorizontalHighlightIndicator(false);
    }

    private void configureViewport(long minTimestamp, long maxTimestamp) {
        float totalHours = (maxTimestamp - minTimestamp) / (1000f * 60f * 60f);
        float visibleRange = Math.min(totalHours, 7 * 24f);

        moistureChart.setVisibleXRangeMaximum(visibleRange);
        moistureChart.moveViewToX(totalHours);

        moistureChart.getAxisLeft().resetAxisMinimum();
        moistureChart.getAxisLeft().resetAxisMaximum();
        moistureChart.fitScreen();
    }

    private void setupChartInteractions() {
        // Enable marker view
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mv.setChartView(moistureChart);
        moistureChart.setMarker(mv);

        moistureChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                moistureChart.getAxisLeft().resetAxisMinimum();
                moistureChart.getAxisLeft().resetAxisMaximum();
            }
            @Override public void onChartLongPressed(MotionEvent me) {}
            @Override public void onChartDoubleTapped(MotionEvent me) {}
            @Override public void onChartSingleTapped(MotionEvent me) {}
            @Override public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
            @Override public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}
            @Override public void onChartTranslate(MotionEvent me, float dX, float dY) {}
        });

        moistureChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                showDataPointDetails(e);
            }

            @Override
            public void onNothingSelected() {}
        });
    }

    private void optimizeChartPerformance() {
        moistureChart.setDrawMarkers(true);
        moistureChart.setMaxVisibleValueCount(100);
        moistureChart.setHardwareAccelerationEnabled(true);
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

    private class MyMarkerView extends MarkerView {
        private TextView tvDate;
        private TextView tvValue;

        public MyMarkerView(Context context, int layoutResource) {
            super(context, layoutResource);
            tvDate = findViewById(R.id.tvDate);
            tvValue = findViewById(R.id.tvValue);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            long timestamp = minTimestamp + (long)(e.getX() * 60 * 60 * 1000);
            String date = new SimpleDateFormat("MMM dd HH:mm", Locale.getDefault())
                    .format(new Date(timestamp));

            tvDate.setText(date);
            tvValue.setText(String.format(Locale.getDefault(), "%.1f%%", e.getY()));
            super.refreshContent(e, highlight);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }


    private void showDataPointDetails(Entry entry) {
        // Find the corresponding measurement for this data point
        Measurement measurement = findMeasurementByEntry(entry);

        if (measurement != null && getContext() != null) {
            // Create a detailed dialog showing all measurement data
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Measurement Details")
                    .setMessage(createMeasurementDetailsMessage(measurement))
                    .setPositiveButton("OK", null)
                    .show();
        }
    }

    private Measurement findMeasurementByEntry(Entry entry) {
        if (measurementList == null || measurementList.isEmpty()) {
            return null;
        }

        // Calculate the exact timestamp for this entry
        long entryTimestamp = minTimestamp + (long)(entry.getX() * 60 * 60 * 1000);

        // Find the closest matching measurement
        for (Measurement measurement : measurementList) {
            if (Math.abs(measurement.getTimestamp().getTime() - entryTimestamp) < 1000 * 60) { // Within 1 minute
                return measurement;
            }
        }

        return null;
    }

    private String createMeasurementDetailsMessage(Measurement measurement) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        return String.format(Locale.getDefault(),
                "Timestamp: %s\n\n" +
                        "Moisture: %.1f%%\n" +
                        "Temperature: %.1f°C\n" +
                        "pH: %.1f\n" +
                        "Salinity: %.1f dS/m\n" +
                        "Nitrogen: %.1f mg/kg\n" +
                        "Phosphorus: %.1f mg/kg\n" +
                        "Potassium: %.1f mg/kg\n" +
                        "Metals: %s",
                dateFormat.format(measurement.getTimestamp()),
                measurement.getMoisture(),
                measurement.getTemperature(),
                measurement.getPh(),
                measurement.getSalinity(),
                measurement.getNitrogen(),
                measurement.getPhosphorus(),
                measurement.getPotassium(),
                measurement.getMetals() != null ? measurement.getMetals() : "N/A");
    }
}