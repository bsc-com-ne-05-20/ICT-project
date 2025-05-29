package com.example.ssmsprojectapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssmsprojectapp.datamodels.Measurement;

import java.util.List;

public class AllMeasurementsAdapter extends RecyclerView.Adapter<AllMeasurementsAdapter.ViewHolder> {

    private List<Measurement> measurements;



    private OnMeasurementClickListener listener;

    public interface OnMeasurementClickListener {
        void OnMeasurementClick(View view,Measurement measurement);
    }

    public AllMeasurementsAdapter(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    public AllMeasurementsAdapter(List<Measurement> measurements,OnMeasurementClickListener listener) {
        this.listener = listener;
        this.measurements = measurements;
    }

    @NonNull
    @Override
    public AllMeasurementsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.all_measurement_layout_item, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull AllMeasurementsAdapter.ViewHolder holder, int position) {

        Measurement measurement = measurements.get(position);
        holder.bind(measurement);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.OnMeasurementClick(v,measurement);
            }
        });

    }

    @Override
    public int getItemCount() {
        return measurements.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            date = itemView.findViewById(R.id.measurement_timestamp);
        }

        public void bind(Measurement measurement) {
            date.setText(measurement.getTimestamp().toLocaleString());
        }
    }
}
