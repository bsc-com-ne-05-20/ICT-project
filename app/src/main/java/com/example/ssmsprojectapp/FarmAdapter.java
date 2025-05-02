package com.example.ssmsprojectapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ssmsprojectapp.datamodels.Farm;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.MarkerOptions;
import java.util.List;

public class FarmAdapter extends RecyclerView.Adapter<FarmAdapter.FarmViewHolder> {

    private List<Farm> farms;
    private OnFarmClickListener listener;

    public interface OnFarmClickListener {
        void onFarmClick(Farm farm);
    }

    public FarmAdapter(List<Farm> farms,OnFarmClickListener listener) {

        this.farms = farms;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.farm_item, parent, false);
        return new FarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FarmViewHolder holder, int position) {
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

    static class FarmViewHolder extends RecyclerView.ViewHolder {
        private TextView locationTextView;
        private TextView soilTypeTextView;
        private TextView metalsTextView;

        private TextView placeName;
        private MapView mapView;

        public FarmViewHolder(@NonNull View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.farm_location);
            soilTypeTextView = itemView.findViewById(R.id.farm_soil_type);
            metalsTextView = itemView.findViewById(R.id.farm_metals);



        }



        public void bind(Farm farm) {
            locationTextView.setText(String.format("Location: %.4f, %.4f", farm.getLatitude(), farm.getLongitude()));
            soilTypeTextView.setText(String.format("Soil Type: %s", farm.getSoilType()));
            metalsTextView.setText("none");
        }


    }
}
