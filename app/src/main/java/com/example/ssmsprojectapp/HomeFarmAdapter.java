package com.example.ssmsprojectapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssmsprojectapp.datamodels.Farm;

import java.util.List;

public class HomeFarmAdapter extends RecyclerView.Adapter<HomeFarmAdapter.ViewHolder> {

    private List<Farm> farms;
    private OnFarmClickListener listener;

    public interface OnFarmClickListener {
        void onFarmClick(Farm farm);
    }

    public HomeFarmAdapter(List<Farm> farms, OnFarmClickListener listener) {

        this.farms = farms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_farms_ritem, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Farm farm = farms.get(position);
        holder.bind(farm);
        holder.itemView.setOnClickListener(v -> listener.onFarmClick(farm));
    }

    @Override
    public int getItemCount() {
        return farms.size();
    }

    public void updateFarmData(List<Farm> newFarms) {
        farms = newFarms;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView location;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            location = itemView.findViewById(R.id.location);
        }

        public void bind(Farm farm) {
            name.setText(farm.getFarmName());
            location.setText(farm.getLocation());
        }
    }
}
