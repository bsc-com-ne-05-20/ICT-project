package com.example.ssmsprojectapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {
    private List<BluetoothDevice> devices;
    private OnDeviceClickListener listener;

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    public DeviceListAdapter(List<BluetoothDevice> devices, OnDeviceClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new DeviceViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);
        holder.deviceName.setText(device.getName() != null ? device.getName() : "Unknown Device");
        holder.deviceAddress.setText(device.getAddress());

        holder.itemView.setOnClickListener(v -> listener.onDeviceClick(device));
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(android.R.id.text1);
            deviceAddress = itemView.findViewById(android.R.id.text2);
        }
    }
}
