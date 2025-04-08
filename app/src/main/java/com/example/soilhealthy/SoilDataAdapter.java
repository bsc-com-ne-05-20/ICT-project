package com.example.soilhealthy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SoilDataAdapter extends ArrayAdapter<SoilData> {
    private final Context mContext;
    private final List<SoilData> mDataList;
    private final UnifiedDatabaseManager mDbManager;
    private boolean mShowAverages = false;

    public SoilDataAdapter(Context context, List<SoilData> dataList, UnifiedDatabaseManager dbManager) {
        super(context, 0, dataList);
        this.mContext = context;
        this.mDataList = dataList;
        this.mDbManager = dbManager;
    }

    public void setShowAverages(boolean showAverages) {
        this.mShowAverages = showAverages;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mShowAverages ? 1 : mDataList.size();
    }

    @Nullable
    @Override
    public SoilData getItem(int position) {
        return mShowAverages ? calculateAverages() : mDataList.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_soil_data, parent, false);
        }

        SoilData data = getItem(position);
        if (data == null) {
            return convertView;
        }

        // Bind all views
        TextView tvPersonId = convertView.findViewById(R.id.tvPersonId);
        TextView tvTimestamp = convertView.findViewById(R.id.tvTimestamp);
        TextView tvTemperature = convertView.findViewById(R.id.tvTemperature);
        TextView tvSalinity = convertView.findViewById(R.id.tvSalinity);
        TextView tvPh = convertView.findViewById(R.id.tvPh);
        TextView tvMoisture = convertView.findViewById(R.id.tvMoisture);
        TextView tvNpk = convertView.findViewById(R.id.tvNpk);

        if (mShowAverages) {
            // Display averages data
            tvPersonId.setText("Average Values");
            tvTimestamp.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", data.getTemperature()));
            tvSalinity.setText(String.format(Locale.getDefault(), "%.1f", data.getSalinity()));
            tvPh.setText(String.format(Locale.getDefault(), "%.1f", data.getPh()));
            tvMoisture.setText(String.format(Locale.getDefault(), "%.1f%%", data.getMoisture()));
            tvNpk.setText(String.format(Locale.getDefault(), "%.1f-%.1f-%.1f",
                    data.getNitrogen(), data.getPhosphorus(), data.getPotassium()));

            convertView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.light_gray));
        } else {
            // Display regular data
            tvPersonId.setText(data.getPersonId());
            tvTimestamp.setText(data.getTimestamp());

            tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", data.getTemperature()));
            tvSalinity.setText(String.format(Locale.getDefault(), "%.1f", data.getSalinity()));
            tvPh.setText(String.format(Locale.getDefault(), "%.1f", data.getPh()));
            tvMoisture.setText(String.format(Locale.getDefault(), "%.1f%%", data.getMoisture()));
            tvNpk.setText(String.format(Locale.getDefault(), "%.1f-%.1f-%.1f",
                    data.getNitrogen(), data.getPhosphorus(), data.getPotassium()));

            convertView.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.transparent));
        }

        return convertView;
    }

    private SoilData calculateAverages() {
        if (mDataList.isEmpty()) {
            return new SoilData(0, 0, 0, 0, 0, 0, 0, "No Data Available");
        }

        float avgTemp = 0, avgSalinity = 0, avgPh = 0, avgMoisture = 0;
        float avgNitrogen = 0, avgPhosphorus = 0, avgPotassium = 0;

        for (SoilData data : mDataList) {
            avgTemp += data.getTemperature();
            avgSalinity += data.getSalinity();
            avgPh += data.getPh();
            avgMoisture += data.getMoisture();
            avgNitrogen += data.getNitrogen();
            avgPhosphorus += data.getPhosphorus();
            avgPotassium += data.getPotassium();
        }

        int count = mDataList.size();
        return new SoilData(
                avgTemp / count,
                avgSalinity / count,
                avgPh / count,
                avgMoisture / count,
                avgNitrogen / count,
                avgPhosphorus / count,
                avgPotassium / count,
                "AVERAGES_" + System.currentTimeMillis()
        );
    }

    public void saveCurrentAverages() {
        if (!mDataList.isEmpty()) {
            SoilData averages = calculateAverages();
            mDbManager.addSoilData(averages);
            notifyDataSetChanged();
        }
    }
}