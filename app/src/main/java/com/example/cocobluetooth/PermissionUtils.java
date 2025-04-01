package com.example.cocobluetooth;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class PermissionUtils {

     /**
     * 检查特定权限
     *
     * @param context    上下文对象
     * @param permission 要检查的权限
     * @return 如果权限已授予，则返回true；否则返回false
     */
    public static boolean checkPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}