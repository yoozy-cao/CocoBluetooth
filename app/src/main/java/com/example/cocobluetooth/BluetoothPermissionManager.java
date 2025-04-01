package com.example.cocobluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

public class BluetoothPermissionManager {
    public static final int REQUEST_BLUETOOTH_PERMISSION = 1001;
    private static final boolean SDK_GREAT_S31 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    private static final String TAG = BluetoothPermissionManager.class.getSimpleName();
    private static boolean isBluetoothErrorDialogShown = false;
    private Activity context;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    public BluetoothPermissionManager(Activity context) {
        if (context == null) throw new RuntimeException();
        this.context = context;
        this.bluetoothAdapter = getBluetoothAdapter();
    }

    public BluetoothManager getBluetoothManager() {
        if (bluetoothManager == null){

        }
        return bluetoothManager;
    }
    public BluetoothAdapter getBluetoothAdapter() {
        if (bluetoothAdapter == null) {
            BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }
        return bluetoothAdapter;
    }

    public void handleBluetoothPermissions() {
        // 检查设备是否支持蓝牙
        BluetoothAdapter btAdapter = getBluetoothAdapter();
        if (btAdapter == null) {
            Toast.makeText(context, "设备不支持蓝牙", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "设备支持蓝牙", Toast.LENGTH_SHORT).show();
            // 检查蓝牙权限
            if (SDK_GREAT_S31) {
                if (!checkBluetoothPermission()) {
                    // 请求权限
                    ActivityCompat.requestPermissions(
                            context,
                            new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                            REQUEST_BLUETOOTH_PERMISSION);
                    Log.i(TAG, "申请权限-蓝牙: " + Manifest.permission.BLUETOOTH_SCAN + "  \t" + Manifest.permission.BLUETOOTH_CONNECT);
                }
                // 权限已授予，检查蓝牙是否打开
                requestBluetoothEnable();
            } else {
                throw new RuntimeException("取消申请蓝牙权限 Android版本: " + Build.VERSION.RELEASE + "\tSDK版本: " + Build.VERSION.SDK_INT);
                // 旧版本系统不需要额外权限，直接检查蓝牙是否打开
                /*if (!btAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    context.startActivity(enableBtIntent);
                }*/
            }
        }
    }

    public boolean checkBluetoothPermission() {
        return PermissionUtils.checkPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                && PermissionUtils.checkPermission(context, Manifest.permission.BLUETOOTH_SCAN);
    }

    //请求启动蓝牙
    @SuppressLint("MissingPermission")
    public void requestBluetoothEnable() {
        if (!getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            context.startActivity(enableBtIntent);
        }
    }


    public String getBluetoothInfo() {
        // 获取安卓版本和SDK版本信息
        String androidVersion = "Android版本:" + Build.VERSION.RELEASE;
        String sdkVersion = "SDK版本:" + Build.VERSION.SDK_INT;
        // 获取蓝牙信息
        BluetoothAdapter btAdapter = getBluetoothAdapter();
        @SuppressLint("MissingPermission") String bluetoothInfo = "蓝牙名称: " + (btAdapter != null ? btAdapter.getName() : "不支持蓝牙");
        String bluetoothStatus = "蓝牙状态: " + (btAdapter != null && btAdapter.isEnabled() ? "已打开" : "已关闭");
        return androidVersion + "\t  \t" + sdkVersion + "\n" + bluetoothInfo + "\n" + bluetoothStatus;
    }


    public void checkBluetoothStatusAndPermission() {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter btAdapter = bluetoothManager.getAdapter();
        if (btAdapter != null
                && !btAdapter.isEnabled()
                && !isBluetoothErrorDialogShown) {
            showBluetoothErrorDialog();
            isBluetoothErrorDialogShown = true;
        }

        if (SDK_GREAT_S31
                && !PermissionUtils.checkPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                && !isBluetoothErrorDialogShown
        ) {
            showBluetoothErrorDialog();
            isBluetoothErrorDialogShown = true;
        }
    }

    public void showBluetoothErrorDialog() {
        context.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("蓝牙异常")
                    .setMessage("蓝牙出现异常，应用即将关闭。")
                    .setPositiveButton("确定", (dialog, which) -> {
                        context.finishAffinity();
                    })
                    .setCancelable(false)
                    .show();
        });
    }


}