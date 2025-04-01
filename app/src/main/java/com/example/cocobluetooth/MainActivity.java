package com.example.cocobluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private final boolean SDK_GREAT_S31 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    private BluetoothPermissionManager btManager;
    private RecyclerView deviceListView;
    private BluetoothDeviceAdapter deviceAdapter;
    private List<BluetoothDevice> pairedDevices = new ArrayList<>();

    private static final int BUTTON_ENABLE_COLOR = Color.BLUE;
    private static final int BUTTON_DISENABLE_COLOR = Color.GRAY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()..");

        // 设置全局异常捕获器
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            Log.e(TAG, "全局异常捕获器 Uncaught exception: ", ex);
            // 可以在这里添加更多处理逻辑，如重启应用等
        });

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Log.i(TAG, "Android版本: " + Build.VERSION.RELEASE + "\tSDK版本: " + Build.VERSION.SDK_INT);

        btManager = new BluetoothPermissionManager(this);
        // 处理蓝牙权限
        btManager.handleBluetoothPermissions();
        BluetoothAdapter btAdapter = btManager.getBluetoothAdapter();

        // 获取蓝牙按钮
        Button btButton = findViewById(R.id.bluetooth_switch_button);
        // 设置按钮点击事件
        btButton.setOnClickListener(v -> {
//            Toast.makeText(this, "按钮点击", Toast.LENGTH_SHORT).show();
            if (!btManager.checkBluetoothPermission()) {
                Toast.makeText(this, "蓝牙权限异常", Toast.LENGTH_LONG).show();
                return;
            }
            if (btAdapter.isEnabled()) {
                scanBluetoothDevices();
            } else {
                Toast.makeText(this, "请打开蓝牙", Toast.LENGTH_SHORT).show();
            }
            // 根据蓝牙初始状态设置按钮颜色
            updateBtButtonState(btAdapter.isEnabled() ? BluetoothAdapter.STATE_ON : BluetoothAdapter.STATE_OFF);
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 蓝牙权限请求结果
        if (requestCode == BluetoothPermissionManager.REQUEST_BLUETOOTH_PERMISSION) {
            Log.i(TAG, "蓝牙权限授予 results: " + Arrays.toString(grantResults));
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "蓝牙权限被拒绝，无法扫描设备", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()..");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()..");
        // 注销广播接收器
        try {
            unregisterReceiver(mBluetoothStateReceiver);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Receiver not registered");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()..");
        // 注册蓝牙状态变化广播接收器
        registerReceiver(mBluetoothStateReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        // 更新蓝牙状态信息
        TextView infoText = findViewById(R.id.info_text_view);
        infoText.setText(btManager.getBluetoothInfo());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart()..");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()..");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()..");
    }

    @SuppressLint("MissingPermission")
    private void scanBluetoothDevices() {
        // 获取已配对设备
        pairedDevices.clear();
        BluetoothAdapter btAdapter = btManager.getBluetoothAdapter();
        pairedDevices.addAll(btAdapter.getBondedDevices());

        // 初始化RecyclerView
        deviceListView = findViewById(R.id.device_list_recycler_view);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));
        deviceAdapter = new BluetoothDeviceAdapter(pairedDevices);
        deviceListView.setAdapter(deviceAdapter);

        // 扫描新设备
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryReceiver, filter);
        btAdapter.startDiscovery();
    }

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (!pairedDevices.contains(device)) {
                    pairedDevices.add(device);
                    deviceAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    /**
     * 蓝牙状态变化广播接收器
     * 功能：监听蓝牙状态变化并更新UI
     * 工作原理：
     * 1. 在onResume()中注册，监听BluetoothAdapter.ACTION_STATE_CHANGED广播
     * 2. 在onPause()中注销，避免内存泄漏
     * 3. 当蓝牙状态变化时，系统会发送广播，触发onReceive()方法
     * 4. 根据接收到的状态值更新UI显示和按钮颜色
     * <p>
     * 状态转换说明：
     * - STATE_TURNING_ON: 蓝牙正在开启（过渡状态）
     * - STATE_ON: 蓝牙已开启
     * - STATE_TURNING_OFF: 蓝牙正在关闭（过渡状态）
     * - STATE_OFF: 蓝牙已关闭
     */
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // 获取广播的action
            final String action = intent.getAction();
            // 只处理蓝牙状态变化的广播
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                // 从intent中获取蓝牙状态值，如果获取失败则返回ERROR
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                // 在UI线程中更新界面
                runOnUiThread(() -> {
                    updateBtButtonState(state);
                });
            }
        }
    };

    public void updateBtButtonState(int state) {
        TextView infoText = findViewById(R.id.info_text_view);
        Button bluetoothSwitchButton = findViewById(R.id.bluetooth_switch_button);
        switch (state) {
            case BluetoothAdapter.STATE_TURNING_ON:
                infoText.setText("蓝牙正在开启...");
                bluetoothSwitchButton.setBackgroundColor(Color.YELLOW);
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                infoText.setText("蓝牙正在关闭...");
                bluetoothSwitchButton.setBackgroundColor(Color.YELLOW);
                break;
            case BluetoothAdapter.STATE_ON:
                infoText.setText(btManager.getBluetoothInfo());
                bluetoothSwitchButton.setBackgroundColor(BUTTON_ENABLE_COLOR);
                break;
//            case BluetoothAdapter.STATE_OFF:
            default:
                infoText.setText(btManager.getBluetoothInfo());
                bluetoothSwitchButton.setBackgroundColor(BUTTON_DISENABLE_COLOR);
                break;
            /*default:
                throw new IllegalStateException("Unexpected value: " + state);*/
        }
    }

}