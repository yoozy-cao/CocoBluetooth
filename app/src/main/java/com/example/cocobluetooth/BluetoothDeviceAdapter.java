package com.example.cocobluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BluetoothDeviceAdapter extends RecyclerView.Adapter<BluetoothDeviceAdapter.DeviceViewHolder> {
    private final List<BluetoothDevice> devices;

    public BluetoothDeviceAdapter(List<BluetoothDevice> devices) {
        this.devices = devices;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_device_multi_column, parent, false);
        return new DeviceViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);

        // 获取设备别名(如果有)
        String alias = device.getAlias() != null ? device.getAlias() : "无别名";
        holder.deviceAlias.setText(alias);

        // 获取设备名称
        String name = device.getName() != null ? device.getName() : "未知设备";
        holder.deviceName.setText(name);

        // 获取绑定状态
        String bondState;
        switch (device.getBondState()) {
            case BluetoothDevice.BOND_BONDED:
                bondState = "已配对";
                break;
            case BluetoothDevice.BOND_BONDING:
                bondState = "配对中";
                break;
            default:
                bondState = "未配对";
        }
        holder.deviceBondState.setText(bondState);

        // 获取设备类型
        String type;
        switch (device.getType()) {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                type = "经典";
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                type = "低功耗";
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                type = "双模";
                break;
            default:
                type = "未知";
        }
        holder.deviceType.setText(type);

        // 获取设备地址
        holder.deviceAddress.setText(device.getAddress());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    // 修改ViewHolder类
    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceAlias;
        TextView deviceName;
        TextView deviceBondState;
        TextView deviceType;
        TextView deviceAddress;

        public DeviceViewHolder(@NonNull View itemView) {
            super(itemView);
            deviceAlias = itemView.findViewById(R.id.device_alias);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceBondState = itemView.findViewById(R.id.device_bond_state);
            deviceType = itemView.findViewById(R.id.device_type);
            deviceAddress = itemView.findViewById(R.id.device_address);
        }
    }
}