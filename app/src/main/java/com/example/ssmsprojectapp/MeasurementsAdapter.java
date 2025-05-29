package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssmsprojectapp.datamodels.Farm;
import com.example.ssmsprojectapp.datamodels.Measurement;

import java.util.List;

public class MeasurementsAdapter extends RecyclerView.Adapter<MeasurementsAdapter.ViewHolher> {

    private List<Measurement> measurements;

    private OnMeasurementClickListener listener;

    public interface OnMeasurementClickListener {
        void OnMeasurementClick(View view,Measurement measurement);
    }

    public MeasurementsAdapter(List<Measurement> measurements,OnMeasurementClickListener listener) {
        this.measurements = measurements;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MeasurementsAdapter.ViewHolher onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.measurement_item, parent, false);
        return new ViewHolher(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeasurementsAdapter.ViewHolher holder, int position) {

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

    public void updateData(List<Measurement> newMeasurements) {
        measurements = newMeasurements;
        notifyDataSetChanged();
    }

    public class ViewHolher extends RecyclerView.ViewHolder {

        TextView date;
        public ViewHolher(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.mDate);
        }

        public void bind(Measurement measurement) {
            date.setText(measurement.getTimestamp().toLocaleString());
        }
    }
}
