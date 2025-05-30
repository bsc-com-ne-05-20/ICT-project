package com.example.ssmsprojectapp;

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

public class PhFragment extends Fragment {

    private LineChart phChart;
    private List<Measurement> measurementList;
    private MeasurementDbHelper measurementDbHelper;
    private SwipeRefreshLayout swipeRefreshLayout;
    private long minTimestamp = Long.MAX_VALUE;

    public PhFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ph, container, false);

        // Initialize swipe refresh
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        phChart = view.findViewById(R.id.ph_chart);
        measurementDbHelper = new MeasurementDbHelper(getContext());

        setupEmptyChartState();
        setupPhChart();
        loadInitialData();

        return view;
    }

    private void setupEmptyChartState() {
        phChart.clear();
        phChart.setNoDataText("Loading pH data...");
        phChart.setNoDataTextColor(Color.GRAY);
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

    private void setupPhChart() {
        try {
            phChart.clear();
            phChart.getDescription().setEnabled(true);
            phChart.getDescription().setText("Soil pH Over Time");
            phChart.setBackgroundColor(Color.WHITE);

            // Legend customization
            Legend legend = phChart.getLegend();
            legend.setForm(Legend.LegendForm.LINE);
            legend.setTextSize(12f);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
            legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
            legend.setDrawInside(false);

            // X-axis advanced setup
            XAxis xAxis = phChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setLabelRotationAngle(-45);
            xAxis.setValueFormatter(new DateAxisValueFormatter());
            xAxis.setAxisLineColor(Color.BLACK);
            xAxis.setGridColor(Color.LTGRAY);
            xAxis.setAxisMinimum(0f);
            xAxis.setLabelCount(7, true);

            // Left Y-axis
            YAxis leftAxis = phChart.getAxisLeft();
            leftAxis.setAxisMinimum(0f);
            leftAxis.setAxisMaximum(14f); // pH scale range
            leftAxis.setGranularity(1f);
            leftAxis.setTextColor(Color.BLUE);
            leftAxis.setValueFormatter(new PhValueFormatter());
            leftAxis.setAxisLineColor(Color.BLACK);
            leftAxis.setGridColor(Color.LTGRAY);

            // Right Y-axis disabled
            phChart.getAxisRight().setEnabled(false);

            // Add pH range markers
            addPhRangeMarkers();

            // Setup interactions
            setupChartInteractions();

            // Performance optimizations
            optimizeChartPerformance();

        } catch (Exception e) {
            Log.e("ChartSetup", "Error configuring chart", e);
        }
    }

    private void addPhRangeMarkers() {
        YAxis leftAxis = phChart.getAxisLeft();

        // Acidic range (0-6.5)
        /*LimitLine acidicUpper = new LimitLine(6.5f, "Optimal Max");
        acidicUpper.setLineColor(Color.RED);
        acidicUpper.setLineWidth(1f);
        acidicUpper.enableDashedLine(10f, 10f, 0f);*/

        // Neutral range (6.5-7.5)
        LimitLine optimalMax = new LimitLine(7.0f, "Optimal Max");
        optimalMax.setLineColor(Color.GREEN);
        optimalMax.setLineWidth(1f);
        optimalMax.enableDashedLine(10f, 10f, 0f);

        LimitLine optimalMin = new LimitLine(5.5f, "optimal Min");
        optimalMin.setLineColor(Color.GREEN);
        optimalMin.setLineWidth(1f);
        optimalMin.enableDashedLine(10f, 10f, 0f);


        //leftAxis.addLimitLine(acidicUpper);
        leftAxis.addLimitLine(optimalMax);
        leftAxis.addLimitLine(optimalMin);
        //leftAxis.addLimitLine(alkalineLower);
    }

    private void updateChartWithData() {
        if (measurementList == null || measurementList.isEmpty()) {
            phChart.setNoDataText("No pH data available");
            phChart.invalidate();
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
            float phValue = (float) m.getPh();

            if (!Float.isNaN(phValue) && !Float.isInfinite(phValue)) {
                entries.add(new Entry(normalizedTime, phValue));
            }
        }

        if (entries.isEmpty()) {
            phChart.setNoDataText("No valid pH data");
            phChart.invalidate();
            return;
        }

        // Sort by time
        Collections.sort(entries, (e1, e2) -> Float.compare(e1.getX(), e2.getX()));

        // Create dataset
        LineDataSet dataSet = new LineDataSet(entries, "Soil pH");
        styleDataSet(dataSet);

        LineData lineData = new LineData(dataSet);
        phChart.setData(lineData);

        // Configure viewport
        configureViewport(minTimestamp, maxTimestamp);

        phChart.invalidate();
    }

    private void styleDataSet(LineDataSet dataSet) {
        // Line styling
        dataSet.setColor(Color.parseColor("#9C27B0")); // Purple color for pH
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.parseColor("#9C27B0"));
        dataSet.setCircleRadius(3f);
        dataSet.setCircleHoleRadius(1.5f);
        dataSet.setValueTextSize(10f);
        dataSet.setValueFormatter(new PhValueFormatter());
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

        phChart.setVisibleXRangeMaximum(visibleRange);
        phChart.moveViewToX(totalHours);

        phChart.getAxisLeft().resetAxisMinimum();
        phChart.getAxisLeft().resetAxisMaximum();
        phChart.fitScreen();
    }

    private void setupChartInteractions() {
        // Enable marker view
        MyMarkerView mv = new MyMarkerView(getContext(), R.layout.custom_marker_view);
        mv.setChartView(phChart);
        phChart.setMarker(mv);

        phChart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                phChart.getAxisLeft().resetAxisMinimum();
                phChart.getAxisLeft().resetAxisMaximum();
            }
            @Override public void onChartLongPressed(MotionEvent me) {}
            @Override public void onChartDoubleTapped(MotionEvent me) {}
            @Override public void onChartSingleTapped(MotionEvent me) {}
            @Override public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
            @Override public void onChartScale(MotionEvent me, float scaleX, float scaleY) {}
            @Override public void onChartTranslate(MotionEvent me, float dX, float dY) {}
        });

        phChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                showDataPointDetails(e);
            }

            @Override
            public void onNothingSelected() {}
        });
    }

    private void optimizeChartPerformance() {
        phChart.setDrawMarkers(true);
        phChart.setMaxVisibleValueCount(100);
        phChart.setHardwareAccelerationEnabled(true);
    }

    private void showDataPointDetails(Entry entry) {
        Measurement measurement = findMeasurementByEntry(entry);

        if (measurement != null && getContext() != null) {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("pH Measurement")
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
                        "pH: %.1f\n\n",
                dateFormat.format(measurement.getTimestamp()),
                measurement.getPh(),
                measurement.getSalinity(),
                measurement.getMoisture(),
                measurement.getTemperature());
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

    private class PhValueFormatter extends ValueFormatter {
        @Override
        public String getPointLabel(Entry entry) {
            return String.format(Locale.getDefault(), "%.1f", entry.getY());
        }

        @Override
        public String getAxisLabel(float value, AxisBase axis) {
            return String.format(Locale.getDefault(), "%.1f", value);
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
            tvValue.setText(String.format(Locale.getDefault(), "pH: %.1f", e.getY()));
            super.refreshContent(e, h);
        }

        @Override
        public MPPointF getOffset() {
            return new MPPointF(-(getWidth() / 2), -getHeight());
        }
    }
}
